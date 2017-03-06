package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.ReportGenerator;
import com.xz.scorep.executor.exportexcel.SheetTask;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 分数段分布统计分析（总分）
 */
@Component
public class TotalSchoolSegmentReport extends ReportGenerator {

    @Override
    protected List<SheetTask> getSheetTasks(String projectId, Range range, Target target) {
        return Collections.singletonList(new SheetTask(
                "全部科目（总分）", ScoreDistributionSheet.class, range, target));
    }
}
