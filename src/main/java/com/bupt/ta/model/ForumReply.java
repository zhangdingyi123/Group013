package com.bupt.ta.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 论坛回帖
 */
public class ForumReply {
    private String id;
    private String threadId;
    private String authorId;
    private String authorRole;
    private String authorName;
    private String body;
    private long createdAt;

    public ForumReply() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getCreatedAtText() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(createdAt));
    }
}
