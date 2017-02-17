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

    private String examNo;

    private String schoolExamNo;

    private String name;

    private String classId;

    private String schoolId;

    private String area;

    private String city;

    private String province;

    public ProjectStudent() {
    }

    public ProjectStudent(
            String id, String examNo, String schoolExamNo,
            String name, String classId, String schoolId,
            String area, String city, String province) {

        this.id = id;
        this.examNo = examNo;
        this.schoolExamNo = schoolExamNo;
        this.name = name;
        this.classId = classId;
        this.schoolId = schoolId;
        this.area = area;
        this.city = city;
        this.province = province;
    }

    public ProjectStudent(JSONObject jsonObject) {
        this.id = jsonObject.getString("id");
        this.name = jsonObject.getString("name");
        this.classId = jsonObject.getString("class");
    }

    public String getExamNo() {
        return examNo;
    }

    public void setExamNo(String examNo) {
        this.examNo = examNo;
    }

    public String getSchoolExamNo() {
        return schoolExamNo;
    }

    public void setSchoolExamNo(String schoolExamNo) {
        this.schoolExamNo = schoolExamNo;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
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

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }
}
