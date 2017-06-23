package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.concurrent.Executors;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import com.xz.scorep.executor.utils.DoubleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 计算排名分段内的人数和占比
 *
 * @author by fengye on 2017/6/21.
 */
@AggregateTypes(AggregateType.Basic)
@AggregateOrder(80)
@Component
public class RankSegmentAggregator extends Aggregator {

    @Autowired
    ReportConfigService reportConfigService;

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    SubjectService subjectService;

    public static final Logger LOG = LoggerFactory.getLogger(RankSegmentAggregator.class);

    public static final String QUERY_RANK_BY_SUBJECT = "SELECT stu.{{range_id}} range_id, rank.`student_id`, rank.`subject_id`, rank.`rank`\n" +
            "FROM {{rank_table_name}} rank, student stu\n" +
            "where rank.`student_id` = stu.`id`\n" +
            "ORDER BY stu.`class_id`,rank.`subject_id`, rank.`rank`";

    public static final String QUERY_RANK_COUNT = "SELECT rank.`subject_id`, stu.{{range_id}} range_id, COUNT(1) cnt\n" +
            "from {{rank_table_name}} rank, student stu\n" +
            "WHERE rank.`student_id` = stu.`id`\n" +
            "GROUP BY subject_id, range_id\n" +
            "ORDER BY range_id, subject_id";

    public double[] getRankSegmentParam(String projectId) {
        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);

        int rankSegmentCount = reportConfig.getRankSegmentCount();

        double[] segments = new double[rankSegmentCount];

        for (int i = 0; i < rankSegmentCount; i++) {
            segments[i] = DoubleUtils.round((double) (i + 1) / rankSegmentCount, false);
        }

        return segments;
    }

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {


        LOG.info("开始执行 排名分段 统计 ：{}", this.getClass().getSimpleName());

        long begin = System.currentTimeMillis();

        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table rank_segment");

        //processData(projectDao, Range.SCHOOL, getRankSegmentParam(projectId));

        ThreadPoolExecutor executor = Executors.newBlockingThreadPoolExecutor(3, 3, 3);

        for (String rangeName : RANGE_NAMES) {
            if (Range.PROVINCE.equals(rangeName)) continue;
            executor.submit(() -> processData(projectDao, rangeName, getRankSegmentParam(projectId)));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);

        long end = System.currentTimeMillis();

        LOG.info("结束执行 排名分段 统计:{}， 耗时:{}", this.getClass().getSimpleName(), end - begin);
    }

    private void processData(DAO projectDao, String rangeName, double[] rankSegmentParam) {
        //1.查询排名表中所有班级和科目的排名明细
        //2.查询排名表中所有班级和科目的参考人数
        //3.根据排名分段查询每个排名区间内的人数

        String sql_detail = getSqlByRangeName(QUERY_RANK_BY_SUBJECT, rangeName);
        String sql_count = getSqlByRangeName(QUERY_RANK_COUNT, rangeName);

        List<Row> row_detail = projectDao.query(sql_detail);
        List<Row> row_count = projectDao.query(sql_count);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Row r_c : row_count) {
            int cnt = r_c.getInteger("cnt", 0);
            String subject_id = r_c.getString("subject_id");
            String range_id = r_c.getString("range_id");
            double[] rankInSegment = queryRankInSegment(rankSegmentParam, cnt);

            List<Row> current = row_detail.stream().filter(
                    r_d -> subject_id.equals(r_d.getString("subject_id")) && range_id.equals(r_d.getString("range_id"))
            ).collect(Collectors.toList());

            List<Map<String, Object>> rankSegments = calculateRankSegment(current, rankInSegment, cnt);
            rankSegments.stream().forEach(r -> {
                r.put("range_type", rangeName);
                r.put("subject_id", subject_id);
                r.put("range_id", range_id);
            });

            result.addAll(rankSegments);
        }

        projectDao.insert(result, "rank_segment");
    }

    private List<Map<String, Object>> calculateRankSegment(List<Row> current, double[] rankInSegment, int cnt) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < rankInSegment.length; i++) {

            double head, tail;
            if (i == 0) {
                head = 0;
                tail = rankInSegment[i];
            } else {
                head = rankInSegment[i - 1];
                tail = rankInSegment[i];
            }

            //排名位于排名分段之间
            int count = current.stream().filter(c -> match(c.getIntegerObject("rank"), head, tail)).collect(Collectors.toList()).size();
            Map<String, Object> map = new HashMap<>();
            map.put("rank_percent", rankInSegment[i]);
            map.put("segment_count", count);
            map.put("segment_rate", DoubleUtils.round((double) count / cnt, false));
            result.add(map);
        }
        return result;
    }

    private boolean match(Integer rank, double head, double tail) {
        return rank > head && rank <= tail;
    }

    public double[] queryRankInSegment(double[] rankSegmentParam, int cnt) {
        double[] rankSegment = new double[rankSegmentParam.length];
        for (int i = 0; i < rankSegmentParam.length; i++) {
            rankSegment[i] = DoubleUtils.round(rankSegmentParam[i] * cnt, false);
        }
        return rankSegment;
    }

    private String getSqlByRangeName(String sql, String rangeName) {
        switch (rangeName) {
            case Range.SCHOOL:
                sql = sql.replace("{{rank_table_name}}", "rank_school").
                        replace("{{range_id}}", "school_id");
                break;
            case Range.CLASS:
                sql = sql.replace("{{rank_table_name}}", "rank_class").
                        replace("{{range_id}}", "class_id");
                break;
        }

        return sql;
    }
}
