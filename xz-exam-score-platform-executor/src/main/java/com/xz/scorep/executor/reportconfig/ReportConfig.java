package com.xz.scorep.executor.reportconfig;

/**
 * 报表的项目个性化配置
 *
 * @author yiding_he
 */
public class ReportConfig {

    private String projectId;

    private String combineCategorySubjects;

    private String separateCategorySubjects;

    private String collegeEntryLevelEnabled;

    private String rankLevels;

    private int rankSegmentCount;

    private String scoreLevels;

    private String rankLevelCombines;

    private double topStudentRate;

    private double highScoreRate;

    private String collegeEntryLevel;

    private String entryLevelStatType;

    private String shareSchoolReport;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getCombineCategorySubjects() {
        return combineCategorySubjects;
    }

    public void setCombineCategorySubjects(String combineCategorySubjects) {
        this.combineCategorySubjects = combineCategorySubjects;
    }

    public String getSeparateCategorySubjects() {
        return separateCategorySubjects;
    }

    public void setSeparateCategorySubjects(String separateCategorySubjects) {
        this.separateCategorySubjects = separateCategorySubjects;
    }

    public String getCollegeEntryLevelEnabled() {
        return collegeEntryLevelEnabled;
    }

    public void setCollegeEntryLevelEnabled(String collegeEntryLevelEnabled) {
        this.collegeEntryLevelEnabled = collegeEntryLevelEnabled;
    }

    public String getRankLevels() {
        return rankLevels;
    }

    public void setRankLevels(String rankLevels) {
        this.rankLevels = rankLevels;
    }

    public int getRankSegmentCount() {
        return rankSegmentCount;
    }

    public void setRankSegmentCount(int rankSegmentCount) {
        this.rankSegmentCount = rankSegmentCount;
    }

    public String getScoreLevels() {
        return scoreLevels;
    }

    public void setScoreLevels(String scoreLevels) {
        this.scoreLevels = scoreLevels;
    }

    public String getRankLevelCombines() {
        return rankLevelCombines;
    }

    public void setRankLevelCombines(String rankLevelCombines) {
        this.rankLevelCombines = rankLevelCombines;
    }

    public double getTopStudentRate() {
        return topStudentRate;
    }

    public void setTopStudentRate(double topStudentRate) {
        this.topStudentRate = topStudentRate;
    }

    public double getHighScoreRate() {
        return highScoreRate;
    }

    public void setHighScoreRate(double highScoreRate) {
        this.highScoreRate = highScoreRate;
    }

    public String getCollegeEntryLevel() {
        return collegeEntryLevel;
    }

    public void setCollegeEntryLevel(String collegeEntryLevel) {
        this.collegeEntryLevel = collegeEntryLevel;
    }

    public String getEntryLevelStatType() {
        return entryLevelStatType;
    }

    public void setEntryLevelStatType(String entryLevelStatType) {
        this.entryLevelStatType = entryLevelStatType;
    }

    public String getShareSchoolReport() {
        return shareSchoolReport;
    }

    public void setShareSchoolReport(String shareSchoolReport) {
        this.shareSchoolReport = shareSchoolReport;
    }
}
