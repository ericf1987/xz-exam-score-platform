package com.xz.scorep.executor.exportexcel;

import com.xz.ajiaedu.common.excel.ExcelWriter;

/**
 * (description)
 * created at 2017/2/17
 *
 * @author yidin
 */
public class SheetHeaderBuilder {

    public enum Direction {UP, RIGHT, DOWN, LEFT;}

    private Position position = new Position(0, 0);

    private ExcelWriter excelWriter;

    public Position getPosition() {
        return position;
    }

    public void set(String text) {
        set(text, 1, 1);
    }

    public void set(String text, int rowspan, int colspan) {
        excelWriter.set(position.rowIndex, position.columnIndex, text);

        if (rowspan > 1 || colspan > 1) {
            excelWriter.mergeCells(position.rowIndex, position.columnIndex,
                    position.rowIndex + rowspan - 1, position.columnIndex + colspan - 1);
        }
    }

    public void move(Direction... directions) {
        for (Direction direction : directions) {
            move(direction);
        }
    }

    public void move(Direction direction) {
        if (direction == Direction.UP) {
            position.rowIndex--;
        } else if (direction == Direction.RIGHT) {
            position.columnIndex++;
        } else if (direction == Direction.DOWN) {
            position.rowIndex++;
        } else if (direction == Direction.LEFT) {
            position.columnIndex--;
        }
    }

    public void setAndMove(String text, Direction... directions) {
        set(text);
        move(directions);
    }

    public void setAndMove(String text, Direction direction) {
        set(text);
        move(direction);
    }

    public void setAndMove(String text, int rowspan, int colspan, Direction direction) {
        set(text, rowspan, colspan);
        move(direction);
    }

    public SheetHeaderBuilder(ExcelWriter excelWriter) {
        this.excelWriter = excelWriter;
    }

    //////////////////////////////////////////////////////////////

    public static class Position {

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
    }
}
