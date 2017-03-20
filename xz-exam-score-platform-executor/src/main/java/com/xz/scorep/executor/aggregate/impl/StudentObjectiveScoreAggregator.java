package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.DAOException;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.utils.AsyncCounter;
import com.xz.scorep.executor.utils.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 主客观题分数统计
 */
@Component
@AggregateTypes(AggregateType.Basic)
@AggragateOrder(5)
public class StudentObjectiveScoreAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(StudentObjectiveScoreAggregator.class);

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private QuestService questService;

    @Autowired
    private SubjectService subjectService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        List<ExamSubject> subjects = subjectService.listSubjects(projectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);

        subjects.forEach(subject -> {
            String objectiveTableName = "score_objective_" + subject.getId();
            String subjectiveTableName = "score_subjective_" + subject.getId();

            projectDao.execute("truncate table " + objectiveTableName);
            projectDao.execute("truncate table " + subjectiveTableName);
            projectDao.execute("insert into " + objectiveTableName + "(student_id,score) select id, 0 from student");
            projectDao.execute("insert into " + subjectiveTableName + "(student_id,score) select id, 0 from student");
        });

        LOG.info("主客观题成绩表已清空。");

        ThreadPools.createAndRunThreadPool(
                20, 1, (pool) -> startAggregation(projectId, pool));
    }

    private void startAggregation(String projectId, ThreadPoolExecutor pool) {
        List<ExamQuest> examQuests = questService.queryQuests(projectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        AsyncCounter counter = new AsyncCounter("统计科目主客观题得分", examQuests.size());

        examQuests.forEach(examQuest -> {
            boolean objective = examQuest.isObjective();
            String questId = examQuest.getId();
            String subjectId = examQuest.getExamSubject();

            String accumulateTableName = "score_" + (objective ? "objective" : "subjective") + "_" + subjectId;
            String questScoreTableName = "score_" + questId;

            String combineSql = "update " + accumulateTableName + " p \n" +
                    "  left join `" + questScoreTableName + "` q on p.student_id=q.student_id\n" +
                    "  set p.score=p.score+ifnull(q.score,0)";

            pool.submit(() -> {
                try {
                    projectDao.execute(combineSql);
                    counter.count();
                } catch (DAOException e) {
                    LOG.error("统计主客观题失败", e);
                }
            });
        });
    }
}
