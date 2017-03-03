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

@Component
public class TotalSchoolSegmentReport extends ReportGenerator {

    @Autowired
    private SubjectService subjectService;

    @Override
    protected List<SheetTask> getSheetTasks(String projectId, Range range, Target target) {
        ArrayList<SheetTask> sheetTasks = new ArrayList<>();

        // 全部科目
        sheetTasks.add(new SheetTask("全部科目(总分）", TotalSchoolSegmentSheet0.class));

        // 每个科目
        List<SheetTask> subjectSheetTasks = subjectService.listSubjects(projectId)
                .stream()
                .map(subject -> {
                    SheetTask sheetTask = new SheetTask(subject.getName(), TotalSchoolSegmentSheet1.class);
                    sheetTask.setTarget(Target.subject(subject.getId(), subject.getName()));
                    return sheetTask;
                })
                .collect(Collectors.toList());

        sheetTasks.addAll(subjectSheetTasks);

        return sheetTasks;
    }


}
