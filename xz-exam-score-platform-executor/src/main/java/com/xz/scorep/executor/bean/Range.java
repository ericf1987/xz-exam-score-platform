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

    private String name;

    private String id;

    public Range() {
    }

    public Range(String name, String id) {
        this.name = name;
        this.id = id;
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

    public boolean match(String range) {
        return Objects.equals(this.name, range);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Range range = (Range) o;

        if (!name.equals(range.name)) return false;
        return id.equals(range.id);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Range{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
