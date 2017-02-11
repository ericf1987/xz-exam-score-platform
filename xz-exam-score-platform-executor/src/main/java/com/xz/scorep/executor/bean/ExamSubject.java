package com.xz.scorep.executor.bean;

/**
 * (description)
 * created at 2017/2/11
 *
 * @author yidin
 */
public class ExamSubject {

    private String id;

    private double fullScore;

    public ExamSubject(String id, double fullScore) {
        this.id = id;
        this.fullScore = fullScore;
    }

    public ExamSubject() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getFullScore() {
        return fullScore;
    }

    public void setFullScore(double fullScore) {
        this.fullScore = fullScore;
    }
}
