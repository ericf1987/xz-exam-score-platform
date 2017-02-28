package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.SheetContext;
import org.springframework.stereotype.Component;

@Component
public class TotalDistributionSheet1 extends TotalDistributionSheet {

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        Target target = sheetContext.getSheetTask().getTarget();
        String subjectId = String.valueOf(target.getId());
        String subjectName = target.getName();

        sheetContext.getProperties().put("subjectId", subjectId);
        sheetContext.getProperties().put("subjectName", subjectName);

        generateSheet0(sheetContext);
    }

    @Override
    protected String getTargetType(SheetContext sheetContext) {
        return "subject";
    }

    @Override
    protected String getSubjectName(SheetContext sheetContext) {
        return sheetContext.getProperties().get("subjectName");
    }

    @Override
    protected String getTargetIdCondition(SheetContext sheetContext) {
        return " and target_id='" + sheetContext.getProperties().get("subjectId") + "'";
    }
}
