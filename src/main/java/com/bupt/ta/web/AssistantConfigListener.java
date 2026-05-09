package com.bupt.ta.web;

import com.bupt.ta.service.assistant.AssistantConfig;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;

/**
 * 启动时从 WAR 标准路径加载 {@code assistant.properties}，保证小助手能读到密钥（尤其是 IDE / exploded 部署）。
 */
@WebListener
public class AssistantConfigListener implements ServletContextListener {

    private static final String WAR_RESOURCE = "/WEB-INF/classes/assistant.properties";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try (InputStream in = sce.getServletContext().getResourceAsStream(WAR_RESOURCE)) {
            if (in != null) {
                AssistantConfig.primeFromWarClasspathResource(in);
            }
        } catch (IOException ignored) {
            // 留给 AssistantConfig 惰性 classpath / 文件路径加载
        }
    }
}
