package com.xz.scorep.executor.pss.bean;

import java.util.List;
import java.util.Map;

/**
 * @author by fengye on 2017/2/28.
 */
public class PaperScreenShotBean {
    private String projectId;
    private String schoolId;
    private String classId;
    private String subjectId;
    private List<Map<String, Object>> studentCardSlices;
    private String taskId;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public List<Map<String, Object>> getStudentCardSlices() {
        return studentCardSlices;
    }

    public void setStudentCardSlices(List<Map<String, Object>> studentCardSlices) {
        this.studentCardSlices = studentCardSlices;
    }

    public PaperScreenShotBean(String projectId, String schoolId, String classId, String subjectId, List<Map<String, Object>> studentCardSlices, String taskId) {
        this.projectId = projectId;
        this.schoolId = schoolId;
        this.classId = classId;
        this.subjectId = subjectId;
        this.studentCardSlices = studentCardSlices;
        this.taskId = taskId;
    }

    public PaperScreenShotBean(String projectId, String schoolId, String classId, String subjectId, List<Map<String, Object>> studentCardSlices) {
        this.projectId = projectId;
        this.schoolId = schoolId;
        this.classId = classId;
        this.subjectId = subjectId;
        this.studentCardSlices = studentCardSlices;
    }

    @Override
    public String toString() {
        return "PaperScreenShotBean{" +
                "projectId='" + projectId + '\'' +
                ", schoolId='" + schoolId + '\'' +
                ", classId='" + classId + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", studentCardSlices=" + studentCardSlices +
                ", taskId='" + taskId + '\'' +
                '}';
    }
}
