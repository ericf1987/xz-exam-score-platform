package com.xz.scorep.executor.pss.bean;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author by fengye on 2017/3/15.
 */
public class ObjectiveQuestZone {

    private double totalScore;

    private int correctCount;

    private int totalCount;

    private double coordinateX;

    private double coordinateY;

    private List<String> errorQuests = new LinkedList<>();

    private List<TextRect> textRects = new LinkedList<>();

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public List<String> getErrorQuests() {
        return errorQuests;
    }

    public void setErrorQuests(List<String> errorQuests) {
        this.errorQuests = errorQuests;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
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

    public List<TextRect> getTextRects() {
        return textRects;
    }

    public void setTextRects(List<TextRect> textRects) {
        this.textRects = textRects;
    }

    /**
     * 绘制多行错题显示
     *
     * @param errorDesc   错误描述
     * @param errorDescX  错误描述原点X坐标
     * @param coordinateY 错误描述原点Y坐标
     * @param font        字体
     * @return 返回结果
     */
    public List<TextRect> getLines(List<String> errorDesc, double errorDescX, double coordinateY, Font font) {

        int fontSize = font.getSize();

        //剩余宽度中可以容纳的字符数
        int count = 10;

        int size = errorDesc.size();

        //行数
        int row = errorDesc.size() / count;

        List<TextRect> textRects = new LinkedList<>();
        for (int x = 0; x <= row; x++) {
            int tail = count * (x + 1);
            int lastIndex = tail > size ? size : tail;
            TextRect textRect = new TextRect((float) errorDescX, (float) (coordinateY + fontSize * x),
                    getErrorNoString(errorDesc.subList(count * x, lastIndex)), font);
            textRects.add(textRect);
        }

        return textRects;
    }

    public String getErrorNoString(List<String> errorQuests) {
        StringBuilder builder = new StringBuilder();
        if (null != errorQuests && !errorQuests.isEmpty()) {
            for (String errorNo : errorQuests) {
                builder.append(errorNo).append("、");
            }
            return builder.substring(0, builder.length() - 1);
        }
        return "";
    }
}
