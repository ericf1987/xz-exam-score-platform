package com.xz.scorep.executor.bean;

import com.alibaba.fastjson.JSONObject;

/**
 * (description)
 * created at 2017/2/4
 *
 * @author yidin
 */
public class ProjectStudent {

    private String id;

    private String name;

    private String classId;

    public ProjectStudent() {
    }

    public ProjectStudent(String id, String name, String classId) {
        this.id = id;
        this.name = name;
        this.classId = classId;
    }

    public ProjectStudent(JSONObject jsonObject) {
        this.id = jsonObject.getString("id");
        this.name = jsonObject.getString("name");
        this.classId = jsonObject.getString("class");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }
}
