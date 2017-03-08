package com.xz.scorep.executor.exportexcel.impl.subject;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.ReportGenerator;
import com.xz.scorep.executor.exportexcel.SheetTask;
import com.xz.scorep.executor.exportexcel.impl.total.TotalSchoolSubjectObjectiveSheet0;
import com.xz.scorep.executor.exportexcel.impl.total.TotalSchoolSubjectObjectiveSheet1;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: luckylo
 * Date : 2017-03-08
 * 主、客观题分析（班级）
 */
@Component
public class SubjectSchoolObjectiveReport extends ReportGenerator {
    @Override
    protected List<SheetTask> getSheetTasks(String projectId, Range range, Target target) {
        List<SheetTask> list = new ArrayList<>();
        SheetTask task0 = new SheetTask("客观题分析", TotalSchoolSubjectObjectiveSheet0.class, range, target);
        SheetTask task1 = new SheetTask("主观题分析", TotalSchoolSubjectObjectiveSheet1.class, range, target);

        list.add(task0);
        list.add(task1);
        return list;
    }
}
