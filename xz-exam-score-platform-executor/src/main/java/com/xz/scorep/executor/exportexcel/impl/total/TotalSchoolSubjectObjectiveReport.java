package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.ReportGenerator;
import com.xz.scorep.executor.exportexcel.SheetTask;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class TotalSchoolSubjectObjectiveReport extends ReportGenerator {

    @Override
    protected List<SheetTask> getSheetTasks(String projectId, Range range, Target target) {

        return Arrays.asList(
                new SheetTask("客观题分析", TotalSchoolSubjectObjectiveSheet0.class, range, target),
                new SheetTask("主观题分析", TotalSchoolSubjectObjectiveSheet1.class, range, target)
        );
    }
}
