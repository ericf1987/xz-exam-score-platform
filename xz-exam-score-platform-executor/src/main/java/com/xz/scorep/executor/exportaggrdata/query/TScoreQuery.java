package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.cryption.MD5;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.TScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author luckylo
 * @createTime 2017-07-24.
 */
@Component
public class TScoreQuery {

    private static final String QUERY = "select * from t_value";

    @Autowired
    DAOFactory daoFactory;

    private static final Logger LOG = LoggerFactory.getLogger(TScoreQuery.class);

    public List<TScore> queryData(String projectId) {
        LOG.info("开始查询 TScore 数据.....");

        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<Row> rows = projectDao.query(QUERY);
        List<TScore> result = rows.stream()
                .map(row -> pakObj(row, projectId))
                .collect(Collectors.toList());

        LOG.info("查询完成 TScore 共 {} 条.....", result.size());
        return result;
    }

    private TScore pakObj(Row row, String projectId) {
        TScore tScore = new TScore();

        Range range = new Range();
        range.setName(row.getString("range_type"));
        range.setId(row.getString("range_id"));

        Target target = new Target();
        target.setName(Target.SUBJECT);
        target.setId(row.getString("target_id"));

        tScore.setRange(range);
        tScore.setTarget(target);
        tScore.settScore(row.getDouble("value", 0));
        tScore.setProject(projectId);
        tScore.setMd5(MD5.digest(UUID.randomUUID().toString()));

        return tScore;
    }

}
