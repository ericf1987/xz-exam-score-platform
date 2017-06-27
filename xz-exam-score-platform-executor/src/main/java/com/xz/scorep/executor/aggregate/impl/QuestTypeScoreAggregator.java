package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.beans.dic.QuestType;
import com.xz.ajiaedu.common.concurrent.Executors;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamQuestType;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.QuestTypeService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.utils.DoubleUtils;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author by fengye on 2017/6/22.
 */
@AggregateTypes(AggregateType.Advanced)
@AggregateOrder(81)
@Component
public class QuestTypeScoreAggregator extends Aggregator {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    SubjectService subjectService;

    @Autowired
    QuestTypeService questTypeService;

    public static final Logger LOG = LoggerFactory.getLogger(QuestTypeScoreAggregator.class);

    public static final String QUERY_QUEST_TYPE_BY_SUBJECT = "SELECT qtl.id quest_type_id, qtl.quest_type_name quest_type_name, GROUP_CONCAT(quest.id) quest_ids\n" +
            "FROM quest_type_list qtl, quest\n" +
            "WHERE qtl.id = quest.`question_type_id`\n" +
            "AND quest.`exam_subject` = ?\n" +
            "GROUP BY quest_type_id, quest_type_name";

    public static final String QUERY_SCORE_BY_QUEST_ID = "select student_id, score from score_{{quest_id}} ";

    public static final String SELECT_QUEST_TYPE_SUM = "select student_id, sum(score) score, COUNT(1) cnt from ({{union_all_sql}}) t group by student_id ";

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {

        LOG.info("开始执行 试卷题型得分 统计 ：{}", this.getClass().getSimpleName());

        long begin = System.currentTimeMillis();

        String projectId = aggregateParameter.getProjectId();

        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table quest_type_score");

        //查询考试科目
        List<String> subjectIds = subjectService.listSubjects(projectId).stream().filter(s -> !BooleanUtils.toBoolean(s.getVirtualSubject()))
                .map(ExamSubject::getId).collect(Collectors.toList());

        ThreadPoolExecutor executor = Executors.newBlockingThreadPoolExecutor(10, 10, 10);

        for (String subjectId : subjectIds) {
            executor.submit(() -> processData(projectId, projectDao, subjectId));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);

        long end = System.currentTimeMillis();

        LOG.info("结束执行 试卷题型得分 统计:{}， 耗时:{}", this.getClass().getSimpleName(), end - begin);
    }

    private void processData(String projectId, DAO projectDao, String subjectId) {
        //1.查询出单个科目下面每个题型包含的题目ID
        //2.根据ID做得分明细表关联查询，求出每个学生的得分汇总
        //3.汇总
        List<Row> questTypeInOneSubject = projectDao.query(QUERY_QUEST_TYPE_BY_SUBJECT, subjectId);

        //存放单个科目每个学生每个题型的得分
        List<Row> result = new ArrayList<>();
        questTypeInOneSubject.forEach(q -> {
            //当前题型ID
            String questTypeId = q.getString("quest_type_id");
            String quest_ids = q.getString("quest_ids");

            ExamQuestType examQuestType = questTypeService.getQuestType(projectId, questTypeId);
            //当前题型ID的满分

            double fullScore = examQuestType.getFullScore();

            //当前科目参考学生该题型ID的总分
            List<Row> questTypeSum = runTaskByQuestTypeId(projectDao, quest_ids);

            questTypeSum.forEach(o -> {
                        double score = o.getDouble("score", 0);
                        o.put("exam_subject", examQuestType.getExamSubject());
                        o.put("quest_subject", examQuestType.getQuestSubject());
                        o.put("quest_type_id", questTypeId);
                        o.put("quest_type_name", examQuestType.getQuestTypeName());
                        o.put("rate", fullScore != 0 ? DoubleUtils.round(score / fullScore, true) : 0);
                    }
            );
            result.addAll(questTypeSum);
        });

        projectDao.insert(result, "quest_type_score");
    }

    private List<Row> runTaskByQuestTypeId(DAO projectDao, String quest_ids) {
        String unionSql = SELECT_QUEST_TYPE_SUM.replace("{{union_all_sql}}", unionSqlByQuestId(QUERY_SCORE_BY_QUEST_ID, quest_ids));
        //查询出所有学生该题型的得分
        return projectDao.query(unionSql);
    }

    public String unionSqlByQuestId(String sql, String quest_ids) {
        List<String> result = new ArrayList<>();
        Arrays.asList(quest_ids.split(",")).forEach(f -> {
            String r = sql.replace("{{quest_id}}", f);
            result.add(r);
        });
        return StringUtil.joinPaths(" UNION ALL ", result);
    }

}
