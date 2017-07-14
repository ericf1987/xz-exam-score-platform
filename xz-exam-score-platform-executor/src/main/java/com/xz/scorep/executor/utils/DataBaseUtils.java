package com.xz.scorep.executor.utils;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.aggregate.AggregateStatus;
import com.xz.scorep.executor.db.DAOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author luckylo
 * @createTime 2017-07-13.
 */
public class DataBaseUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DataBaseUtils.class);

    public static final String QUERY = "" +
            "select * from aggregation where project_id = '{{projectId}}' " +
            "and aggr_type = 'Quick' {{subject}} " +
            "ORDER BY start_time desc ;";

    public static final String SUB = " and subject_id = '";

    /**
     * 全科统计,且Quick完成  --->取原始库(继续生成pdf)
     * 全科统计,且Quick未完成  --->取该科目备份库(终止生成)
     * <p>
     * 单科统计,同一科目,且Quick完成  --->取原始库和备份库没区别
     * 单科统计,同一科目,且Quick未完成  --->取该科目备份库(终止生成)
     * <p>
     * (这两种情况:取备份库,数据更改则要求重新生成)
     * 单科统计,不同科目,且Quick完成
     * --->1:当正在生成pdf的科目数据没发生更改,则取原始库和备份库没区别;
     * --->2:当正在生成pdf的科目数据发生更改,则需要取原始库数据.
     * <p>
     * 单科统计,不同科目,且Quick未完成
     * --->取该科目备份库(当正在生成pdf的科目数据发生更改,此时应该终止生成pdf)
     *
     * @param projectId  项目ID
     * @param subjectId  科目ID
     * @param daoFactory dao
     * @return projectId / databaseName / "" (返回""表示此时应该终止生成pdf)
     */
    //获取数据库名
    public static String getDataBaseName(String projectId, String subjectId, DAOFactory daoFactory) {
        String dataBase = projectId + "_" + subjectId + "_bak";

        DAO managerDao = daoFactory.getManagerDao();
        String allSubject = QUERY.replace("{{projectId}}", projectId).replace("{{subject}}", "");

        Row row = managerDao.queryFirst(allSubject);
        if (row == null || row.isEmpty()) {
            return "";
        }

        //判断第一条是否全科统计,是判断第一条,不是则判断后面的
        if (StringUtil.isEmpty(row.getString("subject_id"))) {
            return checkDatabaseAndReturn(projectId, row);
        }


        String subjectSql = QUERY.replace("{{projectId}}", projectId)
                .replace("{{subject}}", SUB + subjectId + "'");
        Row subjectRow = managerDao.queryFirst(subjectSql);
        //尚未执行单科统计
        if (subjectRow == null || subjectRow.isEmpty()) {
            return "";
        }
        //执行过单科统计
        if (subjectId.equals(row.getString("subject_id"))) {
            return checkDatabaseAndReturn(dataBase, subjectRow);
        } else {
            return dataBase;
        }

    }

    private static String checkDatabaseAndReturn(String projectId, Row row) {
        if (AggregateStatus.Finished.name().equals(row.getString("status"))) {
            return projectId;
        } else {
            return "";
        }
    }

}
