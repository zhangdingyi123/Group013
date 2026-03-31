package com.bupt.ta.service;

import com.bupt.ta.model.ForumReply;
import com.bupt.ta.model.ForumThread;
import com.bupt.ta.storage.Storage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ForumService {
    public static final int MAX_TITLE_LEN = 200;
    public static final int MAX_BODY_LEN = 8000;

    public List<ForumThread> listThreadsSorted() throws IOException {
        return Storage.loadForumThreads().stream()
                .sorted(Comparator.comparingLong((ForumThread t) -> {
                    long lr = t.getLastReplyAt() > 0 ? t.getLastReplyAt() : t.getCreatedAt();
                    return lr;
                }).reversed())
                .collect(Collectors.toList());
    }

    public Optional<ForumThread> findThreadById(String id) throws IOException {
        return Storage.loadForumThreads().stream().filter(x -> id.equals(x.getId())).findFirst();
    }

    public List<ForumReply> findRepliesForThread(String threadId) throws IOException {
        return Storage.loadForumReplies().stream()
                .filter(r -> threadId.equals(r.getThreadId()))
                .sorted(Comparator.comparingLong(ForumReply::getCreatedAt))
                .collect(Collectors.toList());
    }

    public ForumThread createThread(String authorId, String authorRole, String authorName, String title, String body)
            throws IOException {
        String t = normalizeTitle(title);
        String b = normalizeBody(body);
        if (t == null || b == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        ForumThread thread = new ForumThread();
        thread.setId(UUID.randomUUID().toString());
        thread.setTitle(t);
        thread.setBody(b);
        thread.setAuthorId(authorId);
        thread.setAuthorRole(authorRole);
        thread.setAuthorName(authorName != null ? authorName : "");
        thread.setCreatedAt(now);
        thread.setLastReplyAt(now);
        thread.setReplyCount(0);
        List<ForumThread> list = Storage.loadForumThreads();
        list.add(thread);
        Storage.saveForumThreads(list);
        return thread;
    }

    public ForumReply addReply(String threadId, String authorId, String authorRole, String authorName, String body)
            throws IOException {
        Optional<ForumThread> opt = findThreadById(threadId);
        if (opt.isEmpty()) {
            return null;
        }
        String b = normalizeBody(body);
        if (b == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        ForumReply reply = new ForumReply();
        reply.setId(UUID.randomUUID().toString());
        reply.setThreadId(threadId);
        reply.setAuthorId(authorId);
        reply.setAuthorRole(authorRole);
        reply.setAuthorName(authorName != null ? authorName : "");
        reply.setBody(b);
        reply.setCreatedAt(now);
        List<ForumReply> replies = Storage.loadForumReplies();
        replies.add(reply);
        Storage.saveForumReplies(replies);

        List<ForumThread> threads = Storage.loadForumThreads();
        for (ForumThread t : threads) {
            if (threadId.equals(t.getId())) {
                t.setLastReplyAt(now);
                t.setReplyCount(t.getReplyCount() + 1);
                break;
            }
        }
        Storage.saveForumThreads(threads);
        return reply;
    }

    private static String normalizeTitle(String title) {
        if (title == null) {
            return null;
        }
        String s = title.trim();
        if (s.isEmpty()) {
            return null;
        }
        return s.length() > MAX_TITLE_LEN ? s.substring(0, MAX_TITLE_LEN) : s;
    }

    private static String normalizeBody(String body) {
        if (body == null) {
            return null;
        }
        String s = body.trim();
        if (s.isEmpty()) {
            return null;
        }
        return s.length() > MAX_BODY_LEN ? s.substring(0, MAX_BODY_LEN) : s;
    }
}
