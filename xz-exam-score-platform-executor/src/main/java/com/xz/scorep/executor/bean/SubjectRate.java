package com.xz.scorep.executor.bean;

import java.io.Serializable;

/**
 * @author by fengye on 2017/5/15.
 */
public class SubjectRate implements Serializable {
    private String rangeId;
    private String rangeType;
    private double subjectRate;

    public String getRangeId() {
        return rangeId;
    }

    public void setRangeId(String rangeId) {
        this.rangeId = rangeId;
    }

    public String getRangeType() {
        return rangeType;
    }

    public void setRangeType(String rangType) {
        this.rangeType = rangType;
    }

    public double getSubjectRate() {
        return subjectRate;
    }

    public void setSubjectRate(double subjectRate) {
        this.subjectRate = subjectRate;
    }

    public SubjectRate(String rangeId, String rangType, double subjectRate) {
        this.rangeId = rangeId;
        this.rangeType = rangType;
        this.subjectRate = subjectRate;
    }

    @Override
    public String toString() {
        return "SubjectRate{" +
                "rangeId='" + rangeId + '\'' +
                ", rangeType='" + rangeType + '\'' +
                ", subjectRate='" + subjectRate + '\'' +
                '}';
    }
}
