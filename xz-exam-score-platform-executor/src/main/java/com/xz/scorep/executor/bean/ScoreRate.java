package com.xz.scorep.executor.bean;

import java.io.Serializable;

/**
 * 科目/项目得分率
 *
 * @author by fengye on 2017/5/7.
 */
public class ScoreRate implements Serializable {
    private String rangeType;

    private String rangeId;

    private String targetType;

    private String targetId;

    private String scoreLevel;

    private double scoreRate;

    private String studentId;

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

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getScoreLevel() {
        return scoreLevel;
    }

    public void setScoreLevel(String scoreLevel) {
        this.scoreLevel = scoreLevel;
    }

    public double getScoreRate() {
        return scoreRate;
    }

    public void setScoreRate(double scoreRate) {
        this.scoreRate = scoreRate;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public ScoreRate(String studentId, String rangeType, String rangeId, String targetId, String targetType, String scoreLevel, double scoreRate) {
        this.studentId = studentId;
        this.rangeType = rangeType;
        this.rangeId = rangeId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.scoreLevel = scoreLevel;
        this.scoreRate = scoreRate;
    }

    public ScoreRate() {
    }

    @Override
    public String toString() {
        return "ScoreRate{" +
                "rangeType='" + rangeType + '\'' +
                ", rangeId='" + rangeId + '\'' +
                ", targetType='" + targetType + '\'' +
                ", targetId='" + targetId + '\'' +
                ", scoreLevel='" + scoreLevel + '\'' +
                ", scoreRate=" + scoreRate +
                ", studentId='" + studentId + '\'' +
                '}';
    }
}
