package com.xz.scorep.executor.exportaggrdata.bean;

import com.xz.scorep.executor.bean.Target;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 排名等级
 * @author by fengye on 2017/7/20.
 */
public class RankLevel extends MongoAggrObject implements Serializable{

    private Target target;

    private String student;

    private Map<String, Object> rankLevel = new HashMap<>();

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public Map<String, Object> getRankLevel() {
        return rankLevel;
    }

    public RankLevel(Target target, String student, Map<String, Object> rankLevel) {
        this.target = target;
        this.student = student;
        this.rankLevel = rankLevel;
    }

    public RankLevel() {
    }

    @Override
    public String toString() {
        return "RankLevel{" +
                ", target=" + target +
                ", student='" + student + '\'' +
                ", rankLevel=" + rankLevel +
                '}';
    }
}
