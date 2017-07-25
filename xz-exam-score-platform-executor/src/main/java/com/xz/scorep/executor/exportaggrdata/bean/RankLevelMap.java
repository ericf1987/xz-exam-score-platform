package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 排名等第
 * @author by fengye on 2017/7/20.
 */
public class RankLevelMap extends MongoAggrObject implements Serializable{

    private Range range;

    private Target target;

    private List<Map<String, Object>> rankLevelMap = new ArrayList<>();

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

    public List<Map<String, Object>> getRankLevelMap() {
        return rankLevelMap;
    }

    public RankLevelMap(Range range, Target target, List<Map<String, Object>> rankLevelMap) {
        this.range = range;
        this.target = target;
        this.rankLevelMap = rankLevelMap;
    }

    public RankLevelMap() {
    }

    @Override
    public String toString() {
        return "RankLevelMap{" +
                "range=" + range +
                ", target=" + target +
                ", rankLevelMap=" + rankLevelMap +
                '}';
    }
}
