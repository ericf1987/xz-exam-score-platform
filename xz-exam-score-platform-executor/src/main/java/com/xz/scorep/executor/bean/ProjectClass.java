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

    private String area;

    private String city;

    private String province;

    public ProjectClass() {
    }

    public ProjectClass(String id, String name, String schoolId, String area, String city, String province) {
        this.id = id;
        this.name = name;
        this.schoolId = schoolId;
        this.area = area;
        this.city = city;
        this.province = province;
    }

    public ProjectClass(JSONObject jsonObject) {
        this.id = jsonObject.getString("id");
        this.name = jsonObject.getString("name");
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
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
