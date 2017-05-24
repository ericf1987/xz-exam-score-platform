package com.xz.scorep.executor.pss.bean;

import java.util.List;

/**
 * @author by fengye on 2017/3/15.
 */
public class TotalScoreZone{

    private double totalScore;

    private List<TextRect> textRects;

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public List<TextRect> getTextRects() {
        return textRects;
    }

    public void setTextRects(List<TextRect> textRects) {
        this.textRects = textRects;
    }

    public TotalScoreZone(double totalScore, List<TextRect> textRects) {
        this.totalScore = totalScore;
        this.textRects = textRects;
    }


}
