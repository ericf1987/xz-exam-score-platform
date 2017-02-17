package com.xz.scorep.executor.bean;

import com.xz.ajiaedu.common.report.Keys;

/**
 * (description)
 * created at 2017/2/15
 *
 * @author yidin
 */
public class Target {

    private String type;

    private String id;

    public Target(String type, String id) {
        this.type = type;
        this.id = id;
    }

    public Target() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    //////////////////////////////////////////////////////////////

    public static Target project(String projectId) {
        return new Target(Keys.Target.Project.name(), projectId);
    }
}
