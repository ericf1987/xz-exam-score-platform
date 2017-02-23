package com.xz.scorep.executor.utils;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * (description)
 * created at 2017/2/23
 *
 * @author yidin
 */
public class LogUtils {

    private static Map<Logger, Level> originalLevel = new HashMap<>();

    public static void changeLogLevel(String loggerName, Level level) {
        changeLogLevel(LoggerFactory.getLogger(loggerName), level);
    }

    public static void changeLogLevel(Logger logger, Level level) {

        if (!(logger instanceof ch.qos.logback.classic.Logger)) {
            return;
        }

        ch.qos.logback.classic.Logger l = (ch.qos.logback.classic.Logger) logger;

        if (!originalLevel.containsKey(logger)) {
            originalLevel.put(logger, l.getLevel());
        }

        l.setLevel(level);
    }

    public static void restoreLogLevel(String loggerName) {
        restoreLogLevel(LoggerFactory.getLogger(loggerName));
    }

    public static void restoreLogLevel(Logger logger) {

        if (!(logger instanceof ch.qos.logback.classic.Logger)) {
            return;
        }

        if (originalLevel.containsKey(logger)) {
            ch.qos.logback.classic.Logger l = (ch.qos.logback.classic.Logger) logger;
            l.setLevel(originalLevel.get(logger));
        }
    }
}
