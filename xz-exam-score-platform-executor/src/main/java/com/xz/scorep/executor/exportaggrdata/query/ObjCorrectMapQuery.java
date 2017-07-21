package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.utils.MD5;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.ObjCorrectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author luckylo
 * @createTime 2017-07-20
 */
@Component
public class ObjCorrectMapQuery {

    public static final String QUERY_DATA = "select * from objective_score_rate";

    private static final Logger LOG = LoggerFactory.getLogger(ObjCorrectMapQuery.class);

    @Autowired
    private DAOFactory daoFactory;

    public List<ObjCorrectMap> queryData(String projectId) {
        LOG.info("开始导出  ObjCorrectMap  得分率......");
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<Row> rows = projectDao.query(QUERY_DATA);
        List<ObjCorrectMap> result = rows.stream()
                .map(row -> pakObj(row, projectId))
                .collect(Collectors.toList());
        LOG.info("ObjCorrectMap  导出完毕......");
        return result;
    }

    private ObjCorrectMap pakObj(Row row, String projectId) {
        ObjCorrectMap obj = new ObjCorrectMap();
        Range range = new Range();
        range.setId(row.getString("range_id"));
        range.setName(row.getString("range_type"));

        Target target = new Target();
        target.setName(Target.QUEST);
        target.setId(row.getString("quest_id"));

        obj.setRange(range);
        obj.setTarget(target);
        obj.setCorrectCount(row.getInteger("count", 0));
        obj.setCorrectRate(row.getDouble("score_rate", 0));
        obj.setProject(projectId);
        obj.setMd5(MD5.digest(UUID.randomUUID().toString()));
        return obj;
    }

}
