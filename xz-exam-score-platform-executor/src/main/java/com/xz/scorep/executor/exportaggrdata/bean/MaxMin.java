package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;

import java.io.Serializable;

/**
 * 最高/最低分
 * @author luckylo
 * @createTime 2017-07-20.
 */
public class MaxMin extends MongoAggrObject implements Serializable {

    private Range range;

    private Target target;

    private double max;

    private double min;

    public MaxMin() {
    }

    public MaxMin(Range range, Target target, double max, double min) {
        this.range = range;
        this.target = target;
        this.max = max;
        this.min = min;
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

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    @Override
    public String toString() {
        return "MaxMin{" +
                "range=" + range +
                ", target=" + target +
                ", max=" + max +
                ", min=" + min +
                '}';
    }
}

