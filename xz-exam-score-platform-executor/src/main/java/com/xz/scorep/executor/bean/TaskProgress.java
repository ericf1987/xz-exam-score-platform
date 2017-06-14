package com.xz.scorep.executor.bean;

import com.xz.scorep.executor.utils.DoubleUtils;

import java.io.Serializable;

/**
 * @author by fengye on 2017/6/12.
 */
public class TaskProgress implements Serializable {
    private String projectId;
    private String taskName;
    private int taskCount;
    private int taskFinished;
    private String startTime;
    private String endTime;
    private String taskStatus;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public int getTaskFinished() {
        return taskFinished;
    }

    public void setTaskFinished(int taskFinished) {
        this.taskFinished = taskFinished;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public TaskProgress() {
    }

    public TaskProgress(String projectId, String taskName, int taskCount, int taskFinished, String startTime, String endTime, String taskStatus) {

        this.projectId = projectId;
        this.taskName = taskName;
        this.taskCount = taskCount;
        this.taskFinished = taskFinished;
        this.startTime = startTime;
        this.endTime = endTime;
        this.taskStatus = taskStatus;
    }

    @Override
    public String toString() {
        return "TaskProgress{" +
                "projectId='" + projectId + '\'' +
                ", taskName='" + taskName + '\'' +
                ", taskCount=" + taskCount +
                ", taskFinished=" + taskFinished +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", taskStatus='" + taskStatus + '\'' +
                '}';
    }

    public double getProgress() {
        return this.taskCount == 0 ?
                0 : DoubleUtils.round((double) this.taskFinished / this.taskCount);
    }

    public void increaseFinished(TaskProgress taskProgress) {
        synchronized (taskProgress) {
            this.taskFinished++;
        }
    }

}
