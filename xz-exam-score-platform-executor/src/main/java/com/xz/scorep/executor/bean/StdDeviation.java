package com.xz.scorep.executor.bean;

/**
 * 标准差
 * @author by fengye on 2017/5/16.
 */
public class StdDeviation {
    private String rangeId;
    private String rangeType;
    private String targetId;
    private String targetType;
    private double stdDeviation;

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

    public double getStdDeviation() {
        return stdDeviation;
    }

    public void setStdDeviation(double stdDeviation) {
        this.stdDeviation = stdDeviation;
    }

    public StdDeviation(String rangeId, String rangeType, String targetId, String targetType, double stdDeviation) {
        this.rangeId = rangeId;
        this.rangeType = rangeType;
        this.targetId = targetId;
        this.targetType = targetType;
        this.stdDeviation = stdDeviation;
    }

    public StdDeviation() {
    }

    @Override
    public String toString() {
        return "StdDeviation{" +
                "rangeId='" + rangeId + '\'' +
                ", rangeType='" + rangeType + '\'' +
                ", targetId='" + targetId + '\'' +
                ", targetType='" + targetType + '\'' +
                ", stdDeviation=" + stdDeviation +
                '}';
    }
}
