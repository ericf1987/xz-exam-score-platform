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

    private String range;

    private Date createTime;

    private int grade;

    private double fullScore;

    private String status;

    public ExamProject() {
    }

    public ExamProject(String id, String name, String range, Date createTime, int grade, double fullScore) {
        this.id = id;
        this.name = name;
        this.range = range;
        this.createTime = createTime;
        this.grade = grade;
        this.fullScore = fullScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

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

    @Override
    public String toString() {
        return "ExamProject{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", range='" + range + '\'' +
                ", createTime=" + createTime +
                ", grade=" + grade +
                ", fullScore=" + fullScore +
                '}';
    }

    public boolean canQuery() {
        return this.getStatus() != null && (
                getStatus().equals(ProjectStatus.Ready.name()) ||
                        getStatus().equals(ProjectStatus.GeneratingReport.name()) ||
                        getStatus().equals(ProjectStatus.Archiving.name()));
    }
}
