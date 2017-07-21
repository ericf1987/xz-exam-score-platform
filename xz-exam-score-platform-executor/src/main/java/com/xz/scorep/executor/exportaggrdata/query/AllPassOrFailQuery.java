package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.utils.MD5;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.AllPassOrFail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 统计数据查询-全科及格/不及格率
 *
 * @author by fengye on 2017/7/17.
 */
@Component
public class AllPassOrFailQuery {
    @Autowired
    DAOFactory daoFactory;

    private static final Logger LOG = LoggerFactory.getLogger(AllPassOrFailQuery.class);

    public static final String QUERY_DATA = "select * from all_pass_or_fail";

    public List<Row> queryData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        List<Row> rows = projectDao.query(QUERY_DATA);

        return rows;
    }

    public List<AllPassOrFail> queryObj(String projectId) {
        LOG.info("开始导出  AllPassOrFail  数据.....");
        List<Row> rows = queryData(projectId);

        List<AllPassOrFail> result = rows.stream().map(r -> packObj(projectId, r)).collect(Collectors.toList());
        LOG.info("AllPassOrFail  数据导出完毕.....");
        return result;
    }

    private AllPassOrFail packObj(String projectId, Row r) {
        AllPassOrFail allPassOrFail = new AllPassOrFail();
        Range range = new Range();
        range.setName(r.getString("range_type"));
        range.setId(r.getString("range_id"));
        allPassOrFail.setRange(range);
        allPassOrFail.setAllPassCount(r.getInteger("all_pass_count", 0));
        allPassOrFail.setAllPassRate(r.getDouble("all_pass_rate", 0));
        allPassOrFail.setAllFailCount(r.getInteger("all_fail_count", 0));
        allPassOrFail.setAllFailRate(r.getDouble("all_fail_rate", 0));

        allPassOrFail.setProject(projectId);
        allPassOrFail.setMd5(MD5.digest(UUID.randomUUID().toString()));

        return allPassOrFail;
    }
}
