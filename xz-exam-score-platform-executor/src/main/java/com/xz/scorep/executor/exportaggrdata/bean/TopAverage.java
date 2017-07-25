package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;

import java.io.Serializable;
import java.util.Map;

/**
 * 高分段平均分
 * @author luckylo
 * @createTime 2017-07-24.
 */
public class TopAverage extends MongoAggrObject implements Serializable {

    private Range range;

    private Target target;

    private Map<String, Object> topAverages;

    public TopAverage() {
    }

    public TopAverage(Range range, Target target, Map<String, Object> topAverages) {
        this.range = range;
        this.target = target;
        this.topAverages = topAverages;
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

    public Map<String, Object> getTopAverages() {
        return topAverages;
    }

    public void setTopAverages(Map<String, Object> topAverages) {
        this.topAverages = topAverages;
    }

    @Override
    public String toString() {
        return "TopAverage{" +
                "range=" + range +
                ", target=" + target +
                ", topAverages=" + topAverages +
                '}';
    }
}
