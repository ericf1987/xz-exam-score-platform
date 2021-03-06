package com.xz.scorep.executor.exportexcel;

import com.xz.ajiaedu.common.excel.ExcelWriter;
import com.xz.ajiaedu.common.lang.CollectionUtils;
import com.xz.ajiaedu.common.lang.Context;
import com.xz.scorep.executor.bean.ExamProject;
import com.xz.scorep.executor.table.Table;
import com.xz.scorep.executor.table.TableRow;
import com.xz.scorep.executor.utils.Direction;
import com.xz.scorep.executor.utils.Position;
import org.apache.poi.ss.usermodel.CellStyle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 生成 Sheet 的上下文，提供所有需要的方法
 */
public class SheetContext {

    private ExamProject project;

    private ExcelWriter excelWriter;

    private SheetTask sheetTask;

    private Table table = new Table();

    private SheetHeaderBuilder sheetHeaderBuilder;

    private Map<Integer, String> columnStyles = new HashMap<>();

    private Map<String, String> rowStyles = new HashMap<>();

    private Context properties = new Context();

    public Context getProperties() {
        return properties;
    }

    public void setProperties(Context properties) {
        this.properties = properties;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public ExamProject getProject() {
        return project;
    }

    public void setProject(ExamProject project) {
        this.project = project;
    }

    public ExcelWriter getExcelWriter() {
        return excelWriter;
    }

    public void setExcelWriter(ExcelWriter excelWriter) {
        this.excelWriter = excelWriter;
        this.sheetHeaderBuilder = new SheetHeaderBuilder(excelWriter);
    }

    public SheetTask getSheetTask() {
        return sheetTask;
    }

    public void setSheetTask(SheetTask sheetTask) {
        this.sheetTask = sheetTask;
    }

    //////////////////////////////////////////////////////////////
    // SheetContext 提供一系列方便写入 Excel 的方法，其中以 column 开头的方法
    // 表示设置列的名字和位置；以 header 开头的方法表示设置表头的内容，支持多行/多
    // 列和单元格合并；以 row 开头的方法表示填充记录、对记录进行排序，以及设置第一条
    // 记录的行位置。

    public void columnSet(int columnIndex, String columnName) {
        this.table.setColumnIndex(columnIndex, columnName);
    }

    public String getProjectId() {
        return this.project == null ? null : this.project.getId();
    }

    public void headerPut(String text) {
        this.sheetHeaderBuilder.set(text);
    }

    public void headerPut(String text, int rowspan, int colspan) {
        this.sheetHeaderBuilder.set(text, rowspan, colspan);
    }

    public void headerMove(Direction... directions) {
        this.sheetHeaderBuilder.move(directions);
    }

    public Position headerPosition() {
        return this.sheetHeaderBuilder.getPosition();
    }

    public void rowAdd(List<Map<String, Object>> rows) {
        this.table.readRows(rows);
    }

    public void rowAdd(Map<String, Object> row) {
        this.table.readRow(row);
    }

    public void rowSortBy(String... colNames) {
        this.table.sortBy(colNames);
    }

    public void tablePutValue(String key, String columnName, Object value) {
        this.table.setValue(key, columnName, value);
    }

    private int startRow = 0;

    public void rowStartPosition(int rowIndex) {
        this.startRow = rowIndex;
    }

    public void saveData() {

        if (this.startRow == 0) {
            this.startRow = this.sheetHeaderBuilder.getMaxRowIndex() + 1;
        }

        int[] rowCounter = {this.startRow};
        table.getRows().forEach(tableRow -> {
            writeRow(excelWriter, table, tableRow, rowCounter[0]);
            rowCounter[0]++;
        });
    }

    private void writeRow(ExcelWriter excelWriter, Table table, TableRow tableRow, int rowIndex) {
        CellStyle defaultStyle = excelWriter.getWorkbook().getCellStyleAt(0);

        tableRow.entrySet().forEach(entry -> {
            int columnIndex = table.getColumnIndex(entry.getKey());
            if (columnIndex > -1) {
                excelWriter.set(rowIndex, columnIndex, entry.getValue());

                // 设置缺省样式
                excelWriter.getOrCreateCell(rowIndex, columnIndex).setCellStyle(defaultStyle);

                // 检查并设置列样式
                if (columnStyles.containsKey(columnIndex)) {
                    String styleName = columnStyles.get(columnIndex);
                    excelWriter.setCellStyle(rowIndex, columnIndex, styleName);
                }

                // 检查并设置行样式（将覆盖列样式）
                String tableKeyValue = String.valueOf(tableRow.get(table.getKey()));
                if (rowStyles.containsKey(tableKeyValue)) {
                    String styleName = rowStyles.get(tableKeyValue);
                    excelWriter.setCellStyle(rowIndex, columnIndex, styleName);
                }
            }
        });
    }

    public void tableSetKey(String keyName) {
        this.table.setKey(keyName);
    }

    public void columnWidth(int columnIndex, int widthEm) {
        excelWriter.setWidth(columnIndex, widthEm);
    }

    public void columnStyle(int columnIndex, String styleName) {
        columnStyles.put(columnIndex, styleName);
    }

    public void rowStyle(String key, String styleName) {
        rowStyles.put(key, styleName);
    }

    public void freeze(int rowIndex, int colIndex) {
        excelWriter.getCurrentSheet().createFreezePane(colIndex, rowIndex);
    }

    public void fillEmptyCells(Predicate<String> columnPicker, Object emptyValue) {
        List<String> columns = CollectionUtils.filter(table.getColumnNames(), columnPicker::test);
        table.getRows().forEach(tableRow -> {
            for (String column : columns) {
                if (!tableRow.containsKey(column)) {
                    tableRow.put(column, emptyValue);
                }
            }
        });
    }
}
