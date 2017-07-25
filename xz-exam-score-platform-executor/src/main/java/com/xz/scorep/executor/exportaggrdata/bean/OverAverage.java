package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;

import java.io.Serializable;

/**
 * 超均率
 * @author luckylo
 * @createTime 2017-07-20.
 */
public class OverAverage extends MongoAggrObject implements Serializable {

    private Range range;

    private Target target;

    private double overAverage;

    public OverAverage() {
    }

    public OverAverage(Range range, Target target, double overAverage) {
        this.range = range;
        this.target = target;
        this.overAverage = overAverage;
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

    public double getOverAverage() {
        return overAverage;
    }

    public void setOverAverage(double overAverage) {
        this.overAverage = overAverage;
    }

    @Override
    public String toString() {
        return "OverAverage{" +
                "range=" + range +
                ", target=" + target +
                ", overAverage=" + overAverage +
                '}';
    }
}
