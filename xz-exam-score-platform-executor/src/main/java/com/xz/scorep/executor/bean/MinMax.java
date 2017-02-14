package com.xz.scorep.executor.bean;

/**
 * (description)
 * created at 2017/2/14
 *
 * @author yidin
 */
public class MinMax {

    private double min;

    private double max;

    public MinMax(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public MinMax() {
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
}
