package com.xz.scorep.executor.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * (description)
 * created at 2017/3/1
 *
 * @author yidin
 */
public class AsyncCounter {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncCounter.class);

    private final String tag;

    private final int total;

    private int interval;

    private final AtomicInteger atomicInteger;

    public AsyncCounter(String tag, int total) {
        this.total = total;
        this.tag = tag;
        this.atomicInteger = new AtomicInteger(0);
    }

    public AsyncCounter(String tag, int total, int interval) {
        this.tag = tag;
        this.total = total;
        this.interval = interval;
        this.atomicInteger = new AtomicInteger(0);
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getCurrentCount() {
        return this.atomicInteger.get();
    }

    public int getTotal() {
        return total;
    }

    public synchronized void count() {
        int value = atomicInteger.incrementAndGet();

        if (interval <= 1 || value % interval == 0) {
            LOG.info(tag + "已完成 " + value + "/" + total);
        }

        if (value >= total) {
            if (interval > 1) {
                LOG.info(tag + "已完成 " + value + "/" + total);
            }
            LOG.info(tag + "全部完成。");
        }
    }
}
