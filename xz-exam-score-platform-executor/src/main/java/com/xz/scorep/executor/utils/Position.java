package com.xz.scorep.executor.utils;

/**
 * (description)
 * created at 17/02/18
 *
 * @author yidin
 */
public class Position {

    private int rowIndex;

    private int columnIndex;

    public Position() {
    }

    public Position(int rowIndex, int columnIndex) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public void rowIndexAdd(int i) {
        this.rowIndex += i;
    }

    public void columnIndexAdd(int i) {
        this.columnIndex += i;
    }
}
