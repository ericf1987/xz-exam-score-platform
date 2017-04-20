package com.xz.scorep.executor.bean;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * (description)
 * created at 2017/2/11
 *
 * @author yidin
 */
public class ExamSubject implements Serializable {

    private String id;

    private String name;

    private double fullScore;

    private String cardId;

    private String virtualSubject;

    public ExamSubject(String id, String name, double fullScore) {
        this.id = id;
        this.name = name;
        this.fullScore = fullScore;
    }

    public ExamSubject() {
    }

    public ExamSubject(JSONObject doc) {
        this.id = doc.getString("subjectId");
        this.name = doc.getString("subjectName");
        this.fullScore = doc.getDoubleValue("totalScore");
        this.cardId = doc.getString("libCardId");
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getVirtualSubject() {
        return virtualSubject;
    }

    public void setVirtualSubject(String virtualSubject) {
        this.virtualSubject = virtualSubject;
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

    public String getVirtualSubject() {
        return virtualSubject;
    }

    public void setVirtualSubject(String virtualSubject) {
        this.virtualSubject = virtualSubject;
    }
}
