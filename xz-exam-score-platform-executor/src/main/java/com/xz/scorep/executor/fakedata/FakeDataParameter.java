package com.xz.scorep.executor.fakedata;

/**
 * (description)
 * created at 16/12/28
 *
 * @author yidin
 */
public class FakeDataParameter {

    private String projectId;

    private int schoolPerProject;

    private int classPerSchool;

    private int studentPerClass;

    private int subjectPerProject;

    private int questPerSubject;

    private double scorePerQuest;

    public FakeDataParameter() {
    }

    public FakeDataParameter(
            String projectId, int schoolPerProject, int classPerSchool,
            int studentPerClass, int subjectPerProject, int questPerSubject,
            double scorePerQuest) {

        this.projectId = projectId;
        this.schoolPerProject = schoolPerProject;
        this.classPerSchool = classPerSchool;
        this.studentPerClass = studentPerClass;
        this.subjectPerProject = subjectPerProject;
        this.questPerSubject = questPerSubject;
        this.scorePerQuest = scorePerQuest;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public int getSchoolPerProject() {
        return schoolPerProject;
    }

    public void setSchoolPerProject(int schoolPerProject) {
        this.schoolPerProject = schoolPerProject;
    }

    public int getClassPerSchool() {
        return classPerSchool;
    }

    public void setClassPerSchool(int classPerSchool) {
        this.classPerSchool = classPerSchool;
    }

    public int getStudentPerClass() {
        return studentPerClass;
    }

    public void setStudentPerClass(int studentPerClass) {
        this.studentPerClass = studentPerClass;
    }

    public int getSubjectPerProject() {
        return subjectPerProject;
    }

    public void setSubjectPerProject(int subjectPerProject) {
        this.subjectPerProject = subjectPerProject;
    }

    public int getQuestPerSubject() {
        return questPerSubject;
    }

    public void setQuestPerSubject(int questPerSubject) {
        this.questPerSubject = questPerSubject;
    }

    public double getScorePerQuest() {
        return scorePerQuest;
    }

    public void setScorePerQuest(double scorePerQuest) {
        this.scorePerQuest = scorePerQuest;
    }

    //////////////////////////////////////////////////////////////

    public double getProjectFullScore() {
        return subjectPerProject * questPerSubject * scorePerQuest;
    }

    public double getSubjectFullScore() {
        return questPerSubject * scorePerQuest;
    }
}
