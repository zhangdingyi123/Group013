package com.bupt.ta.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 论坛主题帖（楼主首帖）
 */
public class ForumThread {
    private String id;
    private String title;
    private String body;
    private String authorId;
    /** ta | mo | admin */
    private String authorRole;
    private String authorName;
    private long createdAt;
    /** 最后回复时间，与 createdAt 同步维护 */
    private long lastReplyAt;
    private int replyCount;

    public ForumThread() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getLastReplyAt() { return lastReplyAt; }
    public void setLastReplyAt(long lastReplyAt) { this.lastReplyAt = lastReplyAt; }
    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }

    public String getCreatedAtText() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(createdAt));
    }

    public String getLastReplyAtText() {
        long t = lastReplyAt > 0 ? lastReplyAt : createdAt;
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(t));
    }
}
