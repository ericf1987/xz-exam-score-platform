package com.xz.scorep.executor.api.service;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.utils.DoubleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.xz.scorep.executor.utils.SqlUtils.*;

/**
 * 主客观题情况，用于快报
 * @author by fengye on 2017/7/4.
 */
@Component
public class SAndOQuestQuery {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    CacheFactory cacheFactory;

    @Autowired
    ExamBaseInfoQuery examBaseInfoQuery;

    public static final String QUERY_SUBJECTIVE_AND_OBJECTIVE_STATUS = "SELECT {{group_type}}(score) score FROM {{table_name}}_{{subject_id}} score, student stu\n" +
            "where score.student_id = stu.id\n" +
            "and {{range_id}} = ?";

    public static final String QUERY_MAX_SCORE_STUDENT = "SELECT student_id FROM {{table_name}}_{{subject_id}} score WHERE score = (\n" +
            "SELECT {{group_type}}(score) FROM {{table_name}}_{{subject_id}} score, student stu\n" +
            "WHERE score.`student_id` = stu.`id`\n" +
            "AND stu.{{range_id}} = ?)";

    public Map<String, Object> getSAndOMap(String projectId, String subjectId, String rangeName, String rangeId, boolean isObjective) {

        Map<String, Object> map = new HashMap<>();

        double maxScore = getGroupTypeScore(GroupType.MAX, projectId, subjectId, rangeName, rangeId, isObjective);
        double avgScore = getGroupTypeScore(GroupType.AVG, projectId, subjectId, rangeName, rangeId, isObjective);

        map.put("avgScore", avgScore);
        map.put("maxScore", maxScore);

        if(Range.CLASS.equals(rangeName)){
            map.put("studentName", getQueryMaxScoreStudent(GroupType.MAX, projectId, subjectId, rangeName, rangeId, isObjective));
        }

        return map;
    }

    public double getGroupTypeScore(String groupType, String projectId, String subjectId, String rangeName, String rangeId, boolean isObjective) {
        String sql = replaceQuery(groupType, subjectId, rangeName, QUERY_SUBJECTIVE_AND_OBJECTIVE_STATUS, isObjective);

        DAO projectDao = daoFactory.getProjectDao(projectId);
        Row row = projectDao.queryFirst(sql, rangeId);

        return row != null ? DoubleUtils.round(row.getDouble("score", 0)) : 0;
    }

    public String getQueryMaxScoreStudent(String groupType, String projectId, String subjectId, String rangeName, String rangeId, boolean isObjective) {
        return examBaseInfoQuery.getMaxScoreStudentName(projectId, rangeId,
                replaceQuery(groupType, subjectId, rangeName, QUERY_MAX_SCORE_STUDENT, isObjective));
    }

    private String replaceQuery(String groupType, String subjectId, String rangeName, String sql, boolean isObjective) {
        return replaceGroupType(groupType,
                replaceRangeId(rangeName,
                        replaceSubjectId(sql, "{{subject_id}}", subjectId)
                )
        ).replace("{{table_name}}", isObjective ? "score_objective" : "score_subjective");
    }

}
