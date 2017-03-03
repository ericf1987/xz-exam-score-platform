package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetTask;
import org.springframework.stereotype.Component;

/**
 * Author: luckylo
 * Date : 2017-03-03
 */
@Component
public class TotalSchoolDetailSheet0 extends TotalSchoolDetailSheet {

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        SheetTask sheetTask = sheetContext.getSheetTask();

        Range range = sheetTask.getRange();
        String schoolId = range.getId();

        sheetContext.getProperties().put("schoolId", schoolId);

        generateTotalScoreRankSheet(sheetContext);
    }

    @Override
    protected String getSubjectId(SheetContext sheetContext) {
        return "000";
    }

    @Override
    protected String getSubjectName(SheetContext sheetContext) {
        return "总分";
    }

    @Override
    protected String getSchoolId(SheetContext sheetContext) {
        return sheetContext.getProperties().getString("schoolId");
    }
}
