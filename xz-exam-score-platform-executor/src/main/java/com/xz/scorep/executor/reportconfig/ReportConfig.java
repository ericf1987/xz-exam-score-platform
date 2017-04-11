package com.xz.scorep.executor.reportconfig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 报表的项目个性化配置
 *
 * @author yiding_he
 */
public class ReportConfig {

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 是否合并独立的文理科目
     */
    private String combineCategorySubjects;

    /**
     * 是否将文综和理综拆分
     */
    private String separateCategorySubjects;

    /**
     * 是否拟定录取分数线
     */
    private String collegeEntryLevelEnabled;

    /**
     * 等第配置
     */
    private String rankLevels;

    /**
     *
     */
    private int rankSegmentCount;

    /**
     * 等第组合配置
     */
    private String rankLevelCombines;

    /**
     * 四率（优/良/及格/不及格）的得分率标准
     */
    private String scoreLevels;

    /**
     * 尖子生比例
     */
    private double topStudentRate;

    /**
     * 高分比例
     */
    private double highScoreRate;

    /**
     * 拟定录取分数线配置
     */
    private String collegeEntryLevel;

    /**
     *
     */
    private String entryLevelStatType;

    /**
     *
     */
    private String shareSchoolReport;

    /**
     * 科目分数分段大小
     */
    private Double subjectSegment;

    /**
     * 总分分段大小
     */
    private Double totalSegment;

    /**
     * 是否排除所有零分
     */
    private String removeZeroScores;

    /**
     * 是否排除所有缺考考生
     */
    private String removeAbsentStudent;

    /**
     * 是否将接近及格的考生分数修改为及格分数
     */
    private String fillAlmostPass;

    /**
     * 比及格分数低多少分以内算作接近及格
     */
    private Double almostPassOffset;

    public String getFillAlmostPass() {
        return fillAlmostPass;
    }

    public void setFillAlmostPass(String fillAlmostPass) {
        this.fillAlmostPass = fillAlmostPass;
    }

    public Double getAlmostPassOffset() {
        return almostPassOffset;
    }

    public void setAlmostPassOffset(Double almostPassOffset) {
        this.almostPassOffset = almostPassOffset;
    }

    public String getRemoveAbsentStudent() {
        return removeAbsentStudent;
    }

    public void setRemoveAbsentStudent(String removeAbsentStudent) {
        this.removeAbsentStudent = removeAbsentStudent;
    }

    public String getRemoveZeroScores() {
        return removeZeroScores;
    }

    public void setRemoveZeroScores(String removeZeroScores) {
        this.removeZeroScores = removeZeroScores;
    }

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

    public Double getSubjectSegment() {
        return subjectSegment;
    }

    public void setSubjectSegment(Double subjectSegment) {
        this.subjectSegment = subjectSegment;
    }

    public Double getTotalSegment() {
        return totalSegment;
    }

    public void setTotalSegment(Double totalSegment) {
        this.totalSegment = totalSegment;
    }

    //////////////////////////////////////////////////////////////

    public Map<String, Double> scoreLevelMap() {
        JSONObject scoreLevels = JSON.parseObject(getScoreLevels());
        Map<String, Double> result = new HashMap<>();

        result.put("Excellent", scoreLevels.getDouble("Excellent"));
        result.put("Good", scoreLevels.getDouble("Good"));
        result.put("Pass", scoreLevels.getDouble("Pass"));
        result.put("Fail", scoreLevels.getDouble("Fail"));

        return result;
    }
}
