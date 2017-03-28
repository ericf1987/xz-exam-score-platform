package com.xz.scorep.executor.bean;

/**
 * @author yiding_he
 */
public enum ProjectStatus {

    /**
     * 尚未导入
     */
    NotImported,

    /**
     * 正在导入
     */
    Importing,

    /**
     * 空闲
     */
    Ready,

    /**
     * 正在统计
     */
    Aggregating,

    /**
     * 正在生成报表
     */
    GeneratingReport,

    /**
     * 正在打包
     */
    Archiving
}
