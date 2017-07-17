package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;

import java.io.Serializable;

/**
 * 平均分
 * @author by fengye on 2017/7/17.
 */
public class Average extends MongoAggrObject implements Serializable{
    private Range range;
    private Target target;
    private double average;

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

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public Average() {
    }

    public Average(Range range, Target target, double average) {
        this.range = range;
        this.target = target;
        this.average = average;
    }

    @Override
    public String toString() {
        return "Average{" +
                "range=" + range +
                ", target=" + target +
                ", average=" + average +
                '}';
    }
}
