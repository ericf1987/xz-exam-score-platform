package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.concurrent.Executors;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.db.DAOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author by fengye on 2017/6/28.
 */
@AggregateTypes(AggregateType.Advanced)
@AggregateOrder(85)
@Component
public class PointAndAbilityLevelSumToRangeAggregator extends Aggregator{

    @Autowired
    DAOFactory daoFactory;

    public static final Logger LOG = LoggerFactory.getLogger(PointAndAbilityLevelSumToRangeAggregator.class);

    public static final String QUERY_SUM_BY_RANGE_ID = "select stu.{{range_id}} range_id, '{{range_type}}' range_type, {{key}}, SUM(score.total_score) total_score" +
            " from student stu, {{table_name}} score " +
            " where stu.id = score.student_id" +
            " group by {{range_id}}, {{key}}";

    public static final String[] TABLE_NAME = new String[]{
            "score_point", "score_point_level", "score_subject_level"
    };

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {

        LOG.info("开始执行 （班级，学校，总体）知识点、能力层级、双向细目 统计 ：{}", this.getClass().getSimpleName());

        long begin = System.currentTimeMillis();

        String projectId = aggregateParameter.getProjectId();

        DAO projectDao = daoFactory.getProjectDao(projectId);

        ThreadPoolExecutor executor = Executors.newBlockingThreadPoolExecutor(10, 10, 10);

        projectDao.execute("truncate table score_point_range");
        projectDao.execute("truncate table score_point_level_range");
        projectDao.execute("truncate table score_subject_level_range");

        for (String rangeName : RANGE_NAMES) {
            for (String tableName : TABLE_NAME) {
                executor.submit(() -> doProcess(projectDao, rangeName, tableName));
            }
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);

        long end = System.currentTimeMillis();

        LOG.info("结束执行 （班级，学校，总体）知识点、能力层级、双向细目 统计:{}， 耗时:{}", this.getClass().getSimpleName(), end - begin);
    }

    private void doProcess(DAO projectDao, String rangeName, String tableName) {
        String sql = getSqlByRangeName(rangeName, tableName);
        List<Row> rows = projectDao.query(sql);
        projectDao.insert(rows, getResultTable(tableName));
    }

    private String getResultTable(String tableName) {
        return tableName + "_range";
    }

    public String getSqlByRangeName(String rangeName, String tableName) {
        String sql = QUERY_SUM_BY_RANGE_ID;
        switch (rangeName){
            case Range.PROVINCE:
                sql = sql.replace("{{range_id}}", "province").replace("{{range_type}}", rangeName);
                break;
            case Range.SCHOOL:
                sql = sql.replace("{{range_id}}", "school_id").replace("{{range_type}}", rangeName);
                break;
            case Range.CLASS:
                sql = sql.replace("{{range_id}}", "class_id").replace("{{range_type}}", rangeName);
                break;
        }

        switch (tableName){
            case "score_point":
                sql = sql.replace("{{key}}", "score.point_id").replace("{{table_name}}", "score_point");
                break;
            case "score_point_level":
                sql = sql.replace("{{key}}", "score.point, score.level").replace("{{table_name}}", "score_point_level");
                break;
            case "score_subject_level":
                sql = sql.replace("{{key}}", "score.subject, score.level").replace("{{table_name}}", "score_subject_level");
                break;
        }
        return sql;
    }}
