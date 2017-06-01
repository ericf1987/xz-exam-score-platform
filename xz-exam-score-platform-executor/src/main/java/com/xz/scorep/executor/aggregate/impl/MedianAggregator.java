package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 中位数(班级,学校,项目 的每个科目已经总分的中位数.)
 *
 * @author luckylo
 */
@Component
@AggregateTypes({AggregateType.Complete})
@AggragateOrder(71)
public class MedianAggregator extends Aggregator {

    private static Logger LOG = LoggerFactory.getLogger(MedianAggregator.class);

//            mysql calculate median
//            "SELECT avg(t1.score) as median_val FROM (\n"+
//            "SELECT @rownum:=@rownum+1 as `row_number`, d.score\n"+
//            "  FROM score_project d,  (SELECT @rownum:=0) r\n"+
//            "  WHERE 1\n"+
//            "  -- put some where clause here\n"+
//            "  ORDER BY d.score\n"+
//            ") as t1, \n"+
//            "(\n"+
//            "  SELECT count(*) as total_rows\n"+
//            "  FROM score_project d\n"+
//            "  WHERE 1\n"+
//            "  -- put same where clause here\n"+
//            ") as t2\n"+
//            "WHERE 1\n"+
//            "AND t1.row_number in ( floor((total_rows+1)/2), floor((total_rows+2)/2) );\n"+

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ClassService classService;

    @Autowired
    private SchoolService schoolService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        List<ExamSubject> subjects = subjectService.listSubjects(projectId);
        projectDao.execute("truncate table median");
        LOG.info("开始统计项目 ID {} 中位数....", projectId);
    }
}
