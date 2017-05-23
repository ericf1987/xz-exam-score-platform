package com.xz.scorep.executor.bean;

import java.io.Serializable;

/**
 * 包装客观题得分详情
 *
 * @author luckylo
 */
public class ObjectiveScoreDetail implements Serializable {

    private String questNo;

    private String standardAnswer;

    private String answer;

    private String scoreRate;

    public ObjectiveScoreDetail() {
    }

    public ObjectiveScoreDetail(String questNo, String standardAnswer, String answer, String scoreRate) {
        this.questNo = questNo;
        this.standardAnswer = standardAnswer;
        this.answer = answer;
        this.scoreRate = scoreRate;
    }

    @Override
    public String toString() {
        return "ObjectiveScoreDetail{" +
                "questNo='" + questNo + '\'' +
                ", standardAnswer='" + standardAnswer + '\'' +
                ", answer='" + answer + '\'' +
                ", scoreRate='" + scoreRate + '\'' +
                '}';
    }

    public String getQuestNo() {
        return questNo;
    }

    public void setQuestNo(String questNo) {
        this.questNo = questNo;
    }

    public String getStandardAnswer() {
        return standardAnswer;
    }

    public void setStandardAnswer(String standardAnswer) {
        this.standardAnswer = standardAnswer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getScoreRate() {
        return scoreRate;
    }

    public void setScoreRate(String scoreRate) {
        this.scoreRate = scoreRate;
    }
}
