package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;

import java.io.Serializable;

/**
 * 得分率
 * @author by fengye on 2017/7/21.
 */
public class ScoreRate extends MongoAggrObject implements Serializable{

    private Range range;

    private Target target;

    private double scoreRate;

    private String scoreLevel;

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public double getScoreRate() {
        return scoreRate;
    }

    public void setScoreRate(double scoreRate) {
        this.scoreRate = scoreRate;
    }

    public String getScoreLevel() {
        return scoreLevel;
    }

    public void setScoreLevel(String scoreLevel) {
        this.scoreLevel = scoreLevel;
    }

    public ScoreRate() {
    }

    public ScoreRate(Range range, Target target, double scoreRate, String scoreLevel) {
        this.range = range;
        this.target = target;
        this.scoreRate = scoreRate;
        this.scoreLevel = scoreLevel;
    }

    @Override
    public String toString() {
        return "ScoreRate{" +
                "range=" + range +
                ", target=" + target +
                ", scoreRate=" + scoreRate +
                ", scoreLevel='" + scoreLevel + '\'' +
                '}';
    }
}
