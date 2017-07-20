package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.cryption.MD5;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.QuestDeviation;
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
public class QuestDeviationQuery {

    public static final String QUERY = "select * quest_deviation";

    private static final Logger LOG = LoggerFactory.getLogger(QuestDeviationQuery.class);

    @Autowired
    private DAOFactory daoFactory;

    public List<QuestDeviation> queryData(String projectId) {
        LOG.info("开始导出題目区分度....");
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<Row> rows = projectDao.query(QUERY);
        List<QuestDeviation> result = rows.stream()
                .map(row -> pakObj(row, projectId))
                .collect(Collectors.toList());
        LOG.info("題目区分度导出完毕.... result size {}", result.size());
        return result;
    }

    private QuestDeviation pakObj(Row row, String projectId) {
        QuestDeviation questDeviation = new QuestDeviation();

        Range range = new Range();
        range.setName(row.getString("range_type"));
        range.setId(row.getString("range_id"));

        questDeviation.setRange(range);
        questDeviation.setQuest(row.getString("quest_id"));
        questDeviation.setDeviation(row.getDouble("value", 0));
        questDeviation.setProject(projectId);
        questDeviation.setMd5(MD5.digest(UUID.randomUUID().toString()));
        return questDeviation;
    }

}
