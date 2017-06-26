package com.xz.scorep.executor.bean;

import com.alibaba.fastjson.JSONObject;

/**
 * @author by fengye on 2017/6/26.
 */
public class AbilityLevel {
    private String studyStage;
    private String levelId;
    private String levelName;
    private String abilityType;
    private String subjectId;
    private String fullScore;

    public String getStudyStage() {
        return studyStage;
    }

    public void setStudyStage(String studyStage) {
        this.studyStage = studyStage;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getAbilityType() {
        return abilityType;
    }

    public void setAbilityType(String abilityType) {
        this.abilityType = abilityType;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getFullScore() {
        return fullScore;
    }

    public void setFullScore(String fullScore) {
        this.fullScore = fullScore;
    }

    public AbilityLevel() {
    }

    public AbilityLevel(String studyStage, String levelId, String levelName, String abilityType, String subjectId, String fullScore) {
        this.studyStage = studyStage;
        this.levelId = levelId;
        this.levelName = levelName;
        this.abilityType = abilityType;
        this.subjectId = subjectId;
        this.fullScore = fullScore;
    }

    public AbilityLevel(String studyStage, String levelId, String levelName, String abilityType, String subjectId) {
        this.studyStage = studyStage;
        this.levelId = levelId;
        this.levelName = levelName;
        this.abilityType = abilityType;
        this.subjectId = subjectId;
    }

    @Override
    public String toString() {
        return "AbilityLevel{" +
                "studyStage='" + studyStage + '\'' +
                ", levelId='" + levelId + '\'' +
                ", levelName='" + levelName + '\'' +
                ", abilityType='" + abilityType + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", fullScore='" + fullScore + '\'' +
                '}';
    }
}
