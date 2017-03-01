package com.xz.scorep.executor.exportexcel.impl.subject;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.ReportGenerator;
import com.xz.scorep.executor.exportexcel.SheetTask;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SubjectClassDetailReport extends ReportGenerator {

    @Override
    protected List<SheetTask> getSheetTasks(String projectId, Range range, Target target) {

        SheetTask sheetTask = new SheetTask("成绩排名", SubjectClassDetailSheet0.class);
        sheetTask.setRange(range);
        sheetTask.setTarget(target);

        return Collections.singletonList(sheetTask);
    }
}
