package com.xz.scorep.executor.bean;

import java.io.Serializable;

/**
 * 包装主观题得分详情
 *
 * @author luckylo
 */
public class SubjectiveScoreDetail implements Serializable {
    private String questNo;

    private double score;

    private double averageScore;

    private double maxScore;

    public SubjectiveScoreDetail() {
    }

    public SubjectiveScoreDetail(String questNo, double score, double averageScore, double maxScore) {
        this.questNo = questNo;
        this.score = score;
        this.averageScore = averageScore;
        this.maxScore = maxScore;
    }

    public String getQuestNo() {
        return questNo;
    }

    public void setQuestNo(String questNo) {
        this.questNo = questNo;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(double maxScore) {
        this.maxScore = maxScore;
    }

    @Override
    public String toString() {
        return "ObjectiveScoreDetail{" +
                "questNo='" + questNo + '\'' +
                ", score=" + score +
                ", averageScore=" + averageScore +
                ", maxScore=" + maxScore +
                '}';
    }
}
