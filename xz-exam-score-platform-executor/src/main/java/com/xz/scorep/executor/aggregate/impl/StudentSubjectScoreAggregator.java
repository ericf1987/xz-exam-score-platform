package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.DAOException;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.config.AggregateConfig;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.project.SubjectService;
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

    public static final String DEL_ABS_SCORE = "delete from score_subject_{{subject}} " +
            "where student_id in (\n" +
            "  select a.student_id from absent a where a.subject_id='{{subject}}'  \n" +
            ")";

    @Autowired
    private QuestService questService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private AggregateConfig aggregateConfig;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        List<ExamSubject> subjects = getSubjects(aggregateParameter);

        ThreadPools.createAndRunThreadPool(aggregateConfig.getSubjectPoolSize(), 1,
                pool -> accumulateSubjectScores(projectId, projectDao, pool, subjects));

        LOG.info("删除项目 {} 缺考考生...", projectId);
        removeAbsentStudents(projectId, subjects);
        LOG.info("项目 {} 缺考考生删除完毕。", projectId);
    }

    // 删除科目分数表中被标记为缺考的考生记录
    private void removeAbsentStudents(String projectId, List<ExamSubject> subjects) {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        for (ExamSubject subject : subjects) {
            String sql = DEL_ABS_SCORE.replace("{{subject}}", subject.getId());
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
            String tableName = "score_subject_" + subjectId;

            // 初始化
            projectDao.execute("truncate table " + tableName);
            projectDao.execute("insert into " + tableName + "(student_id,score) select id, 0 from student");
            LOG.info("项目 {} 的科目 {} 总分已清空", projectId, subjectId);

            // 累加分数
            List<ExamQuest> examQuests = questService.queryQuests(projectId, subjectId);
            final AtomicInteger counter = new AtomicInteger(0);

            Runnable accumulateTip = () -> LOG.info(
                    "项目 {} 的科目 {} 总分合计已完成 {}/{}", projectId, subjectId, counter.incrementAndGet(), examQuests.size());

            examQuests.forEach(
                    examQuest -> executor.submit(
                            () -> accumulateQuestScores(projectDao, tableName, examQuest, accumulateTip)));
        });
    }

    private void accumulateQuestScores(DAO projectDao, String tableName, ExamQuest examQuest, Runnable tip) {
        try {
            String questId = examQuest.getId();

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
