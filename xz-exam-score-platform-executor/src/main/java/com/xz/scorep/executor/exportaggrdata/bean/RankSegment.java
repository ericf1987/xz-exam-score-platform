package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author by fengye on 2017/7/20.
 */
public class RankSegment extends MongoAggrObject implements Serializable{

    private Range range;

    private Target target;

    private List<Map<String, Object>> rankSegments = new ArrayList();

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

    public List<Map<String, Object>> getRankSegments() {
        return rankSegments;
    }

    public RankSegment() {
    }

    public RankSegment(Range range, Target target, List<Map<String, Object>> rankSegments) {
        this.range = range;
        this.target = target;
        this.rankSegments = rankSegments;
    }

    @Override
    public String toString() {
        return "RankSegment{" +
                "range=" + range +
                ", target=" + target +
                ", rankSegments=" + rankSegments +
                '}';
    }
}
