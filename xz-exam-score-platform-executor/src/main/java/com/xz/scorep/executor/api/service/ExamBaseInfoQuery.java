package com.xz.scorep.executor.api.service;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.SimpleCache;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.StudentService;
import com.xz.scorep.executor.utils.DoubleUtils;
import com.xz.scorep.executor.utils.SqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 考试基本情况信息，用于快报
 *
 * @author by fengye on 2017/7/3.
 */
@Component
public class ExamBaseInfoQuery {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    CacheFactory cacheFactory;

    @Autowired
    StudentService studentService;

    //查询参考人数相关
    public static final String QUERY_STUDENT_COUNT_LIST = "SELECT province, school_id, class_id, COUNT(1) cnt FROM student stu WHERE id NOT IN(\n" +
            "select student_id from absent where stu.id = student_id\n" +
            "AND subject_id = {{subject_id}}\n" +
            ") GROUP BY province, school_id, class_id";

    //按分组规律查询得分
    public static final String QUERY_GROUP_TYPE_SCORE = "SELECT {{group_type}}(score) score FROM score_subject_{{subject_id}} WHERE student_id IN (\n" +
            "SELECT id FROM student WHERE {{range_id}} = ?)";

    //查询最高分的学生
    public static final String QUERY_MAX_SCORE_STU = "SELECT * FROM score_subject_{{subject_id}} WHERE score = (\n" +
            "SELECT MAX(score) FROM score_subject_{{subject_id}} score, student stu\n" +
            "WHERE score.student_id = stu.id\n" +
            "AND stu.{{range_id}} = ?)" +
            "AND student_id IN (\n" +
            "select id from student where {{range_id}} = ?)";

    //按维度查询平均分排名
    public static final String QUERY_RANK_BY_CLASS = "SELECT AVG(score) score, stu.class_Id FROM score_subject_{{subject_id}} score, student stu\n" +
            "WHERE score.student_id = stu.id\n" +
            "GROUP BY stu.class_id  order by score desc";

    public Map<String, Object> queryBaseInfoMap(String projectId, String subjectId, String rangeName, String rangeId) {
        //参考人数
        long count = getCountByRange(projectId, subjectId, rangeName, rangeId);
        //平均分
        double average = getGroupScoreByRange(SqlUtils.GroupType.AVG, projectId, subjectId, rangeName, rangeId);
        //最高分
        double max = getGroupScoreByRange(SqlUtils.GroupType.MAX, projectId, subjectId, rangeName, rangeId);
        //学生姓名

        String sql = SqlUtils.replaceRangeId(rangeName, QUERY_MAX_SCORE_STU).replace("{{subject_id}}", subjectId);

        String studentName = getMaxScoreStudentName(projectId, rangeId, sql);
        //如果是班级维度，则需要查询该班级的平均分在所有班级中的排名

        Map<String, Object> map = new HashMap<>();
        map.put("count", count);
        map.put("average", average);
        map.put("max", max);
        map.put("topStudentName", studentName);
        map.put("rangeName", rangeName);

        if (Range.CLASS.equals(rangeName)) {
            map.put("rank", queryRankByRange(projectId, subjectId, rangeId));
        } else {
            map.put("rank", "");
        }

        return map;
    }

    public List<Row> queryCountList(String projectId, String subjectId) {
        SimpleCache cache = cacheFactory.getProjectCache(projectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String cacheKey = "query_student_count_list:" + projectId + ":" + subjectId;
        return cache.get(cacheKey, () -> new ArrayList<>(projectDao.query(QUERY_STUDENT_COUNT_LIST.replace("{{subject_id}}", subjectId))));
    }

    public int queryRankByRange(String projectId, String subjectId, String rangeId) {
        String sql = QUERY_RANK_BY_CLASS.replace("{{subject_id}}", subjectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<Row> rows = projectDao.query(sql);
        for (int i = 0; i < rows.size(); i++) {
            if (rangeId.equals(rows.get(i).getString("class_id"))) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * 查询参考人数
     *
     * @param projectId 项目ID
     * @param subjectId 科目ID
     * @param rangeName 维度名称
     * @param rangeId   维度ID
     * @return 返回结果
     */
    public long getCountByRange(String projectId, String subjectId, String rangeName, String rangeId) {
        List<Row> rows = queryCountList(projectId, subjectId);
        long count = 0;
        switch (rangeName) {
            case Range.PROVINCE:
                count = rows.stream().filter(r -> rangeId.equals(r.getString("province"))).mapToInt(r -> r.getInteger("cnt", 0)).sum();
                break;
            case Range.SCHOOL:
                count = rows.stream().filter(r -> rangeId.equals(r.getString("school_id"))).mapToInt(r -> r.getInteger("cnt", 0)).sum();
                break;
            case Range.CLASS:
                count = rows.stream().filter(r -> rangeId.equals(r.getString("class_id"))).mapToInt(r -> r.getInteger("cnt", 0)).sum();
                break;
        }
        return count;
    }

    /**
     * 查询分数分组查询结果
     *
     * @param groupType 分组类型
     * @param projectId 项目ID
     * @param subjectId 科目ID
     * @param rangeName 维度名称
     * @param rangeId   维度ID
     * @return
     */
    public double getGroupScoreByRange(String groupType, String projectId, String subjectId, String rangeName, String rangeId) {
        String sql = SqlUtils.replaceGroupType(groupType, SqlUtils.replaceRangeId(rangeName, QUERY_GROUP_TYPE_SCORE)).replace("{{subject_id}}", subjectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);
        Row row = projectDao.queryFirst(sql, rangeId);
        return DoubleUtils.round(row.getDouble("score", 0));
    }

    /**
     * 查询最高分学生信息
     *
     * @param projectId 项目ID
     * @param rangeId   维度ID
     * @param sql       查询语句
     * @return 返回结果
     */
    public String getMaxScoreStudentName(String projectId, String rangeId, String sql) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<Row> query = projectDao.query(sql, rangeId, rangeId);
        String name = "";
        if (null != query) {
            switch (query.size()) {
                case 0:
                    name = "无";
                    break;
                case 1:
                    name = studentService.findStudent(projectId, query.get(0).getString("student_id")).getString("name");
                    break;
                default:
                    name = getMultipleStudentName(projectId, query);
                    break;
            }
        }
        return name;
    }

    private String getMultipleStudentName(String projectId, List<Row> query) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 2; i++) {
            String student_id = query.get(i).getString("student_id");
            String name = studentService.findStudent(projectId, student_id).getString("name");
            builder.append(name + "、");
        }
        builder.deleteCharAt(builder.length() - 1);

        return builder.append("等" + query.size() + "人").toString();
    }
}
