package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Range;

import java.io.Serializable;

/**
 * 全科及格/不及格率
 *
 * @author by fengye on 2017/7/17.
 */
public class AllPassOrFail extends MongoAggrObject implements Serializable {
    private Range range;

    private int allPassCount;

    private double allPassRate;

    private int allFailCount;

    private double allFailRate;

    private int allExcellentCount;

    private double allExcellentRate;

    private int allGoodCount;

    private double allGoodRate;

    public AllPassOrFail() {
    }

    public AllPassOrFail(Range range, int allPassCount, double allPassRate, int allFailCount, double allFailRate) {
        this.range = range;
        this.allPassCount = allPassCount;
        this.allPassRate = allPassRate;
        this.allFailCount = allFailCount;
        this.allFailRate = allFailRate;
    }

    public AllPassOrFail(Range range, int allPassCount, double allPassRate, int allFailCount, double allFailRate,
                         int allExcellentCount, double allExcellentRate, int allGoodCount, double allGoodRate) {
        this.range = range;
        this.allPassCount = allPassCount;
        this.allPassRate = allPassRate;
        this.allFailCount = allFailCount;
        this.allFailRate = allFailRate;
        this.allExcellentCount = allExcellentCount;
        this.allExcellentRate = allExcellentRate;
        this.allGoodCount = allGoodCount;
        this.allGoodRate = allGoodRate;
    }

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

    public int getAllExcellentCount() {
        return allExcellentCount;
    }

    public void setAllExcellentCount(int allExcellentCount) {
        this.allExcellentCount = allExcellentCount;
    }

    public double getAllExcellentRate() {
        return allExcellentRate;
    }

    public void setAllExcellentRate(double allExcellentRate) {
        this.allExcellentRate = allExcellentRate;
    }

    public int getAllGoodCount() {
        return allGoodCount;
    }

    public void setAllGoodCount(int allGoodCount) {
        this.allGoodCount = allGoodCount;
    }

    public double getAllGoodRate() {
        return allGoodRate;
    }

    public void setAllGoodRate(double allGoodRate) {
        this.allGoodRate = allGoodRate;
    }

    @Override
    public String toString() {
        return "AllPassOrFail{" +
                "range=" + range +
                ", allPassCount=" + allPassCount +
                ", allPassRate=" + allPassRate +
                ", allFailCount=" + allFailCount +
                ", allFailRate=" + allFailRate +
                ", allExcellentCount=" + allExcellentCount +
                ", allExcellentRate=" + allExcellentRate +
                ", allGoodCount=" + allGoodCount +
                ", allGoodRate=" + allGoodRate +
                '}';
    }
}
