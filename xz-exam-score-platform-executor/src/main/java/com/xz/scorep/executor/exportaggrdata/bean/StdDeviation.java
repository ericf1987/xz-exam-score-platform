package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;

import java.io.Serializable;

/**
 * 标准差
 * @author by fengye on 2017/7/21.
 */
public class StdDeviation extends MongoAggrObject implements Serializable{

    private Range range;

    private Target target;

    private double stdDeviation;

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

    public double getStdDeviation() {
        return stdDeviation;
    }

    public void setStdDeviation(double stdDeviation) {
        this.stdDeviation = stdDeviation;
    }

    public StdDeviation() {
    }

    public StdDeviation(Range range, Target target, double stdDeviation) {
        this.range = range;
        this.target = target;
        this.stdDeviation = stdDeviation;
    }

    @Override
    public String toString() {
        return "StdDeviation{" +
                "range=" + range +
                ", target=" + target +
                ", stdDeviation=" + stdDeviation +
                '}';
    }
}
