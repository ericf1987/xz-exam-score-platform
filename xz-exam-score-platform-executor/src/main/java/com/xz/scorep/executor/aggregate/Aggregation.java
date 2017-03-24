package com.xz.scorep.executor.aggregate;

import com.xz.ajiaedu.common.lang.Id;

import java.util.Date;

/**
 * 表示对一个项目进行一次统计的信息
 *
 * @author yidin
 */
public class Aggregation {

    private String id = String.valueOf(Id.nextWithDate());

    private AggregateStatus status = AggregateStatus.Idle;

    private String projectId;

    private String subjectId;

    public AggregateType getAggrType() {
        return aggrType;
    }

    public void setAggrType(AggregateType aggrType) {
        this.aggrType = aggrType;
    }

    private AggregateType aggrType;

    private Date startTime;

    private Date endTime;

    public Aggregation(String projectId) {
        this.projectId = projectId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AggregateStatus getStatus() {
        return status;
    }

    public void setStatus(AggregateStatus status) {
        this.status = status;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    //////////////////////////////////////////////////////////////

    public long duration() {
        if (startTime == null) {
            return -1;
        }

        long start = startTime.getTime();
        long end = this.endTime == null ? System.currentTimeMillis() : this.endTime.getTime();
        return end - start;
    }
}
