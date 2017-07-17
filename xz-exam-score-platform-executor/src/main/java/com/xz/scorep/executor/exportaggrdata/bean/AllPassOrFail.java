package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Range;

import java.io.Serializable;

/**
 * 全科及格/不及格率
 * @author by fengye on 2017/7/17.
 */
public class AllPassOrFail extends MongoAggrObject implements Serializable{
    private Range range;

    private int allPassCount;

    private double allPassRate;

    private int allFailCount;

    private double allFailRate;

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public int getAllPassCount() {
        return allPassCount;
    }

    public void setAllPassCount(int allPassCount) {
        this.allPassCount = allPassCount;
    }

    public double getAllPassRate() {
        return allPassRate;
    }

    public void setAllPassRate(double allPassRate) {
        this.allPassRate = allPassRate;
    }

    public int getAllFailCount() {
        return allFailCount;
    }

    public void setAllFailCount(int allFailCount) {
        this.allFailCount = allFailCount;
    }

    public double getAllFailRate() {
        return allFailRate;
    }

    public void setAllFailRate(double allFailRate) {
        this.allFailRate = allFailRate;
    }

    public AllPassOrFail() {
    }

    public AllPassOrFail(Range range, int allPassCount, double allPassRate, int allFailCount, double allFailRate) {
        this.range = range;
        this.allPassCount = allPassCount;
        this.allPassRate = allPassRate;
        this.allFailCount = allFailCount;
        this.allFailRate = allFailRate;
    }

    @Override
    public String toString() {
        return "AllPassOrFail{" +
                "range=" + range +
                ", allPassCount=" + allPassCount +
                ", allPassRate=" + allPassRate +
                ", allFailCount=" + allFailCount +
                ", allFailRate=" + allFailRate +
                '}';
    }
}
