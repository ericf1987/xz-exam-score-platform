package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.ReportGenerator;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.exportexcel.SheetTask;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 分数段分布统计分析（科目）
 */
@Component
public class TotalSchoolSubjectSegmentReport extends ReportGenerator {

    @Override
    protected List<SheetTask> getSheetTasks(String projectId, Range range, Target target) {
        String subjectName = target.getName();
        Class<? extends SheetGenerator> type = TotalSchoolSubjectSegmentSheet0.class;
        return Collections.singletonList(new SheetTask(subjectName, type, range, target));
    }
}
