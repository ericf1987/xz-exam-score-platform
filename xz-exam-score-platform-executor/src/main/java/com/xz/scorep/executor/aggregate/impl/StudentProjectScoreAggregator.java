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

        //删除项目总分为0分,且没有缺考没有作弊记录的学生成绩
        if (Boolean.valueOf(reportConfig.getRemoveZeroScores())) {
            removeZeroScores(projectId);
        }

        //根据报表配置删除全科缺考考生记录
        if (Boolean.valueOf(reportConfig.getRemoveAbsentStudent())) {
            removeAbsentStudent(projectDao, subjects);
        }

        //根据报表配置删除全科作弊考生记录
        if (Boolean.valueOf(reportConfig.getRemoveCheatStudent())) {
            removeCheatStudent(projectDao, subjects);
        }


    }

    private void removeCheatStudent(DAO projectDao, List<ExamSubject> subjects) {
        //学生作弊的科目数,  当学生作弊的科目数等于参考科目数,该学生为全科作弊
        String query = "select student_id,COUNT(*) counts from cheat  GROUP BY student_id ";
        int subjectCount = (int) subjects.stream()
                .filter(subject -> subject.getVirtualSubject().equals("false"))
                .count();

        List<String> studentList = projectDao.query(query).stream()
                .filter(row -> row.getInteger("counts", 0) == subjectCount)
                .map(row -> "\"" + row.getString("student_id") + "\"")
                .collect(Collectors.toList());

        if (studentList == null) {
            return;
        }

        String students = String.join(",", studentList);

        LOG.info("删除全科作弊的考生.... ");
        String sql = "delete from score_project where student_id in ({{condition}})";
        projectDao.execute(sql.replace("{{condition}}", students));
        LOG.info("项目的全科作弊学生删除完毕.....");
    }

    private void removeZeroScores(String projectId) {
        //删除项目总分为0分,且没有缺考没有作弊记录的学生成绩
        String sql = "delete from score_project where score=0 and student_id not in(" +
                "select student_id from absent \n" +
                "UNION \n" +
                "select student_id from cheat ) ";
        LOG.info("删除项目缺考、作弊外的零分记录...");
        daoFactory.getProjectDao(projectId).execute(sql);
        LOG.info("项目 {} 的缺考、作弊外的零分记录删除完毕。", projectId);
    }

    private void removeAbsentStudent(DAO projectDao, List<ExamSubject> subjects) {
        //学生缺考的科目数,  当学生缺考的科目数等于参考科目数,该学生为全科缺考
        String query = "select student_id,COUNT(*) counts from absent  GROUP BY student_id ";
        int subjectCount = (int) subjects.stream()
                .filter(subject -> subject.getVirtualSubject().equals("false"))
                .count();

        List<String> studentList = projectDao.query(query).stream()
                .filter(row -> row.getInteger("counts", 0) == subjectCount)
                .map(row -> "\"" + row.getString("student_id") + "\"")
                .collect(Collectors.toList());

        if (studentList == null) {
            return;
        }

        String students = String.join(",", studentList);
        LOG.info("删除全科缺考的考生记录....");
        String sql = "delete from score_project where student_id in ({{condition}})";
        projectDao.execute(sql.replace("{{condition}}", students));
        LOG.info("项目的全科缺考学生删除完毕.....");
    }


    private void accumulateScore(DAO projectDao, String subjectId) {
        String tableName = "score_subject_" + subjectId;

        String combineSql = "update score_project p " +
                "  inner join " + tableName + " q using(student_id)" +
                "  set p.score=p.score+q.score";

        projectDao.execute(combineSql);
    }
}
