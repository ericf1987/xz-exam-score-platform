package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.concurrent.Executors;
import com.xz.ajiaedu.common.report.Keys;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.utils.AsyncCounter;
import com.xz.scorep.executor.utils.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 每一道题目的平均分和最高分(最高分目前主要应用于留痕主观题得分详情)
 *
 * @author luckylo
 */
@Component
@AggregateTypes({AggregateType.Advanced, AggregateType.Complete})
@AggragateOrder(53)
public class QuestAverageMaxScoreAggregator extends Aggregator {

    private static Logger LOG = LoggerFactory.getLogger(QuestAverageMaxScoreAggregator.class);

    private static final String DROP_TABLE = "drop table if exists quest_average_max_score";

    private static final String CREATE_TABLE = "create table quest_average_max_score(" +
            " quest_id varchar(40),quest_no varchar(20),exam_subject varchar(10)," +
            " full_score decimal(4,1),average_score decimal(4,2),max_score decimal(4,2)," +
            " objective varchar(5),range_type varchar(20),range_id varchar(40))";

    private static final String CREATE_INDEX = "create index idxqams on quest_average_max_score(quest_id,quest_no,range_type,range_id)";

    private static final String PROJECT_AVERAGE_SCORE = "" +
            "select \"{{questId}}\" quest_id,\"{{questNo}}\" quest_no,\"{{subjectId}}\" exam_subject,{{fullScore}} full_score,\n" +
            "AVG(score.score) average_score , max(score.score) max_score," +
            "\"{{objective}}\" objective,\"{{rangeType}}\" range_type,\"{{rangeId}}\" range_id\n" +
            "from `{{table}}` score\n" +
            "where student_id not in (\n" +
            "select student_id from absent where subject_id = \"{{subjectId}}\"\n" +
            "UNION \n" +
            "select student_id from lost where subject_id = \"{{subjectId}}\"\n" +
            "UNION \n" +
            "select student_id from cheat where subject_id = \"{{subjectId}}\"\n" +
            ");";

    private static final String SCHOOL_AVERAGE_SCORE = "" +
            "select \"{{questId}}\" quest_id,\"{{questNo}}\" quest_no,\"{{subjectId}}\" exam_subject,{{fullScore}} full_score,\n" +
            "AVG(score.score) average_score ,max(score.score) max_score," +
            "\"{{rangeType}}\" range_type,s.school_id range_id,\"{{objective}}\" objective \n" +
            "from `{{table}}` score,student s\n" +
            "where s.id = score.student_id\n" +
            "and student_id not in (\n" +
            "select student_id from absent where subject_id = \"{{subjectId}}\"\n" +
            "UNION \n" +
            "select student_id from lost where subject_id = \"{{subjectId}}\"\n" +
            "UNION \n" +
            "select student_id from cheat where subject_id = \"{{subjectId}}\"\n" +
            ")\n" +
            "GROUP BY s.school_id";

    private static final String CLASS_AVERAGE_SCORE = "" +
            "select \"{{questId}}\" quest_id,\"{{questNo}}\" quest_no,\"{{subjectId}}\" exam_subject,{{fullScore}} full_score,\n" +
            "AVG(score.score) average_score ,max(score.score) max_score," +
            "\"{{rangeType}}\" range_type,s.class_id range_id,\"{{objective}}\" objective \n" +
            "from `{{table}}` score,student s\n" +
            "where s.id = score.student_id\n" +
            "and student_id not in (\n" +
            "select student_id from absent where subject_id = \"{{subjectId}}\"\n" +
            "UNION \n" +
            "select student_id from lost where subject_id = \"{{subjectId}}\"\n" +
            "UNION \n" +
            "select student_id from cheat where subject_id = \"{{subjectId}}\"\n" +
            ")\n" +
            "GROUP BY s.class_id";

    @Autowired
    private QuestService questService;

    @Autowired
    private DAOFactory daoFactory;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        initializeTable(projectDao);

        processQuestAverage(projectDao, projectId);
    }

    private void processQuestAverage(DAO projectDao, String projectId) {
        ThreadPoolExecutor pool = Executors.newBlockingThreadPoolExecutor(20, 20, 1);

        List<ExamQuest> quests = questService.queryQuests(projectId);
        AsyncCounter counter = new AsyncCounter("正在统计项目题目平均分最高分 ", quests.size());
        quests.forEach(quest -> pool.submit(() -> insertData(projectDao, quest, counter)));
    }

    private void insertData(DAO projectDao, ExamQuest quest, AsyncCounter count) {

        List<Row> insertRow = new ArrayList<>();
        String questId = quest.getId();
        String table = "score_" + questId;
        boolean objective = quest.isObjective();
        String questNo = quest.getQuestNo();
        String examSubjectId = quest.getExamSubject();//参加考试科目
        double fullScore = quest.getFullScore();
        String project = PROJECT_AVERAGE_SCORE
                .replace("{{questId}}", questId)
                .replace("{{questNo}}", questNo)
                .replace("{{table}}", table)
                .replace("{{objective}}", String.valueOf(objective))
                .replace("{{subjectId}}", examSubjectId)
                .replace("{{fullScore}}", String.valueOf(fullScore))
                .replace("{{rangeType}}", Keys.Range.Province.name())
                .replace("{{rangeId}}", "430000");
        insertRow.addAll(projectDao.query(project));

        String school = SCHOOL_AVERAGE_SCORE
                .replace("{{questId}}", questId)
                .replace("{{questNo}}", questNo)
                .replace("{{table}}", table)
                .replace("{{objective}}", String.valueOf(objective))
                .replace("{{subjectId}}", examSubjectId)
                .replace("{{fullScore}}", String.valueOf(fullScore))
                .replace("{{rangeType}}", Keys.Range.School.name());
        insertRow.addAll(projectDao.query(school));

        String clazz = CLASS_AVERAGE_SCORE
                .replace("{{questId}}", questId)
                .replace("{{questNo}}", questNo)
                .replace("{{table}}", table)
                .replace("{{objective}}", String.valueOf(objective))
                .replace("{{subjectId}}", examSubjectId)
                .replace("{{fullScore}}", String.valueOf(fullScore))
                .replace("{{rangeType}}", Keys.Range.Class.name());
        insertRow.addAll(projectDao.query(clazz));
        projectDao.insert(insertRow, "quest_average_max_score");
        count.count();
    }

    private void initializeTable(DAO projectDao) {
        SqlUtils.initialTable(projectDao, DROP_TABLE, CREATE_TABLE, CREATE_INDEX);
    }
}
