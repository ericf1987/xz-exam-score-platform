package com.xz.scorep.executor.utils;

/**
 * (description)
 * created at 2017/4/7
 *
 * @author yidin
 */
public class SysUtils {

    public static boolean isUnitTesting() {
        return System.getProperty("unit_testing") != null;
    }
}
