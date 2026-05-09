package com.bupt.ta.service;

import com.bupt.ta.model.ForumReply;
import com.bupt.ta.model.ForumThread;
import com.bupt.ta.storage.Storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ForumService {
    public static final int MAX_TITLE_LEN = 200;
    public static final int MAX_BODY_LEN = 8000;
    public static final int DEFAULT_PAGE_SIZE = 15;

    public static final class ForumIndexPage {
        public final List<ForumThread> threads;
        public final int total;
        public final int page;
        public final int pageSize;
        public final int totalPages;

        public ForumIndexPage(List<ForumThread> threads, int total, int page, int pageSize, int totalPages) {
            this.threads = threads;
            this.total = total;
            this.page = page;
            this.pageSize = pageSize;
            this.totalPages = totalPages;
        }
    }

    /**
     * 主题列表：可选标题关键词、排序、分页。
     *
     * @param sort "created" 按发帖时间；其它按最后回复时间
     */
    public ForumIndexPage listThreadsForIndex(String query, String sort, int page, int pageSize) throws IOException {
        if (pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        List<ForumThread> all = new ArrayList<>(Storage.loadForumThreads());
        String q = query == null ? "" : query.trim();
        if (!q.isEmpty()) {
            String lower = q.toLowerCase(Locale.ROOT);
            all = all.stream()
                    .filter(t -> t.getTitle() != null && t.getTitle().toLowerCase(Locale.ROOT).contains(lower))
                    .collect(Collectors.toList());
        }
        Comparator<ForumThread> cmp;
        if ("created".equals(sort)) {
            cmp = Comparator.comparingLong(ForumThread::getCreatedAt).reversed();
        } else {
            cmp = Comparator.comparingLong((ForumThread t) -> {
                long lr = t.getLastReplyAt() > 0 ? t.getLastReplyAt() : t.getCreatedAt();
                return lr;
            }).reversed();
        }
        all.sort(cmp);
        int total = all.size();
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / pageSize);
        if (page < 1) {
            page = 1;
        }
        if (page > totalPages) {
            page = totalPages;
        }
        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<ForumThread> slice = from >= total || from < 0
                ? new ArrayList<>()
                : new ArrayList<>(all.subList(from, to));
        return new ForumIndexPage(slice, total, page, pageSize, totalPages);
    }

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
