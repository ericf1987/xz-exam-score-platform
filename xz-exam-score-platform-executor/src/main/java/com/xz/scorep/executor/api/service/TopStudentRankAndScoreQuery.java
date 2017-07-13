package com.xz.scorep.executor.api.service;

import com.hyd.dao.DAO;
import com.hyd.simplecache.SimpleCache;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 排名靠前和靠后的学生得分和排名查询，用于快报
 *
 * @author by fengye on 2017/7/4.
 */
@Component
public class TopStudentRankAndScoreQuery {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    CacheFactory cacheFactory;

    public static final String QUERY_TOP_STUDENT_RANK_AND_SCORE = "SELECT stu.id student_id, stu.name student_name, rank.rank rank, p_rank.rank p_rank, score.score \n" +
            "FROM {{rank_table}} rank, student stu, {{parent_rank_table}} p_rank, score_subject_{{subject_id}} score\n" +
            "WHERE rank.subject_id = {{subject_id}} \n" +
            "AND p_rank.subject_id = {{subject_id}} \n" +
            "AND rank.student_id = p_rank.student_id\n" +
            "AND p_rank.student_id = stu.id\n" +
            "AND stu.id = score.student_id\n" +
            "and stu.class_id = ?\n" +
            "ORDER BY rank";

    public List<Map<String, Object>> queryTopStudent(String projectId, String subjectId, String rangeName, String rangeId, boolean isTop, int count) {
        List<Map<String, Object>> rankAndScore = queryTopStudentRankAndScore(projectId, subjectId, rangeName, rangeId);

        if(count <= rankAndScore.size()){
            return isTop ? rankAndScore.subList(0, count) : rankAndScore.subList(rankAndScore.size() - count, rankAndScore.size());
        }

        return rankAndScore.subList(0, rankAndScore.size());
    }

    private List<Map<String, Object>> queryTopStudentRankAndScore(String projectId, String subjectId, String rangeName, String rangeId) {
        String sql = replaceRankTable(QUERY_TOP_STUDENT_RANK_AND_SCORE, rangeName).replace("{{subject_id}}", subjectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);

        SimpleCache projectCache = cacheFactory.getProjectCache(projectId);
        String cacheKey = "queryTopStudent:" + projectId + ":" + subjectId + ":" + rangeName + ":" + rangeId;
        return projectCache.get(cacheKey, () -> new ArrayList<>(projectDao.query(sql, rangeId)));
    }

    public String replaceRankTable(String sql, String rangeName) {
        String result = "";
        if (Range.CLASS.equals(rangeName)) {
            result = sql.replace("{{rank_table}}", "rank_class").replace("{{parent_rank_table}}", "rank_school");
        } else if (Range.SCHOOL.equals(rangeName)) {
            result = sql.replace("{{rank_table}}", "rank_school").replace("{{parent_rank_table}}", "rank_province");
        }
        return result;
    }
}
