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
 * 联考分数排名、得分明细表(全科)
 *
 * @author luckylo
 * @createTime 2017-07-10.
 */
@Component
public class TotalScoreRankReport extends ReportGenerator {

    @Autowired
    private SubjectService subjectService;

    @Override
    protected List<SheetTask> getSheetTasks(String projectId, Range range, Target target) {
        List<SheetTask> tasks = new ArrayList<>();
        SheetTask task = new SheetTask("总成绩排名", TotalSchoolDetailSheet0.class);
        task.setRange(Range.PROVINCE_RANGE);
        task.setTarget(target);
        tasks.add(task);
        //每个科目
        List<SheetTask> subjectSheetTasks = subjectService.listSubjects(projectId)
                .stream()
                .map(subject -> {
                    SheetTask sheetTask = new SheetTask(subject.getName(), TotalSchoolDetailSheet1.class);
                    sheetTask.setRange(Range.PROVINCE_RANGE);
                    sheetTask.setTarget(Target.subject(subject.getId(), subject.getName()));
                    return sheetTask;
                })
                .collect(Collectors.toList());

        tasks.addAll(subjectSheetTasks);
        return tasks;
    }
}
