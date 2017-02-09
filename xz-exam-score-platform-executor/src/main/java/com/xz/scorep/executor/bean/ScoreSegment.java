package com.xz.scorep.executor.bean;

/**
 * 分数段
 */
public class ScoreSegment {

    private double min;

    private double max;

    public ScoreSegment() {
    }

    public ScoreSegment(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return "ScoreSegment{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }
}
