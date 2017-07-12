package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetTask;
import org.springframework.stereotype.Component;

/**
 * @author luckylo
 * @createTime 2017-07-10.
 */
@Component
public class TotalScoreRankSheet1 extends TotalScoreRankSheet {

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        SheetTask sheetTask = sheetContext.getSheetTask();

        Target target = sheetTask.getTarget();
        String subjectId = String.valueOf(target.getId());
        String subjectName = target.getName();

        sheetContext.getProperties().put("subjectId", subjectId);
        sheetContext.getProperties().put("subjectName", subjectName);

        generateEachSubjectSheet(sheetContext);
    }

    @Override
    protected String getSchoolId(SheetContext sheetContext) {
        return sheetContext.getProperties().getString("schoolId");
    }

    @Override
    protected String getSubjectId(SheetContext sheetContext) {
        return sheetContext.getProperties().getString("subjectId");
    }

    @Override
    protected String getSubjectName(SheetContext sheetContext) {
        return sheetContext.getProperties().getString("subjectName");
    }
}
