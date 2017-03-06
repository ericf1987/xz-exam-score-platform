package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetTask;
import org.springframework.stereotype.Component;

/**
 * Author: luckylo
 * Date : 2017-03-06
 */
@Component
public class TotalClassDetailSheet0 extends TotalClassDetailSheet {
    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        SheetTask sheetTask = sheetContext.getSheetTask();

        Range range = sheetTask.getRange();
        String classId = range.getId();

        sheetContext.getProperties().put("classId", classId);

        generateTotalSheet(sheetContext);
    }

    @Override
    protected String getSubjectId(SheetContext sheetContext) {
        return "000";
    }

    @Override
    protected String getClassId(SheetContext sheetContext) {
        return sheetContext.getProperties().getString("classId");
    }
}
