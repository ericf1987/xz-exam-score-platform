package com.xz.scorep.executor.bean;

/**
 * 超均率
 * @author by fengye on 2017/5/16.
 */
public class OverAverage {
    private String rangeId;
    private String rangeType;
    private String targetId;
    private String targetType;
    private double overAverage;

    public String getRangeId() {
        return rangeId;
    }

    public void setRangeId(String rangeId) {
        this.rangeId = rangeId;
    }

    public String getRangeType() {
        return rangeType;
    }

    public void setRangeType(String rangeType) {
        this.rangeType = rangeType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public double getOverAverage() {
        return overAverage;
    }

    public void setOverAverage(double overAverage) {
        this.overAverage = overAverage;
    }

    public OverAverage(String rangeId, String rangeType, String targetId, String targetType, double overAverage) {
        this.rangeId = rangeId;
        this.rangeType = rangeType;
        this.targetId = targetId;
        this.targetType = targetType;
        this.overAverage = overAverage;
    }

    public OverAverage() {
    }

    @Override
    public String toString() {
        return "OverAverage{" +
                "rangeId='" + rangeId + '\'' +
                ", rangeType='" + rangeType + '\'' +
                ", targetId='" + targetId + '\'' +
                ", targetType='" + targetType + '\'' +
                ", overAverage=" + overAverage +
                '}';
    }
}
