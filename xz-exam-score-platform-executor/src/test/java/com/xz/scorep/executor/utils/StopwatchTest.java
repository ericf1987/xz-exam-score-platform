package com.xz.scorep.executor.utils;

/**
 * (description)
 * created at 2017/2/20
 *
 * @author yidin
 */
public class StopwatchTest {

    public static void main(String[] args) throws Exception {
        Stopwatch stopwatch = Stopwatch.start();
        Thread.sleep(1234);
        System.out.println(stopwatch.tick("已执行了 {0} ms."));
        Thread.sleep(567);
        System.out.println(stopwatch.stop("完成，共执行了 {0} ms."));
    }
}