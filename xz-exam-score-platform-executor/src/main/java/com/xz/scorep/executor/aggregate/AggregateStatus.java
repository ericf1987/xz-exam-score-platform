package com.xz.scorep.executor.aggregate;

/**
 * (description)
 * created at 2017/2/26
 *
 * @author yidin
 */
public enum AggregateStatus {
    /**
     * 表示尚未开始统计
     */
    Idle,

    /**
     * 表示正在统计当中
     */
    Running,

    /**
     * 表示统计已结束
     */
    Finished
}
