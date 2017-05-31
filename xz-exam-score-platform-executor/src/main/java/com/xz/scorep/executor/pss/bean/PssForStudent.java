package com.xz.scorep.executor.pss.bean;

/**
 * @author by fengye on 2017/5/31.
 */
public class PssForStudent {
    private String projectId;
    private String schoolId;
    private String classId;
    private String subjectId;
    private String studentId;

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

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public PssForStudent() {
    }

    public PssForStudent(String projectId, String schoolId, String classId, String subjectId, String studentId) {
        this.projectId = projectId;
        this.schoolId = schoolId;
        this.classId = classId;
        this.subjectId = subjectId;
        this.studentId = studentId;
    }

    @Override
    public String toString() {
        return "PssForStudent{" +
                "projectId='" + projectId + '\'' +
                ", schoolId='" + schoolId + '\'' +
                ", classId='" + classId + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", studentId='" + studentId + '\'' +
                '}';
    }
}
