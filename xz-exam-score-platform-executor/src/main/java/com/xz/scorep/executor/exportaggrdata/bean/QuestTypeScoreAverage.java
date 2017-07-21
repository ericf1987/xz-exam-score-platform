package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Range;

import java.io.Serializable;

/**
 * @author luckylo
 * @createTime 2017-07-21.
 */
public class QuestTypeScoreAverage extends MongoAggrObject implements Serializable {

    private Range range;

    private String questType;

    private double average;

    private double rate;

    public QuestTypeScoreAverage() {
    }

    public QuestTypeScoreAverage(Range range, String questType, double average, double rate) {
        this.range = range;
        this.questType = questType;
        this.average = average;
        this.rate = rate;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public String getQuestType() {
        return questType;
    }

    public void setQuestType(String questType) {
        this.questType = questType;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "QuestTypeScoreAverage{" +
                "range=" + range +
                ", questType='" + questType + '\'' +
                ", average=" + average +
                ", rate=" + rate +
                '}';
    }
}
