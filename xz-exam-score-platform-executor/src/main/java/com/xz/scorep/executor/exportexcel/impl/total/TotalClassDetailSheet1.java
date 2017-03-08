package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetTask;
import org.springframework.stereotype.Component;

/**
 * Author: luckylo
 * Date : 2017-03-06
 * 分数排名、得分明细表(每个科目)
 */
@Component
public class TotalClassDetailSheet1 extends TotalClassDetailSheet {

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {

        SheetTask sheetTask = sheetContext.getSheetTask();

        Target target = sheetTask.getTarget();
        String subjectId = String.valueOf(target.getId());

        Range range = sheetTask.getRange();

        String classId = range.getId();
        sheetContext.getProperties().put("classId", classId);
        sheetContext.getProperties().put("subjectId", subjectId);

        generateEachSubjectSheet(sheetContext);
    }

    @Override
    protected String getSubjectId(SheetContext sheetContext) {
        return sheetContext.getProperties().getString("subjectId");
    }

    @Override
    protected String getClassId(SheetContext sheetContext) {
        return sheetContext.getProperties().getString("classId");
    }
}
