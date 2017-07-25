package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 分数分段
 * @author by fengye on 2017/7/25.
 */
public class ScoreSegment extends MongoAggrObject implements Serializable{

    private Range range;

    private Target target;

    private List<Map<String, Object>> scoreSegments = new ArrayList<>();

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

    public List<Map<String, Object>> getScoreSegments() {
        return scoreSegments;
    }

    public void setScoreSegments(List<Map<String, Object>> scoreSegments) {
        this.scoreSegments = scoreSegments;
    }

    public ScoreSegment() {
    }

    public ScoreSegment(Range range, Target target, List<Map<String, Object>> scoreSegments) {
        this.range = range;
        this.target = target;
        this.scoreSegments = scoreSegments;
    }

    @Override
    public String toString() {
        return "ScoreSegment{" +
                "range=" + range +
                ", target=" + target +
                ", scoreSegments=" + scoreSegments +
                '}';
    }
}
