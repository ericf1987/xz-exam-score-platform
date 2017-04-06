package com.xz.scorep.manager;

/**
 * (description)
 * created at 2017/4/6
 *
 * @author yidin
 */
public class BaseTest {

    static {
        setupProxy();
    }

    public static void setupProxy() {
        System.setProperty("unit_testing", "true");
        System.setProperty("socksProxyHost", "127.0.0.1");
        System.setProperty("socksProxyPort", "2346");
    }

}
