package com.xz.scorep.executor.bean;

import java.util.Date;

/**
 * (description)
 * created at 2017/2/9
 *
 * @author yidin
 */
public class ExamProject {

    private String id;

    private String name;

    private Date createTime;

    private int grade;

    private double fullScore;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public double getFullScore() {
        return fullScore;
    }

    public void setFullScore(double fullScore) {
        this.fullScore = fullScore;
    }
}
