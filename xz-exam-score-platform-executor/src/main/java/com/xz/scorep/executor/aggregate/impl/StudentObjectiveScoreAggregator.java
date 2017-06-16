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
@AggregateTypes({AggregateType.Check, AggregateType.Quick})
@AggregateOrder(0)
public class StudentObjectiveScoreAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(StudentObjectiveScoreAggregator.class);

    private static final String UPDATE_OBJECTIVE_SCORE_INFO = "" +
            "update `{{tableName}}` set paper_score_type = \n" +
            "case\n" +
            "when student_id in (select student_id from lost WHERE subject_id like \"%{{subjectId}}%\") then \"lost\"\n" +
            "when student_id in (select student_id from absent WHERE subject_id like \"%{{subjectId}}%\") then \"absent\"\n" +
            "when student_id in (select student_id from cheat where subject_id like \"%{{subjectId}}%\") then \"cheat\"\n" +
            "else\"paper\" end";

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private QuestService questService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private AggregateConfig aggregateConfig;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        List<ExamSubject> subjects = subjectService.listSubjects(projectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);

        subjects.forEach(subject -> {
            String subjectId = subject.getId();
            String objectiveTableName = "score_objective_" + subjectId;
            String subjectiveTableName = "score_subjective_" + subjectId;

            projectDao.execute("truncate table " + objectiveTableName);
            projectDao.execute("truncate table " + subjectiveTableName);
            projectDao.execute("insert into " + objectiveTableName + "(student_id,score) select id, 0 from student");
            projectDao.execute("insert into " + subjectiveTableName + "(student_id,score) select id, 0 from student");

            LOG.info("科目{}主客观题成绩表已清空。", subjectId);
            //执行单科的主客观题统计
            try {
                ThreadPools.createAndRunThreadPool(
                        aggregateConfig.getObjectivePoolSize(), 1, (pool) -> startObjectiveScoreAggregation(projectId, subject, pool));
            } catch (InterruptedException e) {
                LOG.error("统计主客观题失败", e);
            }

            //更新主客观题得分来源
            updateSubObjScore(projectDao, subjectId, objectiveTableName, subjectiveTableName);

        });


    }

    private void updateSubObjScore(DAO projectDao, String subjectId, String objectiveTableName, String subjectiveTableName) {
        //先更新主客观题得分表的得分来源
        projectDao.execute(UPDATE_OBJECTIVE_SCORE_INFO
                .replace("{{tableName}}", objectiveTableName)
                .replace("{{subjectId}}", subjectId));

        projectDao.execute(UPDATE_OBJECTIVE_SCORE_INFO
                .replace("{{tableName}}", subjectiveTableName)
                .replace("{{subjectId}}", subjectId));
    }


    private void startObjectiveScoreAggregation(String projectId, ExamSubject subject, ThreadPoolExecutor pool) {
        String subjectId = subject.getId();
        LOG.info("正在统计科目{} , 主客观题得分", subjectId);
        List<ExamQuest> examQuests = questService.queryQuests(projectId, subjectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        AsyncCounter counter = new AsyncCounter("统计科目主客观题得分", examQuests.size());

        examQuests.forEach(examQuest -> {
            boolean objective = examQuest.isObjective();
            String questId = examQuest.getId();

            String accumulateTableName = "score_" + (objective ? "objective" : "subjective") + "_" + subjectId;
            String questScoreTableName = "score_" + questId;

            String combineSql = "update " + accumulateTableName + " p \n" +
                    "  inner join `" + questScoreTableName + "` q using(student_id)\n" +
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
        LOG.info("统计科目  {}  ,主客观题得分完成", subjectId);

    }


}
