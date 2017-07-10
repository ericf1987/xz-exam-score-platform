package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.ReportGenerator;
import com.xz.scorep.executor.exportexcel.SheetTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 联考分数排名、得分明细表(全科)
 *
 * @author luckylo
 * @createTime 2017-07-10.
 */
public class TotalScoreRankReport extends ReportGenerator {
    @Override
    protected List<SheetTask> getSheetTasks(String projectId, Range range, Target target) {
        List<SheetTask> list = new ArrayList<>();
        list.add(null);
        return list;
    }
}
