package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.QuestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 客观题班级得分率
 * 应用于PDF答题留痕和导出云报表json数据
 *
 * @author luckylo
 */
@Component
@AggregateTypes(AggregateType.Quick)
@AggregateOrder(8)
public class QuestScoreRateAggregator extends Aggregator {

    private static Logger LOG = LoggerFactory.getLogger(QuestScoreRateAggregator.class);

    private static final String INSERT_DATA = "insert into objective_score_rate(" +
            "quest_id,quest_no,subject_id,range_id,range_type,answer,score_rate,count) \n" +
            "select '{{questId}}' quest_id,'{{questNo}}' quest_no,'{{subjectId}}' subject_id," +
            "a.{{rangeId}} range_id, '{{rangeType}}' range_type,'{{answer}}' answer," +
            "(b.avg_score / {{fullScore}}) score_rate,a.count from (\n" +
            "select COUNT(1) `count` ,s.{{rangeId}} from \n" +
            "`score_{{questId}}` score,student s\n" +
            "where s.id = score.student_id \n" +
            "and score.score >0\n" +
            "GROUP BY s.{{rangeId}} \n" +
            ") a,\n" +
            "(\n" +
            "select avg(score.score) avg_score,s.{{rangeId}}  from \n" +
            "`score_{{questId}}` score,student s\n" +
            "where s.id = score.student_id \n" +
            "and student_id not in \n" +
            "( select student_id from absent where subject_id = \"{{subjectId}}\" \n" +
            "UNION  select student_id from lost where subject_id = \"{{subjectId}}\" \n" +
            "UNION  select student_id from cheat where subject_id = \"{{subjectId}}\" ) \n" +
            "GROUP BY s.{{rangeId}} \n" +
            ") b\n" +
            "where a.{{rangeId}} = b.{{rangeId}}";

    private static final String CLASS_ID = "class_id";

    private static final String SCHOOL_ID = "school_id";

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private QuestService questService;


    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);
        projectDao.execute("truncate table objective_score_rate");

        LOG.info("项目ID {} 在统计班级客观题得分率 .....", projectId);
        processData(projectId, projectDao);
        LOG.info("项目ID {} 班级客观题得分率统计完成 .....", projectId);
    }


    private void processData(String projectId, DAO projectDao) {
        List<ExamQuest> quests = questService.queryQuests(projectId);
        quests.stream()
                .filter(quest -> quest.isObjective())
                .forEach(quest -> {
                    String answer = quest.getAnswer();
                    String questId = quest.getId();
                    String questNo = quest.getQuestNo();
                    String subjectId = quest.getExamSubject();
                    String fullScore = String.valueOf(quest.getFullScore());
                    String tmp = INSERT_DATA.replace("{{questId}}", questId)
                            .replace("{{answer}}", answer)
                            .replace("{{subjectId}}", subjectId)
                            .replace("{{fullScore}}", fullScore)
                            .replace("{{questNo}}", questNo);

                    String classSql = tmp
                            .replace("{{rangeType}}", Range.CLASS)
                            .replace("{{rangeId}}", CLASS_ID);
                    projectDao.execute(classSql);

                    String schoolSql = tmp
                            .replace("{{rangeType}}", Range.SCHOOL)
                            .replace("{{rangeId}}", SCHOOL_ID);
                    projectDao.execute(schoolSql);

                    String provinceSql = tmp
                            .replace("{{rangeType}}", Range.PROVINCE)
                            .replace("{{rangeId}}", Range.PROVINCE);
                    projectDao.execute(provinceSql);
                });
    }
}
