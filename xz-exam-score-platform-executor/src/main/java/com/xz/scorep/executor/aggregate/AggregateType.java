package com.xz.scorep.executor.aggregate;

/**
 * 统计类型
 *
 * @author yidin
 */
public enum AggregateType {

    /**
     * 用于监控平台低分检查
     * 只统计主客观题得分,科目得分,以及总分(不根据报表配置剔除0分,缺考等)
     */
    Check,

    /**
     * 包含Check统计(防止监控平台不做低分检查)
     * 用于网阅平台页面展示的快速统计,先根据报表配置剔除0分,缺考,作弊等,然后统计四率,平均分
     */
    Quick,

    /**
     * 仅执行基础统计,用于监控平台生成Excel(执行Basic统计之前,必须执行Quick统计)
     */
    Basic,

    /**
     * 仅执行高级统计
     */
    Advanced,

    /**
     * 执行完整统计,包括基础统计和高级统计
     */
    Complete
}
