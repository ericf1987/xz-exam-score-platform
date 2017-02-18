package com.xz.scorep.executor.exportexcel;

import com.xz.ajiaedu.common.excel.ExcelWriter;
import com.xz.scorep.executor.utils.Direction;
import com.xz.scorep.executor.utils.Position;

/**
 * (description)
 * created at 2017/2/17
 *
 * @author yidin
 */
public class SheetHeaderBuilder {

    private Position position = new Position(0, 0);

    private int maxRowIndex = 0;

    private ExcelWriter excelWriter;

    public Position getPosition() {
        return position;
    }

    public void set(String text) {
        set(text, 1, 1);
    }

    public void set(String text, int rowspan, int colspan) {
        excelWriter.set(position.getRowIndex(), position.getColumnIndex(), text);

        if (rowspan > 1 || colspan > 1) {
            excelWriter.mergeCells(
                    position.getRowIndex(),
                    position.getColumnIndex(),
                    position.getRowIndex() + rowspan - 1,
                    position.getColumnIndex() + colspan - 1);
        }
    }

    public void move(Direction... directions) {
        for (Direction direction : directions) {
            move(direction);
        }
    }

    public void move(Direction direction) {
        if (direction == Direction.UP) {
            position.rowIndexAdd(-1);
        } else if (direction == Direction.RIGHT) {
            position.columnIndexAdd(1);
        } else if (direction == Direction.DOWN) {
            position.rowIndexAdd(1);
        } else if (direction == Direction.LEFT) {
            position.columnIndexAdd(-1);
        }

        this.maxRowIndex = Math.max(this.maxRowIndex, position.getRowIndex());
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

    public int getMaxRowIndex() {
        return maxRowIndex;
    }
}
