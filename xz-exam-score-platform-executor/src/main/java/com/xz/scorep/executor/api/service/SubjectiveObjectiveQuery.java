package com.xz.scorep.executor.api.service;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.SimpleCache;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
            "select format(avg(score),2) average_score,MAX(score) max_score from {{scoreTable}} \n" +
            "where student_id in (select id from student where class_id = \"{{classId}}\")\n" +
            ")tmp2\n";


    private static final String QUERY_FULL_SCORE = "select sum(full_score) full_score from quest where " +
            "exam_subject = '{{subjectId}}' " +
            "and objective = 'true' ";


    private static final String QUERY_CORRECT_STUDENT_COUNT = "select COUNT(1) as `count` from `{{table}}` \n" +
            "where \n" +
            "is_right = \"true\"\n" +
            "and student_id in (select id from student where class_id =\"{{classId}}\")";

    private static final String ZERO_SQL = "select * from `{{table}}` where  student_id = '{{studentId}}'";

    @Autowired
    private CacheFactory cacheFactory;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ReportConfigService reportConfigService;

    @Autowired
    private DAOFactory daoFactory;

    //查询学生答案
    public Row queryStudentRow(String projectId, String studentId, String questId) {
        ArrayList<Row> rows = getQuestCache(projectId, questId);

        return rows.stream()
                .filter(row -> studentId.equals(row.getString("student_id")))
                .findFirst().get();
    }


    //获得题目缓存..
    private ArrayList<Row> getQuestCache(String projectId, String questId) {
        SimpleCache cache = cacheFactory.getPaperCache(projectId);
        String cacheKey = "quest:" + questId;
        String table = "score_" + questId;
        DAO projectDao = daoFactory.getProjectDao(projectId);

        return cache.get(cacheKey, () -> new ArrayList<>(projectDao.query("select * from `" + table + "`")));
    }


    //查询客观题得分详情,每一道题的正确答案和班级得分率....
    public Optional<Row> queryObjectiveDetail(String projectId, String questId, String classId) {
        SimpleCache cache = cacheFactory.getPaperCache(projectId);
        String cacheKey = "objective:";
        DAO projectDao = daoFactory.getProjectDao(projectId);

        ArrayList<Row> rows = cache.get(cacheKey, () -> new ArrayList<>(projectDao.query("select * from objective_score_rate")));
        return rows.stream()
                .filter(row -> questId.equals(row.getString("quest_id")) && classId.equals(row.getString("range_id")))
                .findFirst();
    }


    //查询主观题得分详情...每一道题的最高分平均分
    public Optional<Row> querySubjectiveDetail(String projectId, String questId, String classId) {
        SimpleCache cache = cacheFactory.getPaperCache(projectId);
        String cacheKey = "subjective:";
        DAO projectDao = daoFactory.getProjectDao(projectId);

        ArrayList<Row> rows = cache.get(cacheKey, () -> new ArrayList<>(projectDao.query("select * from quest_average_max_score")));
        return rows.stream()
                .filter(row -> "Class".equals(row.getString("range_type")))
                .filter(row -> classId.equals(row.getString("range_id")))
                .filter(row -> questId.equals(row.getString("quest_id")))
                .findFirst();

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


    //获得学生做错了的客观题.......(当学生作对了,则返回 null)
    public Row queryStudentFalseObjectiveQuest(String projectId, String questId, String studentId) {
        ArrayList<Row> rows = getQuestCache(projectId, questId);

        return rows.stream()
                .filter(row -> "false".equals(row.getString("is_right"))
                        && studentId.equals(row.getString("student_id")))
                .findFirst().orElse(null);
    }

    //查学生单题与班级平均分差距较大的TOP5
    public Row queryStudentSubjectiveQuest(String projectId, String questId, String studentId) {
        ArrayList<Row> rows = getQuestCache(projectId, questId);
        return rows.stream()
                .filter(row -> studentId.equals(row.getString("student_id")))
                .findFirst().orElse(null);
    }

    //查班级单题的平均分得分
    public Row queryClassAverageScore(String projectId, String questId, String classId) {
        SimpleCache cache = cacheFactory.getPaperCache(projectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String cacheKey = "average_score";

        ArrayList<Row> rows = cache.get(cacheKey,
                () -> new ArrayList<>(projectDao.query("select * from quest_average_max_score")));
        return rows.stream()
                .filter(row -> isaBoolean(questId, classId, row))
                .findFirst().orElse(null);
    }

    private boolean isaBoolean(String questId, String classId, Row row) {
        return questId.equals(row.getString("quest_id")) && classId.equals(row.getString("range_id"))
                && "Class".equals(row.getString("range_type"));
    }

    //查询客观题学生答对人数(班级)
    public int queryStudentCorrectCount(String projectId, String questId, String classId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String table = "score_" + questId;
        return projectDao.queryFirst(QUERY_CORRECT_STUDENT_COUNT
                .replace("{{table}}", table)
                .replace("{{classId}}", classId)).getInteger("count", 0);

    }

    //判断学生是否需要排除
    public boolean studentIsExclude(String projectId, String subjectId, String studentId) {
        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);

        if (Boolean.valueOf(reportConfig.getRemoveAbsentStudent())) {
            boolean absent = isAbsent(projectId, subjectId, studentId);
            if (absent) {
                return true;
            }
        }

        if (Boolean.valueOf(reportConfig.getRemoveCheatStudent())) {
            boolean cheat = isCheat(projectId, subjectId, studentId);
            if (cheat) {
                return true;
            }
        }

        if (Boolean.valueOf(reportConfig.getRemoveZeroScores())) {
            boolean zero = isZeroScore(projectId, subjectId, studentId);
            if (zero) {
                return true;
            }
        }

        return false;
    }

    //学生是否为0分
    //移除0分,科目0分是会被剔除,此处应该查不到
    private boolean isZeroScore(String projectId, String subjectId, String studentId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        Row row = projectDao.queryFirst(ZERO_SQL
                .replace("{{table}}", "score_subject_" + subjectId)
                .replace("{{studentId}}", studentId));
        return row == null;
    }

    //学生是否作弊
    private boolean isCheat(String projectId, String subjectId, String studentId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        SimpleCache cache = cacheFactory.getPaperCache(projectId);
        String cacheKey = "cheat :";
        ArrayList<Row> cheatRows = cache.get(cacheKey, () -> new ArrayList<>(projectDao.query("select * from cheat")));

        if (cheatRows.isEmpty()) {
            return false;
        }

        return null != cheatRows.stream()
                .filter(row -> subjectId.equals(row.getString("subject_id")) && studentId.equals(row.getString("student_id")))
                .findFirst().orElse(null);
    }

    //学生是否是缺考
    private boolean isAbsent(String projectId, String subjectId, String studentId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        SimpleCache cache = cacheFactory.getPaperCache(projectId);
        String absentKey = "absent :";
        String lostKey = "lost :";

        ArrayList<Row> absentRows = cache.get(absentKey, () -> new ArrayList<>(projectDao.query("select * from absent")));
        ArrayList<Row> lostRows = cache.get(lostKey, () -> new ArrayList<>(projectDao.query("select * from lost")));

        if (absentRows.isEmpty() && lostRows.isEmpty()) {
            return false;
        }

        Row absent = absentRows.stream()
                .filter(row -> subjectId.equals(row.getString("subject_id")) && studentId.equals(row.getString("student_id")))
                .findFirst().orElse(null);
        //缺考丢卷,不可同时判断.....
        if (absent != null) {
            return true;
        }

        Row lost = lostRows.stream()
                .filter(row -> subjectId.equals(row.getString("subject_id")) && studentId.equals(row.getString("student_id")))
                .findFirst().orElse(null);
        if (lost != null) {
            return true;
        }
        return false;
    }

    public boolean isVirtualSubject(String projectId, String subjectId) {
        ExamSubject subject = subjectService.findSubject(projectId, subjectId);

        return Boolean.valueOf(subject.getVirtualSubject());
    }
}
