# 数据文件字典（Data Dictionary）

> 本目录下所有 `.json` 文件均为系统持久化数据，由 `Storage.java` 统一读写。
> JSON 不支持注释，故以本文件作为各数据文件的说明文档。

---

## 1. admins.json — 系统管理员

管理员可查看全局工作负荷、调配录用。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | UUID v4 唯一标识 |
| name | String | 管理员姓名 |
| email | String | 登录邮箱（即账号） |
| passwordHash | String | 密码 SHA-256 哈希（Base64） |
| createdAt | long | 注册时间（Unix 毫秒时间戳） |

---

## 2. applicants.json — 助教申请人（学生）

申请人可浏览岗位、投递申请、上传简历、与课程组织者私信。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | UUID v4 唯一标识 |
| name | String | 姓名 |
| email | String | 登录邮箱 |
| passwordHash | String | 密码哈希 |
| studentId | String | 学号 |
| phone | String | 联系电话 |
| skills | String[] | 技能标签列表，如 ["Java", "Python"]，用于岗位匹配 |
| resumePath | String | 简历文件名（相对 data/resumes/），如 "xxx.txt" |
| createdAt | long | 注册时间 |

---

## 3. module_organisers.json — 课程组织者（教师）

课程组织者可发布岗位、审阅申请、安排面试、与申请人私信。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | UUID v4 唯一标识 |
| name | String | 姓名 |
| email | String | 登录邮箱 |
| passwordHash | String | 密码哈希 |
| department | String | 所属院系/课程组 |
| createdAt | long | 注册时间 |

---

## 4. jobs.json — 招聘岗位

由课程组织者创建，申请人可浏览和投递。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | UUID v4 唯一标识 |
| title | String | 岗位名称，如「数据结构课程助教」 |
| moduleOrganiserId | String | 发布者 ID → module_organisers.json |
| description | String | 岗位详细描述（职责、课时等） |
| requiredSkills | String[] | 所需技能列表，用于与申请人技能匹配 |
| type | String | 岗位类型：`course_ta`(课程助教) / `invigilation`(监考) / `activity`(活动) |
| status | String | `open`(招聘中) / `closed`(已关闭) |
| createdAt | long | 创建时间 |

---

## 5. applications.json — 岗位申请记录

记录申请人对岗位的投递，包含完整的状态流转和面试信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | UUID v4 唯一标识 |
| applicantId | String | 申请人 ID → applicants.json |
| jobId | String | 岗位 ID → jobs.json |
| status | String | `pending`(待审核) → `interview`(待面试) → `accepted`(录用) / `rejected`(拒绝) / `cancelled`(取消) |
| note | String | 申请人备注/附言 |
| appliedAt | long | 投递时间 |
| interviewAt | long | 面试时间（0 表示未安排） |
| interviewDetail | String | 面试地点/线上链接说明 |
| interviewConfirmed | boolean | 旧版兼容字段 |
| interviewTaStatus | String | 申请人面试反馈：`pending` / `confirmed` / `declined` / `reschedule` |

**状态流转图：**
```
pending ──→ interview ──→ accepted
  │              │
  │              └──→ rejected
  └──→ cancelled
```

---

## 6. messages.json — 私信消息

申请人与课程组织者之间的一对一会话消息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | UUID v4 唯一标识 |
| applicantId | String | 申请人 ID → applicants.json |
| moduleOrganiserId | String | 课程组织者 ID → module_organisers.json |
| senderRole | String | 发送方：`ta`(申请人) / `mo`(组织者) |
| body | String | 消息正文 |
| sentAt | long | 发送时间 |
| jobId | String | 关联岗位 ID（可选，用于追溯消息来源） |

---

## 7. dm_read_states.json — 私信已读状态

记录每个会话中双方各自阅读到的时间点，用于计算未读消息数。

| 字段 | 类型 | 说明 |
|------|------|------|
| applicantId | String | 申请人 ID（会话标识之一） |
| moduleOrganiserId | String | 课程组织者 ID（会话标识之二） |
| taLastReadAt | long | 申请人已读至此时间戳（之前的对方消息均已读） |
| moLastReadAt | long | 组织者已读至此时间戳 |

**未读数计算：** 申请人未读 = messages 中 senderRole="mo" 且 sentAt > taLastReadAt 的条数

---

## 8. forum_threads.json — 论坛主题帖

所有角色（ta / mo / admin）均可发帖和浏览。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | UUID v4 唯一标识 |
| title | String | 帖子标题 |
| body | String | 帖子正文 |
| authorId | String | 作者用户 ID |
| authorRole | String | 作者角色：`ta` / `mo` / `admin` |
| authorName | String | 作者显示名称（冗余存储） |
| createdAt | long | 发帖时间 |
| lastReplyAt | long | 最后回复时间（用于排序） |
| replyCount | int | 回帖总数 |

---

## 9. forum_replies.json — 论坛回帖

隶属于某个主题帖，通过 threadId 关联。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | UUID v4 唯一标识 |
| threadId | String | 所属主题帖 ID → forum_threads.json |
| authorId | String | 作者用户 ID |
| authorRole | String | 作者角色：`ta` / `mo` / `admin` |
| authorName | String | 作者显示名称 |
| body | String | 回帖正文 |
| createdAt | long | 回帖时间 |

---

## 10. friend_links.json — 好友关系

申请人与课程组织者成为好友后可随时私信（不依赖岗位投递）。

| 字段 | 类型 | 说明 |
|------|------|------|
| applicantId | String | 申请人 ID → applicants.json |
| moduleOrganiserId | String | 课程组织者 ID → module_organisers.json |
| createdAt | long | 好友关系建立时间 |

---

## 11. friend_requests.json — 好友请求

任一方可发起好友请求，对方接受后自动创建 friend_links 记录。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | UUID v4 唯一标识 |
| applicantId | String | 申请人 ID → applicants.json |
| moduleOrganiserId | String | 课程组织者 ID → module_organisers.json |
| fromRole | String | 发起方：`ta`(申请人发起) / `mo`(组织者发起) |
| status | String | `pending`(待处理) / `accepted`(已接受) |
| createdAt | long | 请求创建时间 |

---

## 12. resumes/ 目录 — 简历文件

| 命名规则 | 说明 |
|----------|------|
| `{applicantId}.txt` | 纯文本简历（可直接读取展示） |
| `{applicantId}.pdf` | PDF 简历（通过 PDFBox 解析或下载） |
| `{applicantId}.docx` | Word 简历（通过 Apache POI 解析或下载） |

---

## 通用约定

| 约定 | 规则 |
|------|------|
| ID 格式 | UUID v4（xxxxxxxx-xxxx-4xxx-8xxx-xxxxxxxxxxxx） |
| 时间格式 | Unix 毫秒时间戳（如 1772323200000） |
| 文件编码 | UTF-8 |
| JSON 格式 | Gson Pretty Printing（带缩进） |
| 密码存储 | SHA-256 → Base64（示例中的测试密码均为 `123456`） |

---

*本文件由数据层负责人维护 —— handmanhsker*
