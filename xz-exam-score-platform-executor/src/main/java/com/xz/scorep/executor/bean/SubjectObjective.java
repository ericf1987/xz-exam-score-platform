package com.xz.scorep.executor.bean;

/**
 * (description)
 * created at 16/05/11
 *
 * @author yiding_he
 */
public class SubjectObjective {

    private String subject;

    private boolean objective;

    public SubjectObjective() {
    }

    public SubjectObjective(String subject, boolean objective) {
        this.subject = subject;
        this.objective = objective;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public boolean isObjective() {
        return objective;
    }

    public void setObjective(boolean objective) {
        this.objective = objective;
    }
}
