package com.xz.scorep.executor.exportexcel.impl.subject;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.ReportGenerator;
import com.xz.scorep.executor.exportexcel.SheetTask;
import com.xz.scorep.executor.exportexcel.impl.total.TotalSchoolSubjectAverageSheet;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Author: luckylo
 * Date : 2017-03-08
 */
@Component
public class SubjectSchoolAverageReport extends ReportGenerator {
    @Override
    protected List<SheetTask> getSheetTasks(String projectId, Range range, Target target) {
        return Collections.singletonList(new SheetTask(
                "平均分及三率", TotalSchoolSubjectAverageSheet.class, range, target));
    }
}
