package com.xz.scorep.executor.exportexcel.impl.subject;

import com.xz.scorep.executor.aggritems.StudentQuery;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.project.QuestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubjectClassDetailSheet0 extends SheetGenerator {

    @Autowired
    private StudentQuery studentQuery;

    @Autowired
    private QuestService questService;

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        SubjectSchoolDetailSheet0.generateSheet0(sheetContext, studentQuery, questService);
    }
}
