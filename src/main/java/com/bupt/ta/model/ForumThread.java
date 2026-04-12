package com.bupt.ta.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 论坛主题帖（楼主首帖）实体类。
 *
 * <p>所有角色（ta、mo、admin）均可发帖和浏览。
 * 每个主题帖下可有多条 {@link ForumReply} 回帖。</p>
 *
 * <p>{@link #replyCount} 和 {@link #lastReplyAt} 为冗余统计字段，
 * 在新增回帖时同步更新，用于列表页排序和展示。</p>
 *
 * <p>对应持久化文件：{@code data/forum_threads.json}</p>
 *
 * @author handmanhsker
 * @see ForumReply
 * @see com.bupt.ta.storage.Storage#loadForumThreads()
 * @see com.bupt.ta.storage.Storage#saveForumThreads(java.util.List)
 */
public class ForumThread {

    /** 唯一标识，UUID v4 格式 */
    private String id;

    /** 帖子标题 */
    private String title;

    /** 帖子正文内容 */
    private String body;

    /** 作者用户 ID */
    private String authorId;

    /** 作者角色：ta / mo / admin */
    private String authorRole;

    /** 作者显示名称（冗余存储） */
    private String authorName;

    /** 发帖时间（Unix 毫秒时间戳） */
    private long createdAt;

    /** 最后回复时间（Unix 毫秒时间戳），无回复时等于 createdAt */
    private long lastReplyAt;

    /** 回帖总数（冗余统计，新增回帖时 +1） */
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
