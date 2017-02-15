package com.xz.scorep.executor.bean;

import com.xz.ajiaedu.common.report.Keys;

/**
 * (description)
 * created at 2017/2/15
 *
 * @author yidin
 */
public class Range {

    private String type;

    private String id;

    public Range() {
    }

    public Range(String type, String id) {
        this.type = type;
        this.id = id;
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

    public static Range province(String province) {
        return new Range(Keys.Range.Province.name(), province);
    }

    public static Range school(String schoolId) {
        return new Range(Keys.Range.School.name(), schoolId);
    }

    public static Range clazz(String classId) {
        return new Range(Keys.Range.Class.name(), classId);
    }
}
