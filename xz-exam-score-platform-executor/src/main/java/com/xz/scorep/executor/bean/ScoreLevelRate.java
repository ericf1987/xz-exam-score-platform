package com.xz.scorep.executor.bean;

import com.hyd.dao.Row;

/**
 * (description)
 * created at 2017/2/14
 *
 * @author yidin
 */
public class ScoreLevelRate {

    private ScoreLevel scoreLevel;

    private int studentCount;

    private double studentRate;

    public ScoreLevelRate() {
    }

    public ScoreLevelRate(ScoreLevel scoreLevel, int studentCount, double studentRate) {
        this.scoreLevel = scoreLevel;
        this.studentCount = studentCount;
        this.studentRate = studentRate;
    }

    public ScoreLevelRate(Row row) {
        this(ScoreLevel.valueOf(row.getString("score_level")),
                row.getInteger("student_count", 0),
                row.getDouble("student_rate", 0));
    }

    public ScoreLevel getScoreLevel() {
        return scoreLevel;
    }

    public void setScoreLevel(ScoreLevel scoreLevel) {
        this.scoreLevel = scoreLevel;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    public double getStudentRate() {
        return studentRate;
    }

    public void setStudentRate(double studentRate) {
        this.studentRate = studentRate;
    }

    @Override
    public String toString() {
        return "ScoreLevelRate{" +
                "scoreLevel=" + scoreLevel +
                ", studentCount=" + studentCount +
                ", studentRate=" + studentRate +
                '}';
    }
}
