package com.xz.scorep.executor.pss.bean;

import java.util.LinkedList;
import java.util.List;

/**
 * @author by fengye on 2017/3/1.
 */
public class SubjectiveQuestZone {
    private String questNo;
    private double coordinateX;
    private double coordinateY;
    private double height;
    private double width;
    private int pageIndex;
    private double fullScore;
    private double score;

    private String paper_positive;
    private String paper_reverse;

    private List<TextRect> textRects = new LinkedList<>();

    public String getQuestNo() {
        return questNo;
    }

    public void setQuestNo(String questNo) {
        this.questNo = questNo;
    }

    public double getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(double coordinateX) {
        this.coordinateX = coordinateX;
    }

    public double getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(double coordinateY) {
        this.coordinateY = coordinateY;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public String getPaper_positive() {
        return paper_positive;
    }

    public void setPaper_positive(String paper_positive) {
        this.paper_positive = paper_positive;
    }

    public String getPaper_reverse() {
        return paper_reverse;
    }

    public void setPaper_reverse(String paper_reverse) {
        this.paper_reverse = paper_reverse;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public double getFullScore() {
        return fullScore;
    }

    public void setFullScore(double fullScore) {
        this.fullScore = fullScore;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public List<TextRect> getTextRects() {
        return textRects;
    }

    public void setTextRects(List<TextRect> textRects) {
        this.textRects = textRects;
    }

    public SubjectiveQuestZone() {
    }

    public SubjectiveQuestZone(String questNo, double coordinateX, double coordinateY, double height, double width, int pageIndex, double fullScore, double score, String paper_positive, String paper_reverse, List<TextRect> textRects) {
        this.questNo = questNo;
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
        this.height = height;
        this.width = width;
        this.pageIndex = pageIndex;
        this.fullScore = fullScore;
        this.score = score;
        this.paper_positive = paper_positive;
        this.paper_reverse = paper_reverse;
        this.textRects = textRects;
    }

    @Override
    public String toString() {
        return "Rect{" +
                "questNo='" + questNo + '\'' +
                ", coordinateX=" + coordinateX +
                ", coordinateY=" + coordinateY +
                ", height=" + height +
                ", width=" + width +
                ", pageIndex=" + pageIndex +
                ", fullScore=" + fullScore +
                ", score=" + score +
                ", paper_positive='" + paper_positive + '\'' +
                ", paper_reverse='" + paper_reverse + '\'' +
                '}';
    }
}
