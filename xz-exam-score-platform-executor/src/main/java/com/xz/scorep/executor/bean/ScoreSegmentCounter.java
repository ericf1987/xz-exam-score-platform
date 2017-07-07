package com.xz.scorep.executor.bean;

import com.xz.ajiaedu.common.lang.CounterMap;

/**
 * 构建分数段对象，描述每一个分数段的人数
 * @author by fengye on 2017/7/7.
 */
public class ScoreSegmentCounter {
    private String projectId;

    private Range range;

    private Target target;

    private int max;

    private int min;

    private int interval;

    private CounterMap<Integer> counterMap = new CounterMap<>();

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
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

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public CounterMap<Integer> getCounterMap() {
        return counterMap;
    }

    public void addToCounter(double score) {
        int key = ((int) (score / interval)) * interval;
        counterMap.incre(key);
    }

    public void addToCounter(double score, int count) {
        int key = ((int) (score / interval)) * interval;
        counterMap.incre(key, count);
    }

    public ScoreSegmentCounter(String projectId, Range range, Target target, int max, int min, int interval) {
        this.projectId = projectId;
        this.range = range;
        this.target = target;
        this.max = max;
        this.min = min;
        this.interval = interval;
    }

    public ScoreSegmentCounter() {
    }
}
