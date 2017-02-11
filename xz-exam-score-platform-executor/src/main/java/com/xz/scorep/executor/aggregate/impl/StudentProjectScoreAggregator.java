package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.aggregate.AggragateOrder;
import com.xz.scorep.executor.aggregate.Aggregator;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.project.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@AggragateOrder(1)
public class StudentProjectScoreAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(StudentProjectScoreAggregator.class);

    @Autowired
    private SubjectService subjectService;

    @Override
    public void aggregate(String projectId) throws Exception {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table score_project");
        projectDao.execute("insert into score_project(student_id,score) select id, 0 from student");
        LOG.info("项目 {} 的学生总分已清空", projectId);

        AtomicInteger counter = new AtomicInteger(0);
        List<String> subjectIds = subjectService.querySubjectIds(projectId)
                .stream().map(ExamSubject::getId).collect(Collectors.toList());

        subjectIds.forEach(subjectId -> {
            accumulateScore(projectDao, subjectId);
            LOG.info("项目 {} 的学生总分统计已完成 {}/{} 个科目",
                    projectId, counter.incrementAndGet(), subjectIds.size());
        });
    }

    private void accumulateScore(DAO projectDao, String subjectId) {
        String tableName = "score_subject_" + subjectId;

        String combineSql = "update score_project p \n" +
                "  left join " + tableName + " q on p.student_id=q.student_id\n" +
                "  set p.score=p.score+q.score";

        projectDao.execute(combineSql);
    }
}
