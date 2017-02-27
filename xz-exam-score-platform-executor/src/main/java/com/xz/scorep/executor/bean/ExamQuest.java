package com.xz.scorep.executor.bean;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xz.ajiaedu.common.lang.StringUtil;

/**
 * 题目
 */
public class ExamQuest {

    private String id;

    private String examSubject;

    private String questSubject;

    private String questionTypeId;

    private String questionTypeName;

    private boolean objective;

    private boolean giveFullScore;      // 是否不论考生选择什么一律给满分（缺考和作弊除外）

    private String questNo;

    private double fullScore;

    private String answer;

    private String scoreRule;

    private String options;

    public ExamQuest() {
    }

    public ExamQuest(String id, String examSubject, boolean objective, String questNo, double fullScore) {
        this.id = id;
        this.examSubject = examSubject;
        this.objective = objective;
        this.questNo = questNo;
        this.fullScore = fullScore;
    }

    public ExamQuest(JSONObject jsonObject) {
        this.id = jsonObject.getString("questId");
        this.examSubject = jsonObject.getString("cardSubjectId");
        this.questSubject = jsonObject.getString("subjectId");
        this.questionTypeId = jsonObject.getString("questionTypeId");
        this.questionTypeName = jsonObject.getString("questionTypeName");

        String questType = jsonObject.getString("questType");
        String objTag = jsonObject.getString("subObjTag");
        this.objective = questType == null ?
                "o".equals(objTag) :
                StringUtil.isOneOf(questType, "0", "1", "2");

        this.giveFullScore = jsonObject.getBooleanValue("awardScoreTag");

        this.questNo = jsonObject.getString("paperQuestNum");
        this.fullScore = jsonObject.getDoubleValue("score");
        this.answer = jsonObject.getString("answer");
        this.scoreRule = jsonObject.getString("scoreRule");

        JSONArray items = jsonObject.getJSONArray("items");
        this.options = items == null? "": items.toJSONString();
    }

    public boolean isGiveFullScore() {
        return giveFullScore;
    }

    public void setGiveFullScore(boolean giveFullScore) {
        this.giveFullScore = giveFullScore;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getQuestionTypeId() {
        return questionTypeId;
    }

    public void setQuestionTypeId(String questionTypeId) {
        this.questionTypeId = questionTypeId;
    }

    public String getQuestionTypeName() {
        return questionTypeName;
    }

    public void setQuestionTypeName(String questionTypeName) {
        this.questionTypeName = questionTypeName;
    }

    public boolean isObjective() {
        return objective;
    }

    public void setObjective(boolean objective) {
        this.objective = objective;
    }

    public String getQuestNo() {
        return questNo;
    }

    public void setQuestNo(String questNo) {
        this.questNo = questNo;
    }

    public double getFullScore() {
        return fullScore;
    }

    public void setFullScore(double fullScore) {
        this.fullScore = fullScore;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getScoreRule() {
        return scoreRule;
    }

    public void setScoreRule(String scoreRule) {
        this.scoreRule = scoreRule;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }
}