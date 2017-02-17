package com.xz.scorep.executor.bean;

/**
 * 知识点-能力层级组合
 * created at 16/06/28
 *
 * @author yiding_he
 */
public class PointLevel {

    private String point;

    private String level;

    public PointLevel() {
    }

    public PointLevel(String point, String level) {
        this.point = point;
        this.level = level;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PointLevel that = (PointLevel) o;

        if (point != null ? !point.equals(that.point) : that.point != null) return false;
        return level != null ? level.equals(that.level) : that.level == null;
    }

    @Override
    public int hashCode() {
        int result = point != null ? point.hashCode() : 0;
        result = 31 * result + (level != null ? level.hashCode() : 0);
        return result;
    }
}
