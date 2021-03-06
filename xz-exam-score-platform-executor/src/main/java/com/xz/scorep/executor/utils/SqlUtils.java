package com.xz.scorep.executor.utils;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.bean.Range;
import org.springframework.util.StringUtils;

/**
 * @author by fengye on 2017/5/7.
 */
public class SqlUtils {

    /**
     * 替换SQL字符串中的变量标记
     *
     * @param sql     SQL语句
     * @param tag     变量标记
     * @param replace 替换标记
     * @return 返回
     */
    public static String replaceSubjectId(String sql, String tag, String replace) {
        return sql.replace(tag, replace);
    }

    public static String replaceSubjectId(String sql, String replace) {
        return sql.replace("{{subject_id}}", replace);
    }

    public static class GroupType {
        public static final String MAX = "MAX";
        public static final String MIN = "MIN";
        public static final String AVG = "AVG";
    }

    /**
     * 初始化考试项目表
     *
     * @param projectDao dao
     * @param sql        SQL语句
     */
    public static void initialTable(DAO projectDao, String... sql) {
        for (String s : sql) {
            projectDao.execute(s);
        }
    }

    /**
     * 初始化科目表
     *
     * @param subjectId  科目ID
     * @param projectDao dao
     * @param sql        SQL语句
     */
    public static void initialTable(String subjectId, DAO projectDao, String... sql) {
        if (StringUtil.isBlank(subjectId)) {
            initialTable(projectDao, sql);
            return;
        }

        for (String s : sql) {
            projectDao.execute(replaceSubjectId(s, "{{subjectId}}", subjectId));
        }
    }

    /**
     * 根据维度名称替换查询条件中的range_id
     *
     * @param rangeName 维度名称
     * @param sql       SQL语句
     * @return 返回结果
     */
    public static String replaceRangeId(String rangeName, String sql) {
        return Range.PROVINCE.equals(rangeName) ? sql.replace("{{range_id}}", "province") :
                Range.SCHOOL.equals(rangeName) ? sql.replace("{{range_id}}", "school_id") :
                        Range.CLASS.equals(rangeName) ? sql.replace("{{range_id}}", "class_id") : sql;
    }

    /**
     * 拼接得分的多个分组函数
     *
     * @param sql        SQL语句
     * @param groupTypes 分组函数名称
     * @return 返回结果
     */
    public static String renderGroupType(String sql, String... groupTypes) {
        StringBuilder builder = new StringBuilder();
        for (String groupType : groupTypes) {
            builder.append(groupType).append("(scores.score) ").append(groupType).append(",");
        }
        return sql.replace("{{more_group_type}}", builder.toString());
    }

    public static String replaceGroupType(String groupType, String sql) {
        return "AVG".equals(groupType) ? sql.replace("{{group_type}}", "AVG") :
                "MAX".equals(groupType) ? sql.replace("{{group_type}}", "MAX") :
                        "MIN".equals(groupType) ? sql.replace("{{group_type}}", "MIN") : sql;
    }

    /**
     * 记录中的target_id匹配项目ID或者科目ID
     *
     * @param projectId 项目ID
     * @param subjectId 科目ID
     * @param r         单行记录
     * @return 返回结果
     */
    public static boolean chooseProjectOrSubject(String projectId, String subjectId, Row r) {

        return StringUtils.isEmpty(subjectId) ?
                projectId.equals(r.getString("target_id")) : subjectId.equals(r.getString("target_id"));

    }
}
