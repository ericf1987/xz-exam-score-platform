package com.xz.scorep.executor.pss.bean;

import java.awt.*;

/**
 * @author by fengye on 2017/3/19.
 */
public class TextRect {

    private float coordinateX;

    private float coordinateY;

    private String textContent;

    private Font font;

    public TextRect() {

    }

    public float getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(float coordinateX) {
        this.coordinateX = coordinateX;
    }

    public float getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(float coordinateY) {
        this.coordinateY = coordinateY;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public TextRect(float coordinateX, float coordinateY, String textContent, Font font) {
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
        this.textContent = textContent;
        this.font = font;
    }

    public float getHorizenTailX(float offsetX) {
        return this.getCoordinateX() + this.getFont().getSize() * this.getTextContent().length() + offsetX;
    }

    public float getHorizenTailY(float offsetY) {
        return this.getCoordinateY() + this.getFont().getSize() + offsetY;
    }

    public float getVerticalTailX(float offsetX) {
        return this.getCoordinateX() + this.getFont().getSize() + offsetX;
    }

    public float getVerticalTailY(float offsetY) {
        return this.getCoordinateX() + this.getFont().getSize() * this.getTextContent().length() + offsetY;
    }

}
