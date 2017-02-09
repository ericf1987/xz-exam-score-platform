package com.xz.scorep.executor.utils;

import com.xz.ajiaedu.common.concurrent.Executors;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ThreadPools {

    /**
     * 创建一个临时的线程池，执行指定的操作，等待直到所有任务执行完毕，最后关闭线程池并返回
     *
     * @param poolSize          线程池大小
     * @param queueSize         任务队列大小，满了将会阻塞
     * @param afterPoolCreation 向线程池提交任务的操作
     *
     * @throws InterruptedException 如果等待线程池关闭出错
     */
    public static void createAndRunThreadPool(
            int poolSize, int queueSize, Consumer<ThreadPoolExecutor> afterPoolCreation) throws InterruptedException {

        ThreadPoolExecutor executor = Executors.newBlockingThreadPoolExecutor(poolSize, poolSize, queueSize);
        if (afterPoolCreation != null) {
            afterPoolCreation.accept(executor);
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);
    }
}
