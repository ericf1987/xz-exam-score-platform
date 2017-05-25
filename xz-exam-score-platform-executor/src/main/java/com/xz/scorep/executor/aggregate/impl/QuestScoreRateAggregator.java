package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.QuestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 客观题班级得分率
 *
 * @author luckylo
 */
@Component
@AggregateTypes({AggregateType.Advanced})
@AggragateOrder(54)
public class QuestScoreRateAggregator extends Aggregator {

    private static Logger LOG = LoggerFactory.getLogger(QuestScoreRateAggregator.class);

    private static final String INSERT_CLASS_DATA = "insert into objective_score_rate(" +
            "quest_id,quest_no,subject_id,range_id,range_type,answer,score_rate) \n" +
            "select '{{questId}}' quest_id,'{{questNo}}' quest_no,'{{subjectId}}' subject_id," +
            "a.class_id range_id, 'Class' range_type,'{{answer}}' answer," +
            "a.count/b.total score_rate from (\n" +
            "select COUNT(1) `count` ,s.class_id from \n" +
            "`score_{{questId}}` score,student s\n" +
            "where s.id = score.student_id \n" +
            "and score.score >0\n" +
            "GROUP BY s.class_id \n" +
            ") a,\n" +
            "(\n" +
            "select COUNT(1) total,s.class_id  from \n" +
            "`score_{{questId}}` score,student s\n" +
            "where s.id = score.student_id \n" +
            "and student_id not in \n" +
            "( select student_id from absent where subject_id = \"{{subjectId}}\" \n" +
            "UNION  select student_id from lost where subject_id = \"{{subjectId}}\" \n" +
            "UNION  select student_id from cheat where subject_id = \"{{subjectId}}\" ) \n" +
            "GROUP BY s.class_id \n" +
            ") b\n" +
            "where a.class_id = b.class_id";

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private QuestService questService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);
        projectDao.execute("truncate table objective_score_rate");

        LOG.info("项目ID {}在统计班级客观题得分率 .....", projectId);
        //目前只统计每一题目的班级得分率(客观题)....
        processClassData(projectId, projectDao);
        LOG.info("项目ID {}班级客观题得分率统计完成 .....", projectId);
    }


    private void processClassData(String projectId, DAO projectDao) {
        List<ExamQuest> quests = questService.queryQuests(projectId);
        quests.stream()
                .filter(quest -> quest.isObjective())
                .forEach(quest -> {
                    String answer = quest.getScoreRule();
                    String questId = quest.getId();
                    String questNo = quest.getQuestNo();
                    String subjectId = quest.getExamSubject();
                    String sql = INSERT_CLASS_DATA
                            .replace("{{questId}}", questId)
                            .replace("{{answer}}", answer)
                            .replace("{{subjectId}}", subjectId)
                            .replace("{{questNo}}", questNo);
                    projectDao.execute(sql);
                });
    }
}
