package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.ReportGenerator;
import com.xz.scorep.executor.exportexcel.SheetTask;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: luckylo
 * Date : 2017-03-06
 */
@Component
public class TotalSchoolAverageReport extends ReportGenerator {
    @Override
    protected List<SheetTask> getSheetTasks(String projectId, Range range, Target target) {
        List<SheetTask> sheetTasks = new ArrayList<>();
        SheetTask task = new SheetTask("平均分及三率",TotalSchoolAverageSheet.class);
        task.setRange(range);
        sheetTasks.add(task);
        return sheetTasks;
    }
}
