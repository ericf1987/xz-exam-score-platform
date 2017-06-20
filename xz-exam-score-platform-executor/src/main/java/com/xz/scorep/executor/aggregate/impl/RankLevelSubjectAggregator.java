package com.xz.scorep.executor.aggregate.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.concurrent.Executors;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.ProjectService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 根据学生的排名计算排名等级
 *
 * @author by fengye on 2017/6/19.
 */
@AggregateTypes(AggregateType.Basic)
@AggregateOrder(70)
@Component
public class RankLevelSubjectAggregator extends Aggregator {

    //查询维度下各个科目参考总人数
    public static final String QUERY_COUNT_BY_RANGE_SUBJECT = "SELECT rank.`subject_id` subject_id, s.{{range_id}} range_id, COUNT(1) cnt\n" +
            "FROM {{rank_data_table}} rank, student s\n" +
            "WHERE rank.`student_id` = s.`id`\n" +
            "GROUP BY subject_id, range_id";

    //根据维度查询所有参考人数明细
    public static final String QUERY_DETAIL_BY_RANGE_SUBJECT = "SELECT rank.`rank`, rank.`subject_id`, s.`id` student_id, s.{{range_id}} range_id\n" +
            "FROM {{rank_data_table}} rank, student s\n" +
            "WHERE rank.`student_id` = s.`id`\n";

    @Autowired
    ProjectService projectService;

    @Autowired
    SchoolService schoolService;

    @Autowired
    ClassService classService;

    @Autowired
    SubjectService subjectService;

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    ReportConfigService reportConfigService;

    public static final String[] RANGES = new String[]{
            Range.PROVINCE, Range.SCHOOL, Range.CLASS
    };

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        //1.获取项目的排名等级参数
        //2.获取各个维度的参考学生人数
        //3.获取各个维度下每个学生的排名
        String projectId = aggregateParameter.getProjectId();
        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);

        String rankLevels = reportConfig.getRankLevels();

        Map<String, Double> rankLevelMap = (Map) JSONUtils.parse(rankLevels);

        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table rank_level_province");
        projectDao.execute("truncate table rank_level_school");
        projectDao.execute("truncate table rank_level_class");

        ThreadPoolExecutor executor = Executors.newBlockingThreadPoolExecutor(5, 5, 5);

        for (String rangeName : RANGES) {
            executor.submit(() -> processData(projectDao, rankLevelMap, rangeName));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);
    }

    private void processData(DAO projectDao, Map<String, Double> rankLevelMap, String rangeName) {
        List<Row> count_rows = getDetailOrCount(rangeName, QUERY_COUNT_BY_RANGE_SUBJECT, projectDao);
        List<Row> detail_rows = getDetailOrCount(rangeName, QUERY_DETAIL_BY_RANGE_SUBJECT, projectDao);
        detail_rows.forEach(d -> {
            String range_id = d.getString("range_id");
            String subject_id = d.getString("subject_id");
            int rank = d.getInteger("rank", 0);
            Optional<Row> optional = count_rows.stream().filter(c -> range_id.equals(c.getString("range_id")) && subject_id.equals(c.getString("subject_id"))).findFirst();
            int count = optional.isPresent() ? optional.get().getInteger("cnt", 0) : 0;
            //追加总参考人数
            d.put("cnt", count);
            //追加排名等级参数
            d.put("rank_level", calculateRankLevel(rank, count, rankLevelMap));
        });

        //执行插入
        projectDao.insert(detail_rows, getTableName(rangeName));
    }

    private String getTableName(String rangeName) {
        String tableName = "";
        switch (rangeName) {
            case Range.PROVINCE:
                tableName = "rank_level_province";
                break;
            case Range.SCHOOL:
                tableName = "rank_level_school";
                break;
            case Range.CLASS:
                tableName = "rank_level_class";
                break;
        }
        return tableName;
    }

    private String calculateRankLevel(int rank, int count, Map<String, Double> rankLevels) {
        List<String> levelKeys = new ArrayList<>(rankLevels.keySet());
        Collections.sort(levelKeys);

        double sum = 0, rankLevelValue = count == 0 ? 0 : ((double) rank / count);

        if (rankLevelValue == 1) {
            for (int i = levelKeys.size() - 1; i >= 0; i--) {
                if (rankLevels.get(levelKeys.get(i)) != 0) {
                    return levelKeys.get(i);
                }
            }
        }

        for (String levelKey : levelKeys) {
            sum += rankLevels.get(levelKey);
            if (rankLevelValue <= sum) {
                return levelKey;
            }
        }

        throw new IllegalStateException("无法找到排名等级");
    }

    private List<Row> getDetailOrCount(String rangeName, String sql, DAO dao) {
        switch (rangeName) {
            case Range.PROVINCE:
                sql = sql.replace("{{rank_data_table}}", "rank_province").
                        replace("{{range_id}}", "province");
                break;
            case Range.SCHOOL:
                sql = sql.replace("{{rank_data_table}}", "rank_school").
                        replace("{{range_id}}", "school_id");
                break;
            case Range.CLASS:
                sql = sql.replace("{{rank_data_table}}", "rank_class").
                        replace("{{range_id}}", "class_id");
                break;
        }

        return dao.query(sql);
    }

}
