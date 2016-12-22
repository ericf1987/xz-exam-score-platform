package com.xz.scorep.executor.utils;

import java.util.UUID;

/**
 * (description)
 * created at 2016/12/16
 *
 * @author yidin
 */
public class UuidUtils {

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "_");
    }
}
