package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.exportexcel.SheetContext;
import org.springframework.stereotype.Component;

@Component
public class TotalDistributionSheet0 extends TotalDistributionSheet {

    protected String getTargetType(SheetContext sheetContext) {
        return "project";
    }

    protected String getSubjectName(SheetContext sheetContext) {
        return "总分";
    }

    protected String getTargetIdCondition(SheetContext sheetContext) {
        return "";
    }
}
