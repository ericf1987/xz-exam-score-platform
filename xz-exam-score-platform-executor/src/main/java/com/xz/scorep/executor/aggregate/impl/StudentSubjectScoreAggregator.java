package com.xz.scorep.executor.aggregate.impl;

import com.alibaba.fastjson.JSONObject;
import com.hyd.dao.DAO;
import com.hyd.dao.DAOException;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.config.AggregateConfig;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import com.xz.scorep.executor.reportconfig.ScoreLevelsHelper;
import com.xz.scorep.executor.utils.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 项目学生的每个科目得分
 */
@Component
@AggregateOrder(1)
@AggregateTypes({AggregateType.Check, AggregateType.Quick})
public class StudentSubjectScoreAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(StudentSubjectScoreAggregator.class);

    private static final String UPDATE_SUBJECT_SCORE_INFO = "" +
            "update `score_subject_{{subjectId}}` set paper_score_type = \n" +
            "case\n" +
            "when student_id in (select student_id from lost WHERE subject_id like \"%{{subjectId}}%\") then \"lost\"\n" +
            "when student_id in (select student_id from absent WHERE subject_id like \"%{{subjectId}}%\") then \"absent\"\n" +
            "when student_id in (select student_id from cheat where subject_id like \"%{{subjectId}}%\") then \"cheat\"\n" +
            "else\"paper\" end";

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private AggregateConfig aggregateConfig;

    @Autowired
    private ReportConfigService reportConfigService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);

        List<ExamSubject> subjects = AggregatorHelper.getSubjects(aggregateParameter, subjectService);

        LOG.info("subjectsSize ...{}", subjects.size());
        ThreadPools.createAndRunThreadPool(aggregateConfig.getSubjectPoolSize(), 1,
                pool -> accumulateSubjectScores(projectId, projectDao, pool, subjects));

        updateSubjectScore(projectId, subjects);

        // 将 score 拷贝到 real_score
        LOG.info("拷贝项目 {} 的科目分数 score 到 real_score...", projectId);
        copyToRealScore(projectId, subjects);

        // (由于低分检查不可能选择到将近及格分数附近,故此时填充基础分数)
        // 根据报表配置补完接近及格的分数
        if (Boolean.valueOf(reportConfig.getFillAlmostPass())) {
            LOG.info("提升项目 {} 的接近及格分数...", projectId);
            Double almostPassOffset = reportConfig.getAlmostPassOffset();
            fillAlmostPass(projectId, subjects, almostPassOffset, reportConfig);
        }
    }

    private void updateSubjectScore(String projectId, List<ExamSubject> subjects) {

        DAO projectDao = daoFactory.getProjectDao(projectId);
        for (ExamSubject subject : subjects) {
            String subjectId = subject.getId();
            //更新科目成绩来源类型(缺考分,作弊分,卷面分),缺考,作弊为0
            projectDao.execute(UPDATE_SUBJECT_SCORE_INFO.replace("{{subjectId}}", subjectId));
        }
    }


    private void copyToRealScore(String projectId, List<ExamSubject> subjects) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        subjects.forEach(subject -> {
            try {
                String tableName = "score_subject_" + subject.getId();
                projectDao.execute("update " + tableName + " set real_score=score");
            } catch (DAOException e) {
                // 如果报错说明表结构是旧版本，忽略此类错误
            }
        });
    }

    private void fillAlmostPass(String projectId, List<ExamSubject> subjects, Double almostPassOffset, ReportConfig reportConfig) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        JSONObject scoreLevels = JSONObject.parseObject(reportConfig.getScoreLevels());
        String scoreLevelConfig = reportConfig.getScoreLevelConfig();

        subjects.forEach(subject -> {
            String subjectId = subject.getId();
            String tableName = "score_subject_" + subjectId;
            double passScore;
            if ("".equals(scoreLevelConfig) || scoreLevelConfig.equals("rate")) {
                passScore = subject.getFullScore() * ScoreLevelsHelper.getScoreLevels(subjectId, scoreLevelConfig, scoreLevels).get("Pass");
            } else {
                passScore = ScoreLevelsHelper.getScoreLevels(subjectId, scoreLevelConfig, scoreLevels).get("Pass");
            }
            double almostPassScore = passScore - Math.abs(almostPassOffset);   // 用 abs() 是以防万一 offset 被设置了一个负数
            projectDao.execute("update " + tableName + " set score=? where score>=? and score<?",
                    passScore, almostPassScore, passScore);
        });
    }

    private void accumulateSubjectScores(String projectId, DAO projectDao, ThreadPoolExecutor executor, List<ExamSubject> subjects) {

        AtomicInteger count = new AtomicInteger(0);

        subjects.forEach(subject -> {
            String subjectId = subject.getId();
            String tableName = "score_subject_" + subjectId;

            // 初始化
            projectDao.execute("truncate table " + tableName);
            projectDao.execute("insert into " + tableName + "(student_id,score) select id, 0 from student");
            LOG.info("项目 {} 的科目 {} 总分已清空", projectId, subjectId);

            //根据主客观题累加分数
            accumulateSubjectScore(subjectId, projectDao);

            int current = count.incrementAndGet();
            LOG.info("项目ID {} 科目总分统计已完成 ... {} / {}", projectId, current, subjects.size());

        });
    }

    private void accumulateSubjectScore(String subjectId, DAO projectDao) {
        String tableName = "score_subject_" + subjectId;
        String subjectiveTableName = "score_subjective_" + subjectId;
        String objectiveTableName = "score_objective_" + subjectId;
        String subSql = "update " + tableName + " p inner join  `" + subjectiveTableName + "` q " +
                "using (student_id) set p.score = p.score + ifnull(q.score,0)";
        String objSql = "update " + tableName + " p inner join   `" + objectiveTableName + "` q " +
                "using (student_id) set p.score = p.score + ifnull(q.score,0)";

        projectDao.execute(subSql);
        projectDao.execute(objSql);
    }

//    private void accumulateQuestScores(String projectId, DAO projectDao, ThreadPoolExecutor executor, String subjectId, String tableName) {
//        List<ExamQuest> examQuests = questService.queryQuests(projectId, subjectId);
//        final AtomicInteger counter = new AtomicInteger(0);
//
//        Runnable accumulateTip = () -> LOG.info(
//                "项目 {} 的科目 {} 总分合计已完成 {}/{}", projectId, subjectId, counter.incrementAndGet(), examQuests.size());
//
//        examQuests.forEach(
//                examQuest -> executor.submit(
//                        () -> accumulateQuestScores0(projectDao, tableName, examQuest, accumulateTip)));
//    }

//    private void accumulateQuestScores0(DAO projectDao, String tableName, ExamQuest examQuest, Runnable tip) {
//        try {
//            String questId = examQuest.getId();
//
//            String combineSql = "update " + tableName + " p \n" +
//                    "  inner join `score_" + questId + "` q using(student_id)\n" +
//                    "  set p.score=p.score+ifnull(q.score,0)";
//
//            projectDao.execute(combineSql);
//
//            if (tip != null) {
//                tip.run();
//            }
//        } catch (DAOException e) {
//            LOG.error("统计科目成绩失败", e);
//        }
//    }
}
