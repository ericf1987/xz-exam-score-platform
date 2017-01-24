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
        parameters.setImportProjectInfo(true);
        parameters.setImportReportConfig(true);
        parameters.setImportStudents(true);
        parameters.setImportQuests(true);
        return parameters;
    }

    public static ImportProjectParameters importNone(String projectId) {
        ImportProjectParameters parameters = new ImportProjectParameters();
        parameters.setProjectId(projectId);
        parameters.setImportProjectInfo(false);
        parameters.setImportReportConfig(false);
        parameters.setImportStudents(false);
        parameters.setImportQuests(false);
        return parameters;
    }

    private String projectId;               // 项目ID

    private boolean importProjectInfo;      // 是否导入考试项目属性

    private boolean importReportConfig;     // 是否导入报表配置

    private boolean importStudents;         // 是否导入考生信息（考生、班级、学校）

    private boolean importQuests;           // 是否导入题目信息（科目、题目）

    private ImportProjectParameters() {

    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
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
