package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.exportexcel.SheetContext;
import org.springframework.stereotype.Component;

/**
 * @author luckylo
 * @createTime 2017-07-10.
 */
@Component
public class TotalScoreRankSheet1 extends TotalScoreRankSheet {

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        generateEachSubjectSheet(sheetContext);
    }
}
