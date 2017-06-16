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


/**
 * 项目的学生总得分
 */
@Component
@AggregateOrder(2)
@AggregateTypes({AggregateType.Check, AggregateType.Quick})
public class StudentProjectScoreAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(StudentProjectScoreAggregator.class);


    private static final String UPDATE_PROJECT_SCORE = "update score_project set paper_score_type = " +
            "case " +
            "when student_id in (select a.student_id from (select student_id,COUNT(*) counts from lost GROUP BY student_id) a where a.counts = {{count}})then \"lost\"\n" +
            "when student_id in (select b.student_id from (select student_id,COUNT(*) counts from absent GROUP BY student_id) b where b.counts = {{count}})then \"absent\"\n" +
            "when student_id in (select c.student_id from (select student_id,COUNT(*) counts from cheat GROUP BY student_id) c where c.counts = {{count}})then \"cheat\"\n" +
            "else\"paper\" end";


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

        List<ExamSubject> subjects = AggregatorHelper.getSubjects(aggregateParameter, subjectService);

        subjects.stream()
                .filter(subject -> subject.getVirtualSubject().equals("false"))
                .forEach(subject -> {
                    String subjectId = subject.getId();
                    accumulateScore(projectDao, subjectId);
                    LOG.info("项目 {} 的学生总分统计已完成 {}/{} 个科目",
                            projectId, counter.incrementAndGet(), subjects.size());
                });

        //全科卷面0分,全科作弊,全科缺考才会进入
        updateProjectScore(subjects, projectDao);

    }

    private void updateProjectScore(List<ExamSubject> subjects, DAO projectDao) {
        int subjectCount = (int) subjects.stream()
                .filter(subject -> subject.getVirtualSubject().equals("false"))
                .count();
        String replace = UPDATE_PROJECT_SCORE.replace("{{count}}", String.valueOf(subjectCount));
        projectDao.execute(replace);
    }


    private void accumulateScore(DAO projectDao, String subjectId) {
        String tableName = "score_subject_" + subjectId;

        String combineSql = "update score_project p " +
                "  inner join " + tableName + " q using(student_id)" +
                "  set p.score=p.score+q.score";

        projectDao.execute(combineSql);
    }
}
