package com.xz.scorep.executor.pss.bean;

import java.util.List;

/**
 * 试卷区域对象
 *
 * @author by fengye on 2017/5/18.
 */
public class PaperZone {

    /**
     * 试卷区域对象包括 一个总分区域 一个客观题区域和多个主观题答题区域
     */
    private TotalScoreZone totalScoreZone;
    private ObjectiveQuestZone objectiveQuestZone;
    private List<SubjectiveQuestZone> subjectiveQuestZones;

    public TotalScoreZone getTotalScoreZone() {
        return totalScoreZone;
    }

    public void setTotalScoreZone(TotalScoreZone totalScoreZone) {
        this.totalScoreZone = totalScoreZone;
    }

    public ObjectiveQuestZone getObjectiveQuestZone() {
        return objectiveQuestZone;
    }

    public void setObjectiveQuestZone(ObjectiveQuestZone objectiveQuestZone) {
        this.objectiveQuestZone = objectiveQuestZone;
    }

    public List<SubjectiveQuestZone> getSubjectiveQuestZones() {
        return subjectiveQuestZones;
    }

    public void setSubjectiveQuestZones(List<SubjectiveQuestZone> subjectiveQuestZones) {
        this.subjectiveQuestZones = subjectiveQuestZones;
    }

    public PaperZone() {
    }

    public PaperZone(TotalScoreZone totalScoreZone, ObjectiveQuestZone objectiveQuestZone, List<SubjectiveQuestZone> subjectiveQuestZones) {
        this.totalScoreZone = totalScoreZone;
        this.objectiveQuestZone = objectiveQuestZone;
        this.subjectiveQuestZones = subjectiveQuestZones;
    }
}
