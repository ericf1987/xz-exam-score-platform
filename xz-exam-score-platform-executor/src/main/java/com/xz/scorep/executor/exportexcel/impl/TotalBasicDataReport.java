package com.xz.scorep.executor.exportexcel.impl;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.ReportGenerator;
import com.xz.scorep.executor.exportexcel.SheetTask;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author by fengye on 2016/6/24.
 * 总体成绩分析-基础数据-学生各科成绩明细
 */
@Component
public class TotalBasicDataReport  extends ReportGenerator {

    @Override
    protected List<SheetTask> getSheetTasks(String projectId, Range range) {
        List<SheetTask> tasks = new ArrayList<>();
        SheetTask projectTask = new SheetTask("全部科目", TotalBasicDataSheets.class);
        projectTask.setTarget(Target.project(projectId));
        tasks.add(projectTask);
        return tasks;
    }
}
