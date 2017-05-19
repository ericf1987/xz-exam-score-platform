package com.xz.scorep.executor.bean;

/**
 * @author luckylo
 */
public class AverageScore {

    private String rangeType;

    private String rangeId;

    private String targetId;

    private String targetType;

    private double averageScore;

    public AverageScore() {
    }

    public AverageScore(String rangeType, String rangeId, String targetId, String targetType, double averageScore) {
        this.rangeType = rangeType;
        this.rangeId = rangeId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.averageScore = averageScore;
    }

    public String getRangeType() {
        return rangeType;
    }

    public void setRangeType(String rangeType) {
        this.rangeType = rangeType;
    }

    public String getRangeId() {
        return rangeId;
    }

    public void setRangeId(String rangeId) {
        this.rangeId = rangeId;
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

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    @Override
    public String toString() {
        return "AverageScore{" +
                "rangeType='" + rangeType + '\'' +
                ", rangeId='" + rangeId + '\'' +
                ", targetId='" + targetId + '\'' +
                ", targetType='" + targetType + '\'' +
                ", averageScore=" + averageScore +
                '}';
    }

}
