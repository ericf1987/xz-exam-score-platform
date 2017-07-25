package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 分数等级
 * @author by fengye on 2017/7/20.
 */
public class ScoreLevelMap extends MongoAggrObject implements Serializable{

    private Range range;

    private Target target;

    private List<Map<String, Object>> scoreLevels = new ArrayList();

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

    public List<Map<String, Object>> getScoreLevels() {
        return scoreLevels;
    }

    public ScoreLevelMap() {
    }

    public ScoreLevelMap(Range range, Target target, List<Map<String, Object>> scoreLevels) {
        this.range = range;
        this.target = target;
        this.scoreLevels = scoreLevels;
    }

    @Override
    public String toString() {
        return "ScoreLevelMap{" +
                "range=" + range +
                ", target=" + target +
                ", scoreLevels=" + scoreLevels +
                '}';
    }
}
