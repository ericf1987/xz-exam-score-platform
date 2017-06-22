package com.xz.scorep.executor.bean;

/**
 * @author by fengye on 2017/6/22.
 */
public class ExamQuestType {
    private String id;
    private String questTypeName;
    private String examSubject;
    private String questSubject;
    private double fullScore;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestTypeName() {
        return questTypeName;
    }

    public void setQuestTypeName(String questTypeName) {
        this.questTypeName = questTypeName;
    }

    public String getExamSubject() {
        return examSubject;
    }

    public void setExamSubject(String examSubject) {
        this.examSubject = examSubject;
    }

    public String getQuestSubject() {
        return questSubject;
    }

    public void setQuestSubject(String questSubject) {
        this.questSubject = questSubject;
    }

    public double getFullScore() {
        return fullScore;
    }

    public void setFullScore(double fullScore) {
        this.fullScore = fullScore;
    }

    public ExamQuestType() {
    }

    public ExamQuestType(String id, String questTypeName, String examSubject, String questSubject, double fullScore) {
        this.id = id;
        this.questTypeName = questTypeName;
        this.examSubject = examSubject;
        this.questSubject = questSubject;
        this.fullScore = fullScore;
    }

    @Override
    public String toString() {
        return "ExamQuestType{" +
                "id='" + id + '\'' +
                ", questTypeName='" + questTypeName + '\'' +
                ", examSubject='" + examSubject + '\'' +
                ", questSubject='" + questSubject + '\'' +
                ", fullScore=" + fullScore +
                '}';
    }
}
