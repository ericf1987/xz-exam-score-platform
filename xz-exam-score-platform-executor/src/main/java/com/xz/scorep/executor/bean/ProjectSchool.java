package com.xz.scorep.executor.bean;

import java.io.Serializable;

/**
 * (description)
 * created at 2017/2/4
 *
 * @author yidin
 */
public class ProjectSchool implements Serializable {

    private String id;

    private String name;

    private String area;

    private String city;

    private String province;

    public ProjectSchool() {
    }

    public ProjectSchool(String id, String name, String area, String city, String province) {
        this.id = id;
        this.name = name;
        this.area = area;
        this.city = city;
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
}
