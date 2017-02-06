package com.xz.scorep.executor.bean;

import com.alibaba.fastjson.JSONObject;

/**
 * (description)
 * created at 2017/2/4
 *
 * @author yidin
 */
public class ProjectClass {

    private String id;

    private String name;

    private String schoolId;

    public ProjectClass() {
    }

    public ProjectClass(String id, String name, String schoolId) {
        this.id = id;
        this.name = name;
        this.schoolId = schoolId;
    }

    public ProjectClass(JSONObject jsonObject) {
        this.id = jsonObject.getString("id");
        this.name = jsonObject.getString("name");
        this.schoolId = jsonObject.getString("schoolId");
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

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }
}
