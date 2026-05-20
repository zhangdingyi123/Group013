/**
 * 北京邮电大学国际学院助教招聘系统（BUPT TA Recruitment）。
 * <p>
 * 分层结构：
 * <ul>
 *   <li>{@link com.bupt.ta.model} — 领域实体（应聘者、岗位、申请等）</li>
 *   <li>{@link com.bupt.ta.storage} — JSON 文件持久化</li>
 *   <li>{@link com.bupt.ta.service} — 业务逻辑与技能匹配</li>
 *   <li>{@link com.bupt.ta.web} — Servlet、Filter、Listener</li>
 *   <li>{@link com.bupt.ta.util} — 密码、国际化、向量工具</li>
 * </ul>
 * 运行与配置说明见项目根目录 {@code README.md}；用户操作见 {@code docs/UserManual.md}。
 */
package com.bupt.ta;
