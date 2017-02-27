package com.xz.scorep.executor.bean;

import com.alibaba.fastjson.JSON;

import java.util.Objects;

/**
 * (description)
 * created at 16/05/10
 *
 * @author yiding_he
 */
@SuppressWarnings("unchecked")
public class Target {

    public static final String PROJECT = "project";

    public static final String SUBJECT = "subject";

    public static final String SUBJECT_OBJECTIVE = "subjectObjective";

    public static final String SUBJECT_COMBINATION = "subjectCombination";

    public static final String QUEST = "quest";

    public static final String QUEST_TYPE = "questType";

    public static final String POINT = "point";

    public static final String POINT_LEVEL = "pointLevel";

    public static final String SUBJECT_LEVEL = "subjectLevel";

    public static final String QUEST_ABILITY_LEVEL = "questAbilityLevel";

    public static Target project(String project) { return new Target(Target.PROJECT, project); }
    public static Target subject(String subject) { return new Target(Target.SUBJECT, subject); }
    public static Target subjectObjective(SubjectObjective subjectObjective) { return new Target(Target.SUBJECT_OBJECTIVE, subjectObjective); }
    public static Target subjectCombination(String subjectCombinationId) { return new Target(Target.SUBJECT_COMBINATION, subjectCombinationId); }
    public static Target quest(String quest) { return new Target(Target.QUEST, quest); }
    public static Target questType(String questType) { return new Target(Target.QUEST_TYPE, questType); }
    public static Target point(String point) { return new Target(Target.POINT, point); }
    public static Target pointLevel(String point, String level) { return new Target(POINT_LEVEL, new PointLevel(point, level)); }
    public static Target subjectLevel(String subject, String level) { return new Target(SUBJECT_LEVEL, new SubjectLevel(subject, level)); }
    public static Target subjectLevel(SubjectLevel subjectLevel) { return new Target(SUBJECT_LEVEL, subjectLevel); }
    public static Target pointLevel(PointLevel pointLevel) { return new Target(POINT_LEVEL, pointLevel); }
    public static Target questAbilityLevel(String questAbilityLevel) {return new Target(QUEST_ABILITY_LEVEL, questAbilityLevel);}

    public static Target project(String project, String name) { return new Target(Target.PROJECT, project, name); }
    public static Target subject(String subject, String name) { return new Target(Target.SUBJECT, subject, name); }
    public static Target subjectObjective(SubjectObjective subjectObjective, String name) { return new Target(Target.SUBJECT_OBJECTIVE, subjectObjective, name); }
    public static Target subjectCombination(String subjectCombinationId, String name) { return new Target(Target.SUBJECT_COMBINATION, subjectCombinationId, name); }
    public static Target quest(String quest, String name) { return new Target(Target.QUEST, quest, name); }
    public static Target questType(String questType, String name) { return new Target(Target.QUEST_TYPE, questType, name); }
    public static Target point(String point, String name) { return new Target(Target.POINT, point, name); }
    public static Target pointLevel(String point, String level, String name) { return new Target(POINT_LEVEL, new PointLevel(point, level), name); }
    public static Target subjectLevel(String subject, String level, String name) { return new Target(SUBJECT_LEVEL, new SubjectLevel(subject, level), name); }
    public static Target subjectLevel(SubjectLevel subjectLevel, String name) { return new Target(SUBJECT_LEVEL, subjectLevel, name); }
    public static Target pointLevel(PointLevel pointLevel, String name) { return new Target(POINT_LEVEL, pointLevel, name); }
    public static Target questAbilityLevel(String questAbilityLevel, String name) {return new Target(QUEST_ABILITY_LEVEL, questAbilityLevel, name);}

    private String type;

    private Object id;

    private String name;

    public Target() {
    }

    public Target(String type, Object id) {
        this.type = type;
        this.id = id;
    }

    public Target(String type, Object id, String name) {
        this.type = type;
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Object getId() {
        return id;
    }

    public <T> T getId(Class<T> type) {
        if (type.isAssignableFrom(this.id.getClass())) {
            return (T) this.id;
        } else {
            return JSON.toJavaObject((JSON) JSON.toJSON(this.id), type);
        }
    }

    public Target setId(Object id) {
        this.id = id;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean match(String target) {
        return Objects.equals(target, this.type);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Target target = (Target) o;

        if (!type.equals(target.type)) return false;
        if (!id.equals(target.id)) return false;
        return name != null ? name.equals(target.name) : target.name == null;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Target{" +
                "type='" + type + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
