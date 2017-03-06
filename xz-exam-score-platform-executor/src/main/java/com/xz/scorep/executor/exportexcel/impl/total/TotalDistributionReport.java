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
 * 联考学校分数分布统计
 */
@Component
public class TotalDistributionReport extends ReportGenerator {

    @Autowired
    private SubjectService subjectService;

    @Override
    protected List<SheetTask> getSheetTasks(String projectId, Range range, Target target) {
        ArrayList<SheetTask> sheetTasks = new ArrayList<>();

        // 全部科目
        sheetTasks.add(new SheetTask("全部科目(总分）",
                TotalDistributionSheet.class, Range.PROVINCE_RANGE, Target.project(projectId, "000")));

        // 每个科目
        List<SheetTask> subjectSheetTasks = subjectService.listSubjects(projectId)
                .stream()
                .map(subject -> {
                    SheetTask sheetTask = new SheetTask(subject.getName(), TotalDistributionSheet.class);
                    sheetTask.setRange(Range.PROVINCE_RANGE);
                    sheetTask.setTarget(Target.subject(subject.getId(), subject.getName()));
                    return sheetTask;
                })
                .collect(Collectors.toList());

        sheetTasks.addAll(subjectSheetTasks);

        return sheetTasks;
    }


}
