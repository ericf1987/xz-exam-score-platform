package com.xz.scorep.executor.api.context;

import org.springframework.context.ApplicationContext;

/**
 * spring 上下文
 *
 * @author zhaorenwu
 */
public class App {

    private static ApplicationContext applicationContext;

    public static boolean WEB_ENABLED = !"true".equals(System.getProperty("noweb"));

    public static void setApplicationContext(ApplicationContext applicationContext) {
        App.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> type) {
        return applicationContext.getBean(type);
    }

    public static <T> T getBean(Class<T> type, String name) {
        return applicationContext.getBean(name, type);
    }
}
