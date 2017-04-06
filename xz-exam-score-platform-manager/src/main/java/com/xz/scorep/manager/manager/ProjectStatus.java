package com.xz.scorep.manager.manager;

/**
 * 项目 ID 和状态
 *
 * @author yidin
 */
public class ProjectStatus {

    private String projectId;

    private String status;

    public ProjectStatus() {
    }

    public ProjectStatus(String projectId, String status) {
        this.projectId = projectId;
        this.status = status;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
