package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
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

    @Autowired
    private ReportConfigService reportConfigService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
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

        //每一个科目移除缺考,总分不可能含有缺考
        //removeAbsent(projectDao, subjectIds);

        //移除总分为0
        if (Boolean.valueOf(reportConfig.getRemoveZeroScores())) {
            removeZeroScores(projectId);
        }
    }

    private void removeZeroScores(String projectId) {
        String sql = "delete from score_project where score=0";
        LOG.info("删除项目零分记录...");
        daoFactory.getProjectDao(projectId).execute(sql);
        LOG.info("项目 {} 的总分零分记录删除完毕。", projectId);
    }

    private void removeAbsent(DAO projectDao, List<String> subjectIds) {
        String where = String.join(" and ", subjectIds.stream().map(
                subjectId -> "student_id not in (select student_id from score_subject_" + subjectId + ")")
                .collect(Collectors.toList()));

        String sql = "delete from score_project where " + where;
        LOG.info("删除全科缺考的考生: " + sql);
        projectDao.execute(sql);
    }


    private void accumulateScore(DAO projectDao, String subjectId) {
        String tableName = "score_subject_" + subjectId;

        String combineSql = "update score_project p " +
                "  inner join " + tableName + " q using(student_id)" +
                "  set p.score=p.score+q.score";

        projectDao.execute(combineSql);
    }
}
