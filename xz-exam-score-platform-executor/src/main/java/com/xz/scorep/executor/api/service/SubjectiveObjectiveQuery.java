package com.xz.scorep.executor.api.service;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.SimpleCache;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 主观题,客观题得分详情
 *
 * @author luckylo
 */
@Component
public class SubjectiveObjectiveQuery {


    private static final String QUERY_RANK_AND_SCORE = "select * from \n" +
            "(\n" +
            "select score.score ,rank.rank \n" +
            "from {{scoreTable}} `score`,{{rankTable}} `rank`\n" +
            "where \n" +
            "rank.student_id = score.student_id\n" +
            "and rank.subject_id = \"{{subjectId}}\"\n" +
            "and rank.student_id = \"{{studentId}}\"\n" +
            ") tmp1,\n" +
            "(\n" +
            "select avg(score) average_score,MAX(score) max_score from {{scoreTable}} \n" +
            "where student_id in (select id from student where class_id = \"{{classId}}\")\n" +
            ")tmp2\n";


    private static final String QUERY_FULL_SCORE = "select sum(full_score) full_score from quest where " +
            "exam_subject = '{{subjectId}}' " +
            "and objective = 'true' ";

    @Autowired
    private CacheFactory cacheFactory;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private DAOFactory daoFactory;

    //查询学生答案
    public Row queryStudentRow(String projectId, String studentId, String questId) {
        SimpleCache cache = cacheFactory.getPaperCache(projectId);
        String cacheKey = "quest:" + questId;
        String table = "score_" + questId;
        DAO projectDao = daoFactory.getProjectDao(projectId);

        ArrayList<Row> rows = cache.get(cacheKey, () -> {
            return new ArrayList<>(projectDao.query("select * from `" + table + "`"));
        });

        return rows.stream()
                .filter(row -> studentId.equals(row.getString("student_id")))
                .findFirst()
                .get();
    }

    //查询客观题得分详情,每一道题的正确答案和班级得分率....
    public Row queryObjectiveDetail(String projectId, String questId, String classId) {
        SimpleCache cache = cacheFactory.getPaperCache(projectId);
        String cacheKey = "objective:";
        DAO projectDao = daoFactory.getProjectDao(projectId);

        ArrayList<Row> rows = cache.get(cacheKey, () -> {
            return new ArrayList<>(projectDao.query("select * from objective_score_rate"));
        });
        return rows.stream()
                .filter(row -> questId.equals(row.getString("quest_id")) && classId.equals(row.getString("range_id")))
                .findFirst().get();
    }


    //查询主观题得分详情...每一道题的最高分做低分
    public Row querySubjectiveDetail(String projectId, String questId, String classId) {
        SimpleCache cache = cacheFactory.getPaperCache(projectId);
        String cacheKey = "subjective:";
        DAO projectDao = daoFactory.getProjectDao(projectId);

        ArrayList<Row> rows = cache.get(cacheKey, () -> {
            return new ArrayList<>(projectDao.query("select * from quest_average_max_score"));
        });
        return rows.stream()
                .filter(row -> "Class".equals(row.getString("range_type")))
                .filter(row -> classId.equals(row.getString("range_id")))
                .filter(row -> questId.equals(row.getString("quest_id")))
                .findFirst().get();

    }

    //查询学生主观题得分信息(我的得分,得分排名,班级平均分,班级最高分...信息)
    public Map<String, Object> querySubjectiveScoreRank(String projectId, String subjectId, String classId, String studentId) {
        String table = "score_subjective_" + subjectId;
        String replace = QUERY_RANK_AND_SCORE
                .replace("{{scoreTable}}", table)
                .replace("{{rankTable}}", "rank_subjective")
                .replace("{{subjectId}}", subjectId)
                .replace("{{studentId}}", studentId)
                .replace("{{classId}}", classId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        return projectDao.queryFirst(replace);
    }


    //查询学生客观题得分信息(我的得分,得分排名,班级平均分,班级最高分...信息)
    public Map<String, Object> queryObjectiveScoreRank(String projectId, String subjectId, String classId, String studentId) {
        String table = "score_objective_" + subjectId;
        String replace = QUERY_RANK_AND_SCORE
                .replace("{{scoreTable}}", table)
                .replace("{{rankTable}}", "rank_objective")
                .replace("{{subjectId}}", subjectId)
                .replace("{{studentId}}", studentId)
                .replace("{{classId}}", classId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        return projectDao.queryFirst(replace);
    }

    //查询每个科目的主观题,客观题分值
    public Map<String, Double> querySubjectiveObjectiveFullScore(String projectId, String subjectId) {
        Map<String, Double> result = new HashMap<>();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        SimpleCache cache = cacheFactory.getPaperCache(projectId);
        String cacheKey = "fullScore" + subjectId;

        Row row = cache.get(cacheKey, () ->
                projectDao.queryFirst(QUERY_FULL_SCORE.replace("{{subjectId}}", subjectId)));
        double objective = row.getDouble("full_score", 0);

        double subjectScore = subjectService.getSubjectScore(projectId, subjectId);

        result.put("objective", objective);
        BigDecimal value = new BigDecimal(String.valueOf(subjectScore))
                .subtract(new BigDecimal(String.valueOf(objective)));
        result.put("subjective", value.doubleValue());
        return result;
    }


}
