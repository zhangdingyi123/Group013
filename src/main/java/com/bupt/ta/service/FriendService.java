package com.bupt.ta.service;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.FriendLink;
import com.bupt.ta.model.FriendRequest;
import com.bupt.ta.model.Job;
import com.bupt.ta.storage.Storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class FriendService {
    private final ApplicationService applicationService = new ApplicationService();
    private final JobService jobService = new JobService();

    public boolean isFriend(String applicantId, String moId) throws IOException {
        if (applicantId == null || moId == null) {
            return false;
        }
        for (FriendLink l : Storage.loadFriendLinks()) {
            if (applicantId.equals(l.getApplicantId()) && moId.equals(l.getModuleOrganiserId())) {
                return true;
            }
        }
        return false;
    }

    private Optional<FriendRequest> findPending(String applicantId, String moId) throws IOException {
        for (FriendRequest r : Storage.loadFriendRequests()) {
            if (!FriendRequest.STATUS_PENDING.equals(r.getStatus())) {
                continue;
            }
            if (applicantId.equals(r.getApplicantId()) && moId.equals(r.getModuleOrganiserId())) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    /**
     * 是否曾对应该 MO 名下的岗位产生过申请（含已撤销、已拒绝等）。
     */
    public boolean hadAnyApplicationToMoJobs(String applicantId, String moId) throws IOException {
        for (Application a : applicationService.findByApplicantId(applicantId)) {
            Optional<Job> j = jobService.findById(a.getJobId());
            if (j.isPresent() && moId.equals(j.get().getModuleOrganiserId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 应聘者向招聘者发起好友请求（对方同意后成为好友）。不能与已投递未撤销的关系重复。
     */
    public boolean requestFromTa(String applicantId, String moId) throws IOException {
        if (applicantId == null || moId == null || moId.isEmpty()) {
            return false;
        }
        if (isFriend(applicantId, moId)) {
            return false;
        }
        if (findPending(applicantId, moId).isPresent()) {
            return false;
        }
        if (hasNonCancelledApplicationToMo(applicantId, moId)) {
            return false;
        }
        List<FriendRequest> list = Storage.loadFriendRequests();
        FriendRequest r = new FriendRequest();
        r.setId(UUID.randomUUID().toString());
        r.setApplicantId(applicantId);
        r.setModuleOrganiserId(moId);
        r.setFromRole(FriendRequest.FROM_TA);
        r.setStatus(FriendRequest.STATUS_PENDING);
        r.setCreatedAt(System.currentTimeMillis());
        list.add(r);
        Storage.saveFriendRequests(list);
        return true;
    }

    /**
     * 招聘者向应聘者发起好友请求：仅当对方曾申请过该 MO 名下岗位（含已撤销）。
     */
    public boolean requestFromMo(String moId, String applicantId) throws IOException {
        if (moId == null || applicantId == null) {
            return false;
        }
        if (isFriend(applicantId, moId)) {
            return false;
        }
        if (!hadAnyApplicationToMoJobs(applicantId, moId)) {
            return false;
        }
        if (findPending(applicantId, moId).isPresent()) {
            return false;
        }
        if (hasNonCancelledApplicationToMo(applicantId, moId)) {
            return false;
        }
        List<FriendRequest> list = Storage.loadFriendRequests();
        FriendRequest r = new FriendRequest();
        r.setId(UUID.randomUUID().toString());
        r.setApplicantId(applicantId);
        r.setModuleOrganiserId(moId);
        r.setFromRole(FriendRequest.FROM_MO);
        r.setStatus(FriendRequest.STATUS_PENDING);
        r.setCreatedAt(System.currentTimeMillis());
        list.add(r);
        Storage.saveFriendRequests(list);
        return true;
    }

    private boolean hasNonCancelledApplicationToMo(String applicantId, String moId) throws IOException {
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

    public boolean acceptRequestAsMo(String requestId, String moId) throws IOException {
        if (requestId == null || moId == null) {
            return false;
        }
        List<FriendRequest> list = Storage.loadFriendRequests();
        for (FriendRequest r : list) {
            if (!requestId.equals(r.getId()) || !FriendRequest.STATUS_PENDING.equals(r.getStatus())) {
                continue;
            }
            if (!FriendRequest.FROM_TA.equals(r.getFromRole())) {
                continue;
            }
            if (!moId.equals(r.getModuleOrganiserId())) {
                continue;
            }
            r.setStatus(FriendRequest.STATUS_ACCEPTED);
            Storage.saveFriendRequests(list);
            addFriendLink(r.getApplicantId(), moId);
            return true;
        }
        return false;
    }

    public boolean acceptRequestAsTa(String requestId, String applicantId) throws IOException {
        if (requestId == null || applicantId == null) {
            return false;
        }
        List<FriendRequest> list = Storage.loadFriendRequests();
        for (FriendRequest r : list) {
            if (!requestId.equals(r.getId()) || !FriendRequest.STATUS_PENDING.equals(r.getStatus())) {
                continue;
            }
            if (!FriendRequest.FROM_MO.equals(r.getFromRole())) {
                continue;
            }
            if (!applicantId.equals(r.getApplicantId())) {
                continue;
            }
            r.setStatus(FriendRequest.STATUS_ACCEPTED);
            Storage.saveFriendRequests(list);
            addFriendLink(applicantId, r.getModuleOrganiserId());
            return true;
        }
        return false;
    }

    private void addFriendLink(String applicantId, String moId) throws IOException {
        List<FriendLink> links = Storage.loadFriendLinks();
        for (FriendLink l : links) {
            if (applicantId.equals(l.getApplicantId()) && moId.equals(l.getModuleOrganiserId())) {
                return;
            }
        }
        links.add(new FriendLink(applicantId, moId, System.currentTimeMillis()));
        Storage.saveFriendLinks(links);
    }

    public List<FriendRequest> listPendingFromTaToMo(String moId) throws IOException {
        List<FriendRequest> out = new ArrayList<>();
        for (FriendRequest r : Storage.loadFriendRequests()) {
            if (!FriendRequest.STATUS_PENDING.equals(r.getStatus())) {
                continue;
            }
            if (FriendRequest.FROM_TA.equals(r.getFromRole()) && moId.equals(r.getModuleOrganiserId())) {
                out.add(r);
            }
        }
        return out;
    }

    public List<FriendRequest> listPendingFromMoToApplicant(String applicantId) throws IOException {
        List<FriendRequest> out = new ArrayList<>();
        for (FriendRequest r : Storage.loadFriendRequests()) {
            if (!FriendRequest.STATUS_PENDING.equals(r.getStatus())) {
                continue;
            }
            if (FriendRequest.FROM_MO.equals(r.getFromRole()) && applicantId.equals(r.getApplicantId())) {
                out.add(r);
            }
        }
        return out;
    }

    public List<String> friendMoIdsForApplicant(String applicantId) throws IOException {
        return Storage.loadFriendLinks().stream()
                .filter(l -> applicantId.equals(l.getApplicantId()))
                .map(FriendLink::getModuleOrganiserId)
                .collect(Collectors.toList());
    }

    public List<String> friendApplicantIdsForMo(String moId) throws IOException {
        return Storage.loadFriendLinks().stream()
                .filter(l -> moId.equals(l.getModuleOrganiserId()))
                .map(FriendLink::getApplicantId)
                .collect(Collectors.toList());
    }
}
