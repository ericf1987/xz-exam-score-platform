package com.xz.scorep.executor.exportexcel.impl.subject;

import com.xz.scorep.executor.aggritems.StudentQuery;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetTask;
import com.xz.scorep.executor.utils.Direction;

/**
 * (description)
 * created at 2017/3/3
 *
 * @author yidin
 */
public class SheetContextHelper {

    public static void fillStudentBasicInfo(SheetContext sheetContext, StudentQuery studentQuery) {
        String projectId = sheetContext.getProjectId();
        SheetTask sheetTask = sheetContext.getSheetTask();
        Range range = sheetTask.getRange();

        sheetContext.headerPut("学校名称", 2, 1);
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("班级", 2, 1);
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("姓名", 2, 1);
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("A佳考号", 2, 1);
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("学校考号", 2, 1);
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.columnSet(0, "school_name");
        sheetContext.columnWidth(0, 20);
        sheetContext.columnSet(1, "class_name");
        sheetContext.columnSet(2, "student_name");
        sheetContext.columnSet(3, "exam_no");
        sheetContext.columnWidth(3, 12);
        sheetContext.columnSet(4, "school_exam_no");
        sheetContext.columnWidth(4, 14);

        sheetContext.rowAdd(studentQuery.listStudentInfo(projectId, range));
    }
}
