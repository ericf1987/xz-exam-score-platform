package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.cryption.MD5;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.TopAverage;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author luckylo
 * @createTime 2017-07-24.
 */
@Component
public class TopAverageQuery {

    private static final String QUERY = "select * from high_score";

    private static final Logger LOG = LoggerFactory.getLogger(TopAverageQuery.class);

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private ReportConfigService reportConfigService;

    public List<TopAverage> queryData(String projectId) {
        LOG.info("开始查询  TopAverage  数据 ....");
        DAO projectDao = daoFactory.getProjectDao(projectId);
        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
        double scoreRate = reportConfig.getHighScoreRate();
        List<Row> rows = projectDao.query(QUERY);
        List<TopAverage> result = rows.stream()
                .map(row -> pakObj(row, projectId, scoreRate))
                .collect(Collectors.toList());

        LOG.info("查询  TopAverage  完成,共 {} 条数据 ....", result.size());
        return result;
    }

    private TopAverage pakObj(Row row, String projectId, double highScoreRate) {
        TopAverage topAverage = new TopAverage();
        Range range = new Range();
        range.setName(row.getString("range_type"));
        range.setId(row.getString("range_id"));

        topAverage.setRange(range);

        Target target = new Target();
        target.setName(row.getString("target_type"));
        target.setId(row.getString("target_id"));

        topAverage.setTarget(target);

        Map<String, Object> map = new HashMap<>();
        map.put("average", row.getDouble("score", 0));
        map.put("percent", highScoreRate);

        topAverage.setTopAverages(map);
        topAverage.setProject(projectId);
        topAverage.setMd5(MD5.digest(UUID.randomUUID().toString()));

        return topAverage;
    }
}
