package com.xz.scorep.executor.utils;

import java.text.MessageFormat;

/**
 * (description)
 * created at 2017/2/20
 *
 * @author yidin
 */
public class Stopwatch {

    public static Stopwatch start() {
        return new Stopwatch(System.currentTimeMillis());
    }

    private Stopwatch(long start) {
        this.start = start;
    }

    private long start = -1;

    private long end = -1;

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getTimeMillis() {
        if (end == -1) {
            return System.currentTimeMillis() - start;
        } else {
            return end - start;
        }
    }

    public String tick(String pattern) {
        return MessageFormat.format(pattern, getTimeMillis());
    }

    public String stop(String pattern) {
        this.end = System.currentTimeMillis();
        return tick(pattern);
    }
}
