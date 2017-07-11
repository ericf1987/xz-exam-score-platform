package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.exportexcel.SheetContext;
import org.springframework.stereotype.Component;

/**
 * 联考分数排名、得分明细表(全科)
 *
 * @author
 * @createTime 2017-07-10.
 */
@Component
public class TotalScoreRankSheet0 extends TotalScoreRankSheet {

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        generateTotalScoreRankSheet(sheetContext);
    }
}
