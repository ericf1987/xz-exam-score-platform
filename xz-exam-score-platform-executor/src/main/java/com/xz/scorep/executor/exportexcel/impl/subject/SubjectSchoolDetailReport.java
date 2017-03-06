package com.xz.scorep.executor.exportexcel.impl.subject;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.ReportGenerator;
import com.xz.scorep.executor.exportexcel.SheetTask;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SubjectSchoolDetailReport extends ReportGenerator {

    @Override
    protected List<SheetTask> getSheetTasks(String projectId, Range range, Target target) {

        SheetTask sheetTask = new SheetTask(
                "成绩排名", SubjectSchoolDetailSheet0.class, range, target);

        return Collections.singletonList(sheetTask);
    }
}
