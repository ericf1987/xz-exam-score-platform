package com.xz.scorep.executor.aggregate.impl;

import com.alibaba.fastjson.JSONObject;
import com.hyd.dao.DAO;
import com.hyd.dao.DAOException;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.config.AggregateConfig;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.QuestService;
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
import java.util.stream.Collectors;

@Component
@AggragateOrder(0)
@AggregateTypes({AggregateType.Basic, AggregateType.Quick})
public class StudentSubjectScoreAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(StudentSubjectScoreAggregator.class);

    private static final String DEL_ZERO_SCORE = "delete from score_subject_{{subject}} where score=0";

    private static final String DEL_ABS_SCORE = "delete from score_subject_{{subject}} " +
            "where student_id in (\n" +
            "  select a.student_id from absent a where a.subject_id like \"%{{subject}}%\"  \n" +
            ")";

    private static final String DEL_LOST_SCORE = "delete from score_subject_{{subject}} " +
            "where student_id in (\n" +
            "  select a.student_id from lost a where a.subject_id like \"%{{subject}}%\"  \n" +
            ")";

    private static final String DEL_CHEAT_SCORE = "delete from score_subject_{{subject}} " +
            "where student_id in (\n" +
            "  select a.student_id from cheat a where a.subject_id like \"%{{subject}}%\"  \n" +
            ")";

    @Autowired
    private QuestService questService;

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

        List<ExamSubject> subjects = getSubjects(aggregateParameter);

        LOG.info("subjectsSize ...{}",subjects.size());
        ThreadPools.createAndRunThreadPool(aggregateConfig.getSubjectPoolSize(), 1,
                pool -> accumulateSubjectScores(projectId, projectDao, pool, subjects));

        // 根据报表配置删除作弊学生
        if (Boolean.valueOf(reportConfig.getRemoveCheatStudent())){
            LOG.info("删除项目 {} 作弊考生...", projectId);
            removeCheatStudent(projectId,subjects);
            LOG.info("项目 {} 作弊考生删除完毕...", projectId);
        }

        // 根据报表配置删除缺考考生记录  (缺卷同缺处理)
        if (Boolean.valueOf(reportConfig.getRemoveAbsentStudent())) {
            LOG.info("删除项目 {} 缺考考生...", projectId);
            removeAbsentStudents(projectId, subjects);
            LOG.info("项目 {} 缺考考生删除完毕。", projectId);

            LOG.info("删除项目{}缺卷学生...", projectId);
            removeLostStudents(projectId, subjects);
            LOG.info("项目 {} 缺卷考生删除完毕。", projectId);

        }

        // 根据报表配置删除零分记录
        if (Boolean.valueOf(reportConfig.getRemoveZeroScores())) {
            removeZeroScores(projectId, subjects);
        }

        // 将 score 拷贝到 real_score
        LOG.info("拷贝项目 {} 的科目分数 score 到 real_score...", projectId);
        copyToRealScore(projectId, subjects);

        // 根据报表配置补完接近及格的分数
        if (Boolean.valueOf(reportConfig.getFillAlmostPass())) {
            LOG.info("提升项目 {} 的接近及格分数...", projectId);
            Double almostPassOffset = reportConfig.getAlmostPassOffset();
            fillAlmostPass(projectId, subjects, almostPassOffset, reportConfig);
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
            if (scoreLevelConfig.equals("rate")) {
                passScore = subject.getFullScore() * ScoreLevelsHelper.getScoreLevels(subjectId, scoreLevelConfig, scoreLevels).get("Pass");
            } else {
                passScore = ScoreLevelsHelper.getScoreLevels(subjectId, scoreLevelConfig, scoreLevels).get("Pass");
            }
            double almostPassScore = passScore - Math.abs(almostPassOffset);   // 用 abs() 是以防万一 offset 被设置了一个负数
            projectDao.execute("update " + tableName + " set score=? where score>=? and score<?",
                    passScore, almostPassScore, passScore);
        });
    }

    private void removeZeroScores(String projectId, List<ExamSubject> subjects) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        subjects.forEach(subject -> {
            String sql = DEL_ZERO_SCORE.replace("{{subject}}", subject.getId());
            LOG.info("删除科目 {} 零分记录...", subject.getId());
            projectDao.execute(sql);
        });
        LOG.info("项目 {} 的科目零分记录删除完毕。", projectId);
    }

    // 删除科目分数表中被标记为缺考的考生记录
    private void removeAbsentStudents(String projectId, List<ExamSubject> subjects) {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        for (ExamSubject subject : subjects) {
            String sql = DEL_ABS_SCORE.replace("{{subject}}", subject.getId());
            LOG.info("删除科目 {} 缺考考生...", subject.getId());
            projectDao.execute(sql);
        }
    }

    //删除科目分数表中作弊的考生记录
    private void removeCheatStudent(String projectId, List<ExamSubject> subjects) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        for (ExamSubject subject : subjects) {
            String sql = DEL_CHEAT_SCORE.replace("{{subject}}", subject.getId());
            LOG.info("删除科目 {} 作弊考生...", subject.getId());
            projectDao.execute(sql);
        }
    }

    //删除科目分数表中标记为缺卷的考生记录
    private void removeLostStudents(String projectId, List<ExamSubject> subjects) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        for (ExamSubject subject : subjects) {
            String sql = DEL_LOST_SCORE.replace("{{subject}}", subject.getId());
            LOG.info("删除科目 {} 缺卷考生...", subject.getId());
            projectDao.execute(sql);
        }
    }

    private List<ExamSubject> getSubjects(AggregateParameter aggregateParameter) {
        String projectId = aggregateParameter.getProjectId();
        List<ExamSubject> subjects;

        List<String> paramSubjects = aggregateParameter.getSubjects();
        LOG.info("传入的科目参数：" + paramSubjects);

        if (paramSubjects.isEmpty()) {
            subjects = subjectService.listSubjects(projectId);

        } else {
            subjects = paramSubjects
                    .stream().map(subjectId -> {
                        ExamSubject subject = subjectService.findSubject(projectId, subjectId);

                        if (subject == null) {
                            throw new IllegalStateException("科目 " + subjectId + " 没找到");
                        }

                        return subject;
                    })
                    .collect(Collectors.toList());
        }
        return subjects;
    }

    private void accumulateSubjectScores(String projectId, DAO projectDao, ThreadPoolExecutor executor, List<ExamSubject> subjects) {

        subjects.forEach(subject -> {
            String subjectId = subject.getId();
            LOG.info("subjectId{}........",subjectId);
            String tableName = "score_subject_" + subjectId;

            // 初始化
            projectDao.execute("truncate table " + tableName);
            projectDao.execute("insert into " + tableName + "(student_id,score) select id, 0 from student");
            LOG.info("项目 {} 的科目 {} 总分已清空", projectId, subjectId);

            // 累加分数
            accumulateQuestScores(projectId, projectDao, executor, subjectId, tableName);
        });
    }

    private void accumulateQuestScores(String projectId, DAO projectDao, ThreadPoolExecutor executor, String subjectId, String tableName) {
        List<ExamQuest> examQuests = questService.queryQuests(projectId, subjectId);
        LOG.info("questSize ...{}",examQuests.size());
        final AtomicInteger counter = new AtomicInteger(0);

        Runnable accumulateTip = () -> LOG.info(
                "项目 {} 的科目 {} 总分合计已完成 {}/{}", projectId, subjectId, counter.incrementAndGet(), examQuests.size());

        examQuests.forEach(
                examQuest -> executor.submit(
                        () -> accumulateQuestScores0(projectDao, tableName, examQuest, accumulateTip)));
    }

    private void accumulateQuestScores0(DAO projectDao, String tableName, ExamQuest examQuest, Runnable tip) {
        try {
            String questId = examQuest.getId();
            LOG.info("累计分数{}",examQuest.getQuestSubject());

            String combineSql = "update " + tableName + " p \n" +
                    "  inner join `score_" + questId + "` q using(student_id)\n" +
                    "  set p.score=p.score+ifnull(q.score,0)";

            projectDao.execute(combineSql);

            if (tip != null) {
                tip.run();
            }
        } catch (DAOException e) {
            LOG.error("统计科目成绩失败", e);
        }
    }
}
