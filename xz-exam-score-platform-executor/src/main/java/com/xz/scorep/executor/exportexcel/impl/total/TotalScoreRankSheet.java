package com.xz.scorep.executor.exportexcel.impl.total;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.aggritems.StudentQuery;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.ReportCacheInitializer;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.exportexcel.impl.subject.SheetContextHelper;
import com.xz.scorep.executor.exportexcel.impl.subject.SubjectSchoolDetailSheet0;
import com.xz.scorep.executor.project.QuestService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author luckylo
 * @createTime 2017-07-10.
 */
public abstract class TotalScoreRankSheet extends SheetGenerator {

    @Autowired
    StudentQuery studentQuery;

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    QuestService questService;

    @Autowired
    ReportCacheInitializer reportCache;


    //联考总分,科目得分排名
    protected void generateTotalScoreRankSheet(SheetContext sheetContext) {
        String projectId = sheetContext.getProjectId();
        sheetContext.tableSetKey("student_id");
        SheetContextHelper.fillStudentBasicInfo(sheetContext, studentQuery);
        AtomicInteger count = new AtomicInteger(5);
        DAO dao = daoFactory.getProjectDao(projectId);

    }


    //联考单科成绩得分明细
    protected void generateEachSubjectSheet(SheetContext sheetContext) {
        SubjectSchoolDetailSheet0.generateSheet0(sheetContext, studentQuery, questService, reportCache);
    }

    private void totalScoreRank(DAO dao, SheetContext sheetContext, AtomicInteger colIndex) {
        String projectId = sheetContext.getProjectId();

    }

}
