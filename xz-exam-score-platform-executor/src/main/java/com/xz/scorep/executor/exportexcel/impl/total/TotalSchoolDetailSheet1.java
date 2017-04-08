package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.ReportCacheInitializer;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Author: luckylo
 * Date : 2017-03-03
 * 全校分数排名、得分明细表(每一个科目)
 */
@Component
public class TotalSchoolDetailSheet1 extends TotalSchoolDetailSheet {

    @Autowired
    ReportCacheInitializer reportCache;


    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        SheetTask sheetTask = sheetContext.getSheetTask();

        Target target = sheetTask.getTarget();
        String subjectId = String.valueOf(target.getId());
        String subjectName = target.getName();

        Range range = sheetTask.getRange();
        String schoolId = range.getId();

        sheetContext.getProperties().put("schoolId", schoolId);
        sheetContext.getProperties().put("subjectId", subjectId);
        sheetContext.getProperties().put("subjectName", subjectName);

        generateEachSubjectSheet(sheetContext, reportCache);
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
