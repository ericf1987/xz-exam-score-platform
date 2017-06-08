package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 删除统计中需排除的情况(主客观题得分,科目得分,项目总分)
 *
 * @author luckylo
 * @createTime 2017-06-08.
 */
@Component
@AggregateTypes(AggregateType.Quick)
@AggragateOrder(3)
public class DeleteExceptionConditionAggregator extends Aggregator {

    private static Logger LOG = LoggerFactory.getLogger(DeleteExceptionConditionAggregator.class);


    //只删除卷面0分(科目)
    private static final String DEL_SUBJECT_ZERO_SCORE = "delete from `{{tableName}}` where score=0 and paper_score_type = \"paper\"";

    //删除主客观题卷面为0分(该科目总得分为0分的学生)
    private static final String DEL_ZERO_SCORE = "delete from `{{tableName}}` where student_id in (" +
            "select student_id from `score_subject_{{subjectId}}` where score = 0 ) and score=0 and paper_score_type = \"paper\"";

    private static final String DEL_ABS_SCORE = "delete from `{{tableName}}` where paper_score_type = \"absent\"";

    private static final String DEL_LOST_SCORE = "delete from `{{tableName}}` where paper_score_type = \"lost\"";

    private static final String DEL_CHEAT_SCORE = "delete from `{{tableName}}` where paper_score_type = \"cheat\"";


