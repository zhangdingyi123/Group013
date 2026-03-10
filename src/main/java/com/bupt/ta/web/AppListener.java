package com.bupt.ta.web;

import com.bupt.ta.storage.Storage;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;

/**
 * 应用启动时设置数据目录为 {context}/data，确保与部署路径一致。
 */
@WebListener
public class AppListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String base = sce.getServletContext().getRealPath("/");
        if (base != null) {
            File dataDir = new File(base, "data");
            Storage.setDataDir(dataDir.getAbsolutePath());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
