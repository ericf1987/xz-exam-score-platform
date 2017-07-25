package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;

import java.io.Serializable;

/**
 * T分值
 *
 * @author luckylo
 * @createTime 2017-07-24.
 */
public class TScore extends MongoAggrObject implements Serializable {

    private Range range;

    private Target target;

    private double tScore;

    public TScore() {
    }

    public TScore(Range range, Target target, double tScore) {
        this.range = range;
        this.target = target;
        this.tScore = tScore;
    }

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

    public double gettScore() {
        return tScore;
    }

    public void settScore(double tScore) {
        this.tScore = tScore;
    }

    @Override
    public String toString() {
        return "TScore{" +
                "range=" + range +
                ", target=" + target +
                ", tScore=" + tScore +
                '}';
    }
}
