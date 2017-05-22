package com.xz.scorep.executor.api.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * (描述)
 *
 * @author zhaorenwu
 */
public class ThrowableUtils {

    public static String toString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
