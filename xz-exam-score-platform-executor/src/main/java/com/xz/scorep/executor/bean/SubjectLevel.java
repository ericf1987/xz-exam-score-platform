package com.xz.scorep.executor.bean;

/**
 * (description)
 * created at 16/06/29
 *
 * @author yiding_he
 */
public class SubjectLevel {

    private String subject;

    private String level;

    public SubjectLevel() {
    }

    public SubjectLevel(String subject, String level) {
        this.subject = subject;
        this.level = level;
    }

    public String getSubject() {
        return subject;
    }

    public String getLevel() {
        return level;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubjectLevel that = (SubjectLevel) o;

        if (!subject.equals(that.subject)) return false;
        return level.equals(that.level);

    }

    @Override
    public int hashCode() {
        int result = subject.hashCode();
        result = 31 * result + level.hashCode();
        return result;
    }
}
