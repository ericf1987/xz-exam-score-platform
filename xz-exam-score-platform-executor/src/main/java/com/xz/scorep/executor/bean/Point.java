package com.xz.scorep.executor.bean;

/**
 * @author by fengye on 2017/6/26.
 */
public class Point {
    private String pointId;
    private String pointName;
    private String parentPointId;
    private String subjectId;
    private double fullScore;

    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public String getParentPointId() {
        return parentPointId;
    }

    public void setParentPointId(String parentPointId) {
        this.parentPointId = parentPointId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public double getFullScore() {
        return fullScore;
    }

    public void setFullScore(double fullScore) {
        this.fullScore = fullScore;
    }

    public Point() {
    }

    public Point(String pointId, String pointName, String parentPointId, String subjectId, double fullScore) {
        this.pointId = pointId;
        this.pointName = pointName;
        this.parentPointId = parentPointId;
        this.subjectId = subjectId;
        this.fullScore = fullScore;
    }

    public Point(String pointId, String pointName, String parentPointId, String subjectId) {
        this.pointId = pointId;
        this.pointName = pointName;
        this.parentPointId = parentPointId;
        this.subjectId = subjectId;
    }

    @Override
    public String toString() {
        return "Point{" +
                "pointId='" + pointId + '\'' +
                ", pointName='" + pointName + '\'' +
                ", parentPointId='" + parentPointId + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", fullScore=" + fullScore +
                '}';
    }
}
