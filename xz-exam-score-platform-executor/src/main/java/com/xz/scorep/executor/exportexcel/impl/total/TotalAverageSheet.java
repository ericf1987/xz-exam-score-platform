package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import org.springframework.stereotype.Component;

/**
 * Author: luckylo
 * Date : 2017-03-02
 */
@Component
public abstract class TotalAverageSheet extends SheetGenerator {

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {

    }




    protected abstract String[] getTableHeader();

    protected abstract String getSubjectName(SheetContext sheetContext);
}
