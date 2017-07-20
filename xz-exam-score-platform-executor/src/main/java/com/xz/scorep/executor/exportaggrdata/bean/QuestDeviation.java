package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Range;

import java.io.Serializable;

/**
 * 题目得分标准差
 *
 * @author luckylo
 * @createTime 2017-07-20.
 */
public class QuestDeviation extends MongoAggrObject implements Serializable {

    private Range range;

    private String quest;

    private double deviation;

    public QuestDeviation() {
    }

    public QuestDeviation(Range range, String quest, double deviation) {
        this.range = range;
        this.quest = quest;
        this.deviation = deviation;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public String getQuest() {
        return quest;
    }

    public void setQuest(String quest) {
        this.quest = quest;
    }

    public double getDeviation() {
        return deviation;
    }

    public void setDeviation(double deviation) {
        this.deviation = deviation;
    }

    @Override
    public String toString() {
        return "QuestDeviation{" +
                "range=" + range +
                ", quest='" + quest + '\'' +
                ", deviation=" + deviation +
                '}';
    }
}
