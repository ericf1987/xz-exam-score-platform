package com.xz.scorep.executor.exportexcel.impl.subject;

import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.ReportGenerator;
import com.xz.scorep.executor.exportexcel.SheetTask;

import java.util.Collections;
import java.util.List;

/**
 * (description)
 * created at 2017/3/1
 *
 * @author yidin
 */
public class SubjectSchoolDetailReport extends ReportGenerator {

    @Override
    protected List<SheetTask> getSheetTasks(String projectId, Range range, Target target) {
        String schoolId = range.getId();
        String schoolName = range.getName();
        String subjectId = String.valueOf(target.getId());
        String subjectName = target.getName();

        return Collections.emptyList();
    }
}