    @Autowired
    private SubjectService subjectService;

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private ReportConfigService reportConfigService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);
        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);

        List<ExamSubject> subjects = subjectService.listSubjects(projectId);

        LOG.info("开始清除项目 ID {} 0分,缺考,作弊学生", projectId);

        subjects.forEach(subject -> removeSubject(projectId, projectDao, subject, reportConfig));

        removeProject(projectId, projectDao, subjects, reportConfig);

        LOG.info("项目 ID {} 0分,缺考,作弊学生清除完毕.....", projectId);
    }

    //根据配置剔除0分,缺考,作弊等学生
    private void removeProject(String projectId, DAO projectDao, List<ExamSubject> subjects, ReportConfig reportConfig) {
        //删除项目总分为0分,且没有缺考没有作弊记录的学生成绩
        if (Boolean.valueOf(reportConfig.getRemoveZeroScores())) {
            removeProjectZeroScores(projectId, subjects);
        }

        //根据报表配置删除全科缺考考生记录
        if (Boolean.valueOf(reportConfig.getRemoveAbsentStudent())) {
            removeProjectAbsentStudent(projectDao, subjects);
        }

        //根据报表配置删除全科作弊考生记录
        if (Boolean.valueOf(reportConfig.getRemoveCheatStudent())) {
            removeProjectCheatStudent(projectDao, subjects);
        }

    }

    private void removeProjectCheatStudent(DAO projectDao, List<ExamSubject> subjects) {
        //学生作弊的科目数,  当学生作弊的科目数等于参考科目数,该学生为全科作弊
        String query = "select student_id,COUNT(*) counts from cheat  GROUP BY student_id ";
        int subjectCount = (int) subjects.stream()
                .filter(subject -> subject.getVirtualSubject().equals("false"))
                .count();

        List<String> studentList = projectDao.query(query).stream()
                .filter(row -> row.getInteger("counts", 0) == subjectCount)
                .map(row -> "\"" + row.getString("student_id") + "\"")
                .collect(Collectors.toList());

        if (studentList.isEmpty()) {
            return;
        }

        if (studentList.size() == 1) {
            String studentId = studentList.get(0);
            LOG.info("删除全科作弊的考生.... ");
            String sql = "delete from score_project where student_id = {{studentId}}";
            projectDao.execute(sql.replace("{{studentId}}", studentId));
            LOG.info("项目的全科作弊学生删除完毕.....");
        } else {
            String students = String.join(",", studentList);
            LOG.info("删除全科作弊的考生.... ");
            String sql = "delete from score_project where student_id in ({{condition}})";
            projectDao.execute(sql.replace("{{condition}}", students));
            LOG.info("项目的全科作弊学生删除完毕.....");
        }

    }

    private void removeProjectZeroScores(String projectId, List<ExamSubject> subjects) {
        //删除项目总分为0分,且没有缺考没有作弊记录的学生成绩
        //(语数外){语缺考,数缺考,外参考得0(应该排除);语数外均缺考得0分此处不应排除}
        String sql = "delete from score_project where score=0 and student_id not in(" +
                "select a.student_id from (select student_id,COUNT(*) counts from absent GROUP BY student_id) a where a.counts = {{count}}\n" +
                "UNION\n" +
                "select a.student_id from (select student_id,COUNT(*) counts from cheat GROUP BY student_id) a where a.counts = {{count}}" +
                ") ";

        int subjectCount = (int) subjects.stream()
                .filter(subject -> subject.getVirtualSubject().equals("false"))
                .count();

        LOG.info("删除项目全科缺考、作弊外的零分记录...");
        daoFactory.getProjectDao(projectId).execute(sql.replace("{{count}}", String.valueOf(subjectCount)));
        LOG.info("项目 {} 的全科缺考、作弊外的零分记录删除完毕。", projectId);
    }

    private void removeProjectAbsentStudent(DAO projectDao, List<ExamSubject> subjects) {
        //学生缺考的科目数,  当学生缺考的科目数等于参考科目数,该学生为全科缺考
        String query = "select student_id,COUNT(*) counts from absent  GROUP BY student_id ";
        int subjectCount = (int) subjects.stream()
                .filter(subject -> subject.getVirtualSubject().equals("false"))
                .count();

        List<String> studentList = projectDao.query(query).stream()
                .filter(row -> row.getInteger("counts", 0) == subjectCount)
                .map(row -> "\"" + row.getString("student_id") + "\"")
                .collect(Collectors.toList());

        if (studentList.isEmpty()) {
            return;
        }

        if (studentList.size() == 1) {
            LOG.info("删除全科缺考的考生记录....");
            String studentId = studentList.get(0);
            String sql = "delete from score_project where student_id = {{studentId}}";
            projectDao.execute(sql.replace("{{studentId}}", studentId));
            LOG.info("项目的全科缺考学生删除完毕.....");
        } else {
            String students = String.join(",", studentList);
            LOG.info("删除全科缺考的考生记录....");
            String sql = "delete from score_project where student_id in ({{condition}})";
            projectDao.execute(sql.replace("{{condition}}", students));
            LOG.info("项目的全科缺考学生删除完毕.....");
        }

    }


    private void removeSubject(String projectId, DAO projectDao, ExamSubject subject, ReportConfig reportConfig) {
        String subjectId = subject.getId();
        String subjectiveTableName = "score_subjective_" + subjectId;
        String objectiveTableName = "score_objective_" + subjectId;
        String subjectTableName = "score_subject_" + subjectId;

        //默认剔除丢卷学生......
        projectDao.execute(DEL_LOST_SCORE.replace("{{tableName}}", subjectTableName));
        projectDao.execute(DEL_LOST_SCORE.replace("{{tableName}}", subjectiveTableName));
        projectDao.execute(DEL_LOST_SCORE.replace("{{tableName}}", objectiveTableName));

        //删除0分
        if (Boolean.valueOf(reportConfig.getRemoveZeroScores())) {

            removeSubjectZeroScore(projectId, subject, subjectTableName);


            projectDao.execute(DEL_ZERO_SCORE
                    .replace("{{tableName}}", subjectiveTableName)
                    .replace("{{subjectId}}", subjectId));
            projectDao.execute(DEL_ZERO_SCORE
                    .replace("{{tableName}}", objectiveTableName)
                    .replace("{{subjectId}}", subjectId));
        }

        //删除缺考得0分
        if (Boolean.valueOf(reportConfig.getRemoveAbsentStudent())) {
            projectDao.execute(DEL_ABS_SCORE.replace("{{tableName}}", subjectTableName));
            projectDao.execute(DEL_ABS_SCORE.replace("{{tableName}}", subjectiveTableName));
            projectDao.execute(DEL_ABS_SCORE.replace("{{tableName}}", objectiveTableName));
        }

        //删除作弊得0分
        if (Boolean.valueOf(reportConfig.getRemoveCheatStudent())) {
            projectDao.execute(DEL_CHEAT_SCORE.replace("{{tableName}}", subjectTableName));
            projectDao.execute(DEL_CHEAT_SCORE.replace("{{tableName}}", subjectiveTableName));
            projectDao.execute(DEL_CHEAT_SCORE.replace("{{tableName}}", objectiveTableName));
        }


    }

    //只删除卷面为0分的记录
    private void removeSubjectZeroScore(String projectId, ExamSubject subject, String subjectTableName) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String subjectId = subject.getId();

        if (Boolean.valueOf(subject.getVirtualSubject())) {
            ExamSubject complexSubject = subjectService.findComplexSubject(projectId, subjectId);
            String complexSubjectId = complexSubject.getId();
            //删除 综合科目为0分的 拆分科目的学生记录
            String sql = "delete from `score_subject_{{subjectId}}` " +
                    "where student_id in (select a.student_id from `score_subject_{{complexSubjectId}}` a " +
                    "where a.score =0) and score=0 and paper_score_type = \"paper\" ";
            String replace = sql.replace("{{subjectId}}", subjectId)
                    .replace("{{complexSubjectId}}", complexSubjectId);
            projectDao.execute(replace);
        } else {
            String sql = DEL_SUBJECT_ZERO_SCORE.replace("{{tableName}}", subjectTableName);
            projectDao.execute(sql);
        }
    }

}
