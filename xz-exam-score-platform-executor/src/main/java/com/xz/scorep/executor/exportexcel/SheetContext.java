package com.xz.scorep.executor.exportexcel;

import com.xz.ajiaedu.common.excel.ExcelWriter;
import com.xz.scorep.executor.bean.ExamProject;

/**
 * 生成 Sheet 的上下文，提供所有需要的方法
 */
public class SheetContext {

    private ExamProject project;

    private ExcelWriter excelWriter;

    private SheetTask sheetTask;

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
    }

    public SheetTask getSheetTask() {

        return sheetTask;
    }

    public void setSheetTask(SheetTask sheetTask) {

        this.sheetTask = sheetTask;
    }
}
