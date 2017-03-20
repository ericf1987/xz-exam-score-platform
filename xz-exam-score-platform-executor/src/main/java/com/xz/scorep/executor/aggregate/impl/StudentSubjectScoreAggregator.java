package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.DAOException;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.ExamSubject;
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

    @Autowired
    private QuestService questService;

    @Autowired
    private SubjectService subjectService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        List<ExamSubject> subjects = getSubjects(aggregateParameter);

        ThreadPools.createAndRunThreadPool(20, 1,
                pool -> accumulateSubjectScores(projectId, projectDao, pool, subjects));
    }

    private List<ExamSubject> getSubjects(AggregateParameter aggregateParameter) {
        String projectId = aggregateParameter.getProjectId();
        List<ExamSubject> subjects;

        if (aggregateParameter.getSubjects().isEmpty()) {
            subjects = subjectService.listSubjects(projectId);

        } else {
            subjects = aggregateParameter.getSubjects()
                    .stream().map(subjectId -> subjectService.findSubject(projectId, subjectId))
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
                    "  left join `score_" + questId + "` q on p.student_id=q.student_id\n" +
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
