package com.bupt.ta.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        WebApp.init(sce.getServletContext());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
