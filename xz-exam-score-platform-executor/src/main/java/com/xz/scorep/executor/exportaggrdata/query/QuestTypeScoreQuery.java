package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.cryption.MD5;
import com.xz.scorep.executor.db.DAOFactory;
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
 * @createTime 2017-07-20.
 */
@Component
public class QuestTypeScoreQuery {

    private static final Logger LOG = LoggerFactory.getLogger(QuestTypeScoreQuery.class);

    public static final String QUERY = "select * from quest_type_score,student\n" +
            "where student_id = student.id";

    @Autowired
    private DAOFactory daoFactory;


    public List<Map<String, Object>> queryData(String projectId) {
        LOG.info("开始导出  QuestTypeScore  数据.....");

        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<Row> rows = projectDao.query(QUERY);
        List<Map<String, Object>> result = rows.stream()
                .map(row -> pakObj(row, projectId))
                .collect(Collectors.toList());
        LOG.info("导出  QuestTypeScore  数据完毕.....");

        return result;
    }

    private Map<String, Object> pakObj(Row row, String projectId) {
        Map<String, Object> result = new HashMap<>();
        result.put("student", row.getString("student_id"));
        result.put("questType", row.getString("quest_type_id"));
        result.put("score", row.getString("score"));
        result.put("rate", row.getString("rate"));
        result.put("class", row.getString("class_id"));
        result.put("school", row.getString("school_id"));
        result.put("area", row.getString("area"));
        result.put("city", row.getString("city"));
        result.put("province", row.getString("province"));
        result.put("project", projectId);
        result.put("md5", MD5.digest(UUID.randomUUID().toString()));

        return result;
    }

}
