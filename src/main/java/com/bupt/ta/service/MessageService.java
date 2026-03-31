package com.bupt.ta.service;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.DirectMessage;
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

    public List<DirectMessage> findConversation(String applicantId, String moduleOrganiserId) throws IOException {
        return Storage.loadMessages().stream()
                .filter(m -> applicantId.equals(m.getApplicantId()) && moduleOrganiserId.equals(m.getModuleOrganiserId()))
                .sorted(Comparator.comparingLong(DirectMessage::getSentAt))
                .collect(Collectors.toList());
    }

    /**
     * 应聘者可联系的招聘者 ID：对开放岗位发布者，或对其岗位有过未取消申请的招聘者。
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
        for (Job j : jobService.findOpen()) {
            ids.add(j.getModuleOrganiserId());
        }
        return ids;
    }

    public boolean canApplicantContactMo(String applicantId, String moId, String jobId) throws IOException {
        if (applicantId == null || moId == null) {
            return false;
        }
        if (contactableMoIdsForApplicant(applicantId).contains(moId)) {
            if (jobId == null || jobId.isEmpty()) {
                return true;
            }
            Optional<Job> j = jobService.findById(jobId);
            return j.isPresent() && moId.equals(j.get().getModuleOrganiserId())
                    && (Job.STATUS_OPEN.equals(j.get().getStatus()) || hasNonCancelledApplicationForJob(applicantId, jobId));
        }
        return false;
    }

    private boolean hasNonCancelledApplicationForJob(String applicantId, String jobId) throws IOException {
        return applicationService.findByApplicantAndJob(applicantId, jobId)
                .filter(a -> !Application.STATUS_CANCELLED.equals(a.getStatus()))
                .isPresent();
    }

    /**
     * 招聘者是否可与该应聘者在本线程内通信：已有消息，或该应聘者申请过该 MO 名下岗位。
     */
    public boolean canMoContactApplicant(String moId, String applicantId) throws IOException {
        if (moId == null || applicantId == null) {
            return false;
        }
        if (hasAnyMessageInThread(applicantId, moId)) {
            return true;
        }
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

    private boolean hasAnyMessageInThread(String applicantId, String moId) throws IOException {
        return Storage.loadMessages().stream()
                .anyMatch(m -> applicantId.equals(m.getApplicantId()) && moId.equals(m.getModuleOrganiserId()));
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
        for (DirectMessage m : Storage.loadMessages()) {
            if (moId.equals(m.getModuleOrganiserId())) {
                applicantIds.add(m.getApplicantId());
            }
        }
        for (Application a : applicationService.findAll()) {
            if (Application.STATUS_CANCELLED.equals(a.getStatus())) {
                continue;
            }
            Optional<Job> j = jobService.findById(a.getJobId());
            if (j.isPresent() && moId.equals(j.get().getModuleOrganiserId())) {
                applicantIds.add(a.getApplicantId());
            }
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
}
