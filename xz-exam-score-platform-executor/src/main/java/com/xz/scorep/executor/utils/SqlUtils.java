package com.xz.scorep.executor.utils;

import com.hyd.dao.DAO;
import com.xz.ajiaedu.common.lang.StringUtil;

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
     * @return  返回
     */
    public static String replaceSubjectId(String sql, String tag, String replace) {
        return sql.replace(tag, replace);
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
        }

        for (String s : sql) {
            projectDao.execute(replaceSubjectId(s, "{{subjectId}}", subjectId));
        }
    }

}
