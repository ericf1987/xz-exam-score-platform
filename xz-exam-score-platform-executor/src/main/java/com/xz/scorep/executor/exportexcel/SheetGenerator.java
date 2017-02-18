package com.xz.scorep.executor.exportexcel;

import com.xz.ajiaedu.common.excel.ExcelWriter;
import com.xz.scorep.executor.bean.ExamProject;
import com.xz.scorep.executor.table.Table;
import com.xz.scorep.executor.table.TableRow;

/**
 * 生成报表文件中的单个 Sheet
 *
 * @author yiding_he
 */
public abstract class SheetGenerator {

    /**
     * 生成 sheet
     *
     * @param project     项目
     * @param excelWriter Excel 写入对象
     * @param sheetTask   任务对象（包含 range 和 target）
     */
    public void generate(ExamProject project, ExcelWriter excelWriter, SheetTask sheetTask) throws Exception {
        SheetContext sheetContext = new SheetContext();
        sheetContext.setProject(project);
        sheetContext.setExcelWriter(excelWriter);
        sheetContext.setSheetTask(sheetTask);
        generateSheet(sheetContext);
    }

    protected abstract void generateSheet(SheetContext sheetContext) throws Exception;

    protected void writeTableToSheet(ExcelWriter excelWriter, Table table, int startRow) {
        int[] rowCounter = {startRow};
        table.getRows().forEach(tableRow -> {
            writeRow(excelWriter, table, tableRow, rowCounter[0]);
            rowCounter[0]++;
        });
    }

    protected void writeRow(ExcelWriter excelWriter, Table table, TableRow tableRow, int rowIndex) {
        tableRow.entrySet().forEach(entry -> {
            int columnIndex = table.getColumnIndex(entry.getKey());
            if (columnIndex > -1) {
                excelWriter.set(rowIndex, columnIndex, entry.getValue());
            }
        });
    }
}
