package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.aggregate.*;
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
@AggregateTypes({AggregateType.Basic, AggregateType.Quick})
public class StudentProjectScoreAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(StudentProjectScoreAggregator.class);

    @Autowired
    private SubjectService subjectService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table score_project");
        projectDao.execute("insert into score_project(student_id,score) select id, 0 from student");
        LOG.info("项目 {} 的学生总分已清空", projectId);

        AtomicInteger counter = new AtomicInteger(0);

        List<String> subjectIds = getSubjectsId(aggregateParameter);

        subjectIds.forEach(subjectId -> {
            accumulateScore(projectDao, subjectId);
            LOG.info("项目 {} 的学生总分统计已完成 {}/{} 个科目",
                    projectId, counter.incrementAndGet(), subjectIds.size());
        });

        LOG.info("删除全科缺考的考生...");

        String where = String.join(" and ", subjectIds.stream().map(
                subjectId -> "student_id not in (select student_id from score_subject_" + subjectId + ")")
                .collect(Collectors.toList()));

        String sql = "delete from score_project where " + where;
        projectDao.execute(sql);
    }


    private List<String> getSubjectsId(AggregateParameter aggregateParameter) {
        String projectId = aggregateParameter.getProjectId();
        List<String> subjectId;

        if (aggregateParameter.getSubjects().isEmpty()) {
            subjectId = subjectService.listSubjects(projectId)
                    .stream().map(ExamSubject::getId)
                    .collect(Collectors.toList());

        } else {
            subjectId = aggregateParameter.getSubjects();
        }
        return subjectId;
    }


    private void accumulateScore(DAO projectDao, String subjectId) {
        String tableName = "score_subject_" + subjectId;

        String combineSql = "update score_project p " +
                "  inner join " + tableName + " q using(student_id)" +
                "  set p.score=p.score+q.score";

        projectDao.execute(combineSql);
    }
}
