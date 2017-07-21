package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.utils.MD5;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.MaxMin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author luckylo
 * @createTime 2017-07-20.
 */
@Component
public class MaxMinQuery {

    @Autowired
    private DAOFactory daoFactory;

    private static final Logger LOG = LoggerFactory.getLogger(MaxMinQuery.class);

    public static final String QUERY_MIN_MAX_DATA = "select * from max_min_score ";

    public List<MaxMin> queryData(String projectId) {
        LOG.info("开始导出 maxMin 数据.....");
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<Row> query = projectDao.query(QUERY_MIN_MAX_DATA);
        List<MaxMin> collect = query.stream()
                .map(row -> pakObj(row, projectId))
                .collect(Collectors.toList());
        LOG.info("maxMin  导出数据完毕.....");
        return collect;
    }


    private MaxMin pakObj(Row row, String projectId) {
        MaxMin maxMin = new MaxMin();

        Range range = new Range();
        range.setId(row.getString("range_id"));
        range.setName(row.getString("range_type"));

        Target target = new Target();
        target.setId(row.getString("target_id"));
        target.setName(row.getString("target_type"));

        maxMin.setRange(range);
        maxMin.setTarget(target);
        maxMin.setMax(row.getDouble("max_score", 0));
        maxMin.setMin(row.getDouble("min_score", 0));
        maxMin.setProject(projectId);
        maxMin.setMd5(MD5.digest(UUID.randomUUID().toString()));
        return maxMin;
    }

}
