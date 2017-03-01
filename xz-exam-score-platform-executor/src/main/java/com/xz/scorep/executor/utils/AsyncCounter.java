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

    private final int total;

    private final AtomicInteger atomicInteger;

    private final String tag;

    public AsyncCounter(int total, String tag) {
        this.total = total;
        this.atomicInteger = new AtomicInteger(0);
        this.tag = tag;
    }

    public synchronized void count() {
        int value = atomicInteger.incrementAndGet();
        LOG.info(tag + "已完成 " + value + "/" + total);

        if (value >= total) {
            LOG.info(tag + "全部完成。");
        }
    }
}
