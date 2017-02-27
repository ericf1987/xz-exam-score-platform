package com.xz.scorep.executor.bean;

import java.util.Objects;

/**
 * (description)
 * created at 16/05/10
 *
 * @author yiding_he
 */
public class Range {

    public static final String PROVINCE = "province";

    public static final String CITY = "city";

    public static final String AREA = "area";

    public static final String SCHOOL = "school";

    public static final String CLASS = "class";

    public static final String STUDENT = "student";

    public static final Range PROVINCE_RANGE = province("430000");

    public static Range student(String student) {
        return new Range(Range.STUDENT, student);
    }

    public static Range clazz(String clazz) {
        return new Range(Range.CLASS, clazz);
    }

    public static Range school(String school) {
        return new Range(Range.SCHOOL, school);
    }

    public static Range area(String area) {
        return new Range(Range.AREA, area);
    }

    public static Range city(String city) {
        return new Range(Range.CITY, city);
    }

    public static Range province(String province) {
        return new Range(Range.PROVINCE, province);
    }

    public static Range student(String student, String name) {
        return new Range(Range.STUDENT, student, name);
    }

    public static Range clazz(String clazz, String name) {
        return new Range(Range.CLASS, clazz, name);
    }

    public static Range school(String school, String name) {
        return new Range(Range.SCHOOL, school, name);
    }

    public static Range area(String area, String name) {
        return new Range(Range.AREA, area, name);
    }

    public static Range city(String city, String name) {
        return new Range(Range.CITY, city, name);
    }

    public static Range province(String province, String name) {
        return new Range(Range.PROVINCE, province, name);
    }

    private String type;

    private String id;

    private String name;

    public Range() {
    }

    public Range(String type, String id) {
        this.type = type;
        this.id = id;
    }

    public Range(String type, String id, String name) {
        this.type = type;
        this.id = id;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    // 反序列化需要
    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    // 反序列化需要
    public void setId(String id) {
        this.id = id;
    }

    public boolean match(String rangeType) {
        return Objects.equals(this.type, rangeType);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Range range = (Range) o;

        if (!type.equals(range.type)) return false;
        if (!id.equals(range.id)) return false;
        return name != null ? name.equals(range.name) : range.name == null;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Range{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
