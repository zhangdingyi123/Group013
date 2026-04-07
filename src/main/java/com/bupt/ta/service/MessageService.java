package com.bupt.ta.service;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.DirectMessage;
import com.bupt.ta.model.DmReadState;
import com.bupt.ta.model.Job;
import com.bupt.ta.storage.Storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MessageService {
    private static final int MAX_BODY_LEN = 4000;

    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    private final FriendService friendService = new FriendService();

    public List<DirectMessage> findConversation(String applicantId, String moduleOrganiserId) throws IOException {
        return Storage.loadMessages().stream()
                .filter(m -> applicantId.equals(m.getApplicantId()) && moduleOrganiserId.equals(m.getModuleOrganiserId()))
                .sorted(Comparator.comparingLong(DirectMessage::getSentAt))
                .collect(Collectors.toList());
    }

    /**
     * 应聘者可私信的招聘者：对其岗位有过未撤销申请的招聘者，或互为好友。
     */
    public Set<String> contactableMoIdsForApplicant(String applicantId) throws IOException {
        Set<String> ids = new LinkedHashSet<>();
        for (Application a : applicationService.findByApplicantId(applicantId)) {
            if (Application.STATUS_CANCELLED.equals(a.getStatus())) {
                continue;
            }
            Optional<Job> j = jobService.findById(a.getJobId());
            j.ifPresent(job -> ids.add(job.getModuleOrganiserId()));
        }
        for (String moId : friendService.friendMoIdsForApplicant(applicantId)) {
            ids.add(moId);
        }
        return ids;
    }

    /** 是否与该招聘者存在未撤销的申请关系（任一名下岗位）。 */
    public boolean hasNonCancelledApplicationToMo(String applicantId, String moId) throws IOException {
        for (Application a : applicationService.findByApplicantId(applicantId)) {
            if (Application.STATUS_CANCELLED.equals(a.getStatus())) {
                continue;
            }
            Optional<Job> j = jobService.findById(a.getJobId());
            if (j.isPresent() && moId.equals(j.get().getModuleOrganiserId())) {
                return true;
            }
        }
        return false;
    }

    public boolean canApplicantContactMo(String applicantId, String moId, String jobId) throws IOException {
        if (applicantId == null || moId == null) {
            return false;
        }
        if (friendService.isFriend(applicantId, moId)) {
            if (jobId == null || jobId.isEmpty()) {
                return true;
            }
            Optional<Job> j = jobService.findById(jobId);
            return j.isPresent() && moId.equals(j.get().getModuleOrganiserId());
        }
        if (!hasNonCancelledApplicationToMo(applicantId, moId)) {
            return false;
        }
        if (jobId == null || jobId.isEmpty()) {
            return true;
        }
        Optional<Job> j = jobService.findById(jobId);
        return j.isPresent() && moId.equals(j.get().getModuleOrganiserId())
                && (Job.STATUS_OPEN.equals(j.get().getStatus()) || hasNonCancelledApplicationForJob(applicantId, jobId));
    }

    private boolean hasNonCancelledApplicationForJob(String applicantId, String jobId) throws IOException {
        return applicationService.findByApplicantAndJob(applicantId, jobId)
                .filter(a -> !Application.STATUS_CANCELLED.equals(a.getStatus()))
                .isPresent();
    }

    /**
     * 招聘者是否可与该应聘者私信：互为好友，或对方曾投递过该 MO 名下岗位且申请未撤销。
     */
    public boolean canMoContactApplicant(String moId, String applicantId) throws IOException {
        if (moId == null || applicantId == null) {
            return false;
        }
        if (friendService.isFriend(applicantId, moId)) {
            return true;
        }
        return hasNonCancelledApplicationToMo(applicantId, moId);
    }

    public OptionalLong lastMessageTime(String applicantId, String moId) throws IOException {
        return Storage.loadMessages().stream()
                .filter(m -> applicantId.equals(m.getApplicantId()) && moId.equals(m.getModuleOrganiserId()))
                .mapToLong(DirectMessage::getSentAt)
                .max();
    }

    /** 该应聘者在私信中出现过的招聘者（含仅有对方回复的会话） */
    public Set<String> moIdsWithAnyMessage(String applicantId) throws IOException {
        return Storage.loadMessages().stream()
                .filter(m -> applicantId.equals(m.getApplicantId()))
                .map(DirectMessage::getModuleOrganiserId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public List<ApplicantMoPair> listThreadsForMo(String moId) throws IOException {
        Set<String> applicantIds = new LinkedHashSet<>();
        for (Application a : applicationService.findAll()) {
            if (Application.STATUS_CANCELLED.equals(a.getStatus())) {
                continue;
            }
            Optional<Job> j = jobService.findById(a.getJobId());
            if (j.isPresent() && moId.equals(j.get().getModuleOrganiserId())) {
                applicantIds.add(a.getApplicantId());
            }
        }
        for (String aid : friendService.friendApplicantIdsForMo(moId)) {
            applicantIds.add(aid);
        }
        List<ApplicantMoPair> list = new ArrayList<>();
        for (String aid : applicantIds) {
            list.add(new ApplicantMoPair(aid, moId));
        }
        return list;
    }

    public DirectMessage sendFromApplicant(String applicantId, String moId, String body, String jobId) throws IOException {
        if (!canApplicantContactMo(applicantId, moId, jobId)) {
            return null;
        }
        String trimmed = normalizeBody(body);
        if (trimmed == null) {
            return null;
        }
        DirectMessage dm = new DirectMessage(UUID.randomUUID().toString(), applicantId, moId,
                DirectMessage.SENDER_TA, trimmed, System.currentTimeMillis());
        if (jobId != null && !jobId.isEmpty()) {
            dm.setJobId(jobId);
        }
        List<DirectMessage> list = Storage.loadMessages();
        list.add(dm);
        Storage.saveMessages(list);
        return dm;
    }

    public DirectMessage sendFromMo(String moId, String applicantId, String body) throws IOException {
        if (!canMoContactApplicant(moId, applicantId)) {
            return null;
        }
        String trimmed = normalizeBody(body);
        if (trimmed == null) {
            return null;
        }
        DirectMessage dm = new DirectMessage(UUID.randomUUID().toString(), applicantId, moId,
                DirectMessage.SENDER_MO, trimmed, System.currentTimeMillis());
        List<DirectMessage> list = Storage.loadMessages();
        list.add(dm);
        Storage.saveMessages(list);
        return dm;
    }

    private static String normalizeBody(String body) {
        if (body == null) {
            return null;
        }
        String t = body.trim();
        if (t.isEmpty()) {
            return null;
        }
        if (t.length() > MAX_BODY_LEN) {
            t = t.substring(0, MAX_BODY_LEN);
        }
        return t;
    }

    public static String lastPreview(String body, int maxLen) {
        if (body == null) {
            return "";
        }
        String t = body.replace('\n', ' ').trim();
        if (t.length() <= maxLen) {
            return t;
        }
        return t.substring(0, maxLen) + "…";
    }

    public static class ApplicantMoPair {
        public final String applicantId;
        public final String moduleOrganiserId;

        public ApplicantMoPair(String applicantId, String moduleOrganiserId) {
            this.applicantId = applicantId;
            this.moduleOrganiserId = moduleOrganiserId;
        }
    }

    // ---------- 已读 / 未读（dm_read_states.json）----------

    private static DmReadState findOrCreateState(List<DmReadState> list, String applicantId, String moId) {
        for (DmReadState s : list) {
            if (applicantId.equals(s.getApplicantId()) && moId.equals(s.getModuleOrganiserId())) {
                return s;
            }
        }
        DmReadState n = new DmReadState();
        n.setApplicantId(applicantId);
        n.setModuleOrganiserId(moId);
        list.add(n);
        return n;
    }

    public void markConversationReadByTa(String applicantId, String moId) throws IOException {
        if (applicantId == null || moId == null) {
            return;
        }
        List<DirectMessage> conv = findConversation(applicantId, moId);
        long t = conv.isEmpty() ? System.currentTimeMillis() : conv.get(conv.size() - 1).getSentAt();
        List<DmReadState> list = Storage.loadDmReadStates();
        DmReadState s = findOrCreateState(list, applicantId, moId);
        if (t >= s.getTaLastReadAt()) {
            s.setTaLastReadAt(t);
        }
        Storage.saveDmReadStates(list);
    }

    public void markConversationReadByMo(String applicantId, String moId) throws IOException {
        if (applicantId == null || moId == null) {
            return;
        }
        List<DirectMessage> conv = findConversation(applicantId, moId);
        long t = conv.isEmpty() ? System.currentTimeMillis() : conv.get(conv.size() - 1).getSentAt();
        List<DmReadState> list = Storage.loadDmReadStates();
        DmReadState s = findOrCreateState(list, applicantId, moId);
        if (t >= s.getMoLastReadAt()) {
            s.setMoLastReadAt(t);
        }
        Storage.saveDmReadStates(list);
    }

    private long getTaReadAt(String applicantId, String moId) throws IOException {
        for (DmReadState s : Storage.loadDmReadStates()) {
            if (applicantId.equals(s.getApplicantId()) && moId.equals(s.getModuleOrganiserId())) {
                return s.getTaLastReadAt();
            }
        }
        return 0L;
    }

    private long getMoReadAt(String applicantId, String moId) throws IOException {
        for (DmReadState s : Storage.loadDmReadStates()) {
            if (applicantId.equals(s.getApplicantId()) && moId.equals(s.getModuleOrganiserId())) {
                return s.getMoLastReadAt();
            }
        }
        return 0L;
    }

    /** 招聘者发给应聘者的、尚未被应聘者读到的条数 */
    public int countUnreadForTa(String applicantId, String moId) throws IOException {
        long read = getTaReadAt(applicantId, moId);
        return (int) Storage.loadMessages().stream()
                .filter(m -> applicantId.equals(m.getApplicantId()) && moId.equals(m.getModuleOrganiserId()))
                .filter(m -> DirectMessage.SENDER_MO.equals(m.getSenderRole()) && m.getSentAt() > read)
                .count();
    }

    /** 应聘者发给招聘者的、尚未被招聘者读到的条数 */
    public int countUnreadForMo(String applicantId, String moId) throws IOException {
        long read = getMoReadAt(applicantId, moId);
        return (int) Storage.loadMessages().stream()
                .filter(m -> applicantId.equals(m.getApplicantId()) && moId.equals(m.getModuleOrganiserId()))
                .filter(m -> DirectMessage.SENDER_TA.equals(m.getSenderRole()) && m.getSentAt() > read)
                .count();
    }

    public int totalUnreadForApplicant(String applicantId) throws IOException {
        int n = 0;
        for (String moId : contactableMoIdsForApplicant(applicantId)) {
            n += countUnreadForTa(applicantId, moId);
        }
        return n;
    }

    public int totalUnreadForMo(String moId) throws IOException {
        int n = 0;
        for (ApplicantMoPair p : listThreadsForMo(moId)) {
            n += countUnreadForMo(p.applicantId, moId);
        }
        return n;
    }
}
