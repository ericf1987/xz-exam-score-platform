package com.xz.scorep.executor.aggregate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * (description)
 * created at 2017/3/17
 *
 * @author yidin
 */
public class AggregateParameter {

    private String projectId;

    private AggregateType aggregateType;

    private boolean importProject;

    private boolean importScore;

    private String aggrName;

    private List<String> subjects = new ArrayList<>();

    public AggregateParameter() {
    }

    public AggregateParameter(String projectId) {
        this.projectId = projectId;
    }

    public AggregateParameter(String projectId, AggregateType aggregateType, String... subjectIds) {
        this.projectId = projectId;
        this.aggregateType = aggregateType;
        this.subjects = Arrays.asList(subjectIds);
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public String getAggrName() {
        return aggrName;
    }

    public void setAggrName(String aggrName) {
        this.aggrName = aggrName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public AggregateType getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(AggregateType aggregateType) {
        this.aggregateType = aggregateType;
    }

    public boolean isImportProject() {
        return importProject;
    }

    public void setImportProject(boolean importProject) {
        this.importProject = importProject;
    }

    public boolean isImportScore() {
        return importScore;
    }

    public void setImportScore(boolean importScore) {
        this.importScore = importScore;
    }
}
