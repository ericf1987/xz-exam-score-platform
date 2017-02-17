package com.xz.scorep.executor.bean;

/**
 * (description)
 * created at 2017/2/11
 *
 * @author yidin
 */
public class ExamSubject {

    private String id;

    private String name;

    private double fullScore;

    public ExamSubject(String id, String name, double fullScore) {
        this.id = id;
        this.name = name;
        this.fullScore = fullScore;
    }

    public ExamSubject() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
