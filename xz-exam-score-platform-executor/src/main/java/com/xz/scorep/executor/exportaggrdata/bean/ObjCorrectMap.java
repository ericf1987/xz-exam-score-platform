package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;

import java.io.Serializable;

/**
 * 导出json为客观题正确率
 *
 * @author luckylo
 * @createTime 2017-07-20.
 */
public class ObjCorrectMap extends MongoAggrObject implements Serializable {

    private Range range;

    private Target target;

    private int correctCount;

    private double correctRate;

    public ObjCorrectMap() {
    }

    public ObjCorrectMap(Range range, Target target, int correctCount, double correctRate) {
        this.range = range;
        this.target = target;
        this.correctCount = correctCount;
        this.correctRate = correctRate;
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

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public double getCorrectRate() {
        return correctRate;
    }

    public void setCorrectRate(double correctRate) {
        this.correctRate = correctRate;
    }

    @Override
    public String toString() {
        return "ObjCorrectMap{" +
                "range=" + range +
                ", target=" + target +
                ", correctCount=" + correctCount +
                ", correctRate=" + correctRate +
                '}';
    }
}
