package com.bupt.ta.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 论坛回帖实体类。
 *
 * <p>隶属于某个 {@link ForumThread}（主题帖），通过 {@link #threadId} 关联。
 * 支持多角色发帖：助教申请人（ta）、课程组织者（mo）、管理员（admin）。</p>
 *
 * <p>对应持久化文件：{@code data/forum_replies.json}</p>
 *
 * @author handmanhsker
 * @see ForumThread
 * @see com.bupt.ta.storage.Storage#loadForumReplies()
 * @see com.bupt.ta.storage.Storage#saveForumReplies(java.util.List)
 */
public class ForumReply {

    /** 唯一标识，UUID v4 格式 */
    private String id;

    /** 所属主题帖 ID，关联 {@link ForumThread#getId()} */
    private String threadId;

    /** 作者用户 ID */
    private String authorId;

    /** 作者角色：ta / mo / admin */
    private String authorRole;

    /** 作者显示名称（冗余存储，避免频繁关联查询） */
    private String authorName;

    /** 回帖正文内容 */
    private String body;

    /** 回帖时间（Unix 毫秒时间戳） */
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
