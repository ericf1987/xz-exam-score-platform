package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.ReportGenerator;
import com.xz.scorep.executor.exportexcel.SheetTask;
import com.xz.scorep.executor.project.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: luckylo
 * Date : 2017-03-03
 * 全校分数排名、得分明细表(全科)
 */
@Component
public class TotalSchoolDetailReport extends ReportGenerator {

    @Autowired
    SubjectService subjectService;

    @Override
    protected List<SheetTask> getSheetTasks(String projectId, Range range, Target target) {
        List<SheetTask> sheetTasks = new ArrayList<>();
        SheetTask task = new SheetTask("总成绩排名", TotalSchoolDetailSheet0.class);
        task.setRange(range);
        task.setTarget(target);
        sheetTasks.add(task);

        //每个科目
        List<SheetTask> subjectSheetTasks = subjectService.listSubjects(projectId)
                .stream()
                .map(subject -> {
                    SheetTask sheetTask = new SheetTask(subject.getName(),TotalSchoolDetailSheet1.class);
                    sheetTask.setRange(range);
                    sheetTask.setTarget(Target.subject(subject.getId(), subject.getName()));
                    return sheetTask;
                })
                .collect(Collectors.toList());

        sheetTasks.addAll(subjectSheetTasks);
        return sheetTasks;
    }
}
