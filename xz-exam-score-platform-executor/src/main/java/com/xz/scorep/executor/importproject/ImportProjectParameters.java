package com.xz.scorep.executor.importproject;

/**
 * (description)
 * created at 2017/1/24
 *
 * @author yidin
 */
public class ImportProjectParameters {

    public static ImportProjectParameters importAll(String projectId) {
        ImportProjectParameters parameters = new ImportProjectParameters();
        parameters.setProjectId(projectId);
        parameters.setRecreateDatabase(true);
        parameters.setImportProjectInfo(true);
        parameters.setImportReportConfig(true);
        parameters.setImportStudents(true);
        parameters.setImportQuests(true);
        parameters.setImportScore(true);
        return parameters;
    }

    public static ImportProjectParameters importAllButScore(String projectId) {
        ImportProjectParameters parameters = new ImportProjectParameters();
        parameters.setProjectId(projectId);
        parameters.setRecreateDatabase(true);
        parameters.setImportProjectInfo(true);
        parameters.setImportReportConfig(true);
        parameters.setImportStudents(true);
        parameters.setImportQuests(true);
        parameters.setImportScore(false);
        return parameters;
    }

    public static ImportProjectParameters importScoreOnly(String projectId) {
        ImportProjectParameters parameters = new ImportProjectParameters();
        parameters.setProjectId(projectId);
        parameters.setRecreateDatabase(false);
        parameters.setImportProjectInfo(false);
        parameters.setImportReportConfig(false);
        parameters.setImportStudents(false);
        parameters.setImportQuests(false);
        parameters.setImportScore(true);
        return parameters;
    }

    public static ImportProjectParameters importNone(String projectId) {
        ImportProjectParameters parameters = new ImportProjectParameters();
        parameters.setProjectId(projectId);
        parameters.setRecreateDatabase(false);
        parameters.setImportProjectInfo(false);
        parameters.setImportReportConfig(false);
        parameters.setImportStudents(false);
        parameters.setImportQuests(false);
        parameters.setImportScore(false);
        return parameters;
    }

    public static ImportProjectParameters importSelected(
            String projectId, boolean recreateDatabase,
            boolean projectInfo, boolean reportConfig, boolean students, boolean quests, boolean score) {
        ImportProjectParameters parameters = new ImportProjectParameters();
        parameters.setRecreateDatabase(recreateDatabase);
        parameters.setProjectId(projectId);
        parameters.setImportProjectInfo(projectInfo);
        parameters.setImportReportConfig(reportConfig);
        parameters.setImportStudents(students);
        parameters.setImportQuests(quests);
        parameters.setImportScore(score);
        return parameters;
    }

    private String projectId;               // 项目ID

    private boolean recreateDatabase;       // 是否重新创建数据库

    private boolean importProjectInfo;      // 是否导入考试项目属性

    private boolean importReportConfig;     // 是否导入报表配置

    private boolean importStudents;         // 是否导入考生信息（考生、班级、学校）

    private boolean importQuests;           // 是否导入题目信息（科目、题目）

    private boolean importScore;            // 是否导入分数信息

    private ImportProjectParameters() {

    }

    public boolean isImportScore() {
        return importScore;
    }

    public void setImportScore(boolean importScore) {
        this.importScore = importScore;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public boolean isRecreateDatabase() {
        return recreateDatabase;
    }

    public void setRecreateDatabase(boolean recreateDatabase) {
        this.recreateDatabase = recreateDatabase;
    }

    public boolean isImportProjectInfo() {
        return importProjectInfo;
    }

    public void setImportProjectInfo(boolean importProjectInfo) {
        this.importProjectInfo = importProjectInfo;
    }

    public boolean isImportReportConfig() {
        return importReportConfig;
    }

    public void setImportReportConfig(boolean importReportConfig) {
        this.importReportConfig = importReportConfig;
    }

    public boolean isImportStudents() {
        return importStudents;
    }

    public void setImportStudents(boolean importStudents) {
        this.importStudents = importStudents;
    }

    public boolean isImportQuests() {
        return importQuests;
    }

    public void setImportQuests(boolean importQuests) {
        this.importQuests = importQuests;
    }
}
