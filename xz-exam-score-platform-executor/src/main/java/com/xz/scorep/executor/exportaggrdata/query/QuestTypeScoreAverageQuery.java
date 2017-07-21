package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.cryption.MD5;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.QuestTypeScoreAverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author luckylo
 * @createTime 2017-07-21.
 */
@Component
public class QuestTypeScoreAverageQuery {

    private static final String QUERY = "" +
            "select b.* ,(b.average / a.full_score) rate, '{{rangeType}}' range_type from quest_type_list a,(\n" +
            "select score.quest_type_id ,student.{{rangeId}} range_id,\n" +
            "avg(score.score) average  from quest_type_score score,student\n" +
            "where score.student_id = student.id\n" +
            "GROUP BY\n" +
            "student.{{rangeId}},score.quest_type_id\n" +
            ")b\n" +
            "where a.id = b.quest_type_id\n";

    @Autowired
    private DAOFactory daoFactory;

    private static final Logger LOG = LoggerFactory.getLogger(QuestTypeScoreAverageQuery.class);

    public List<QuestTypeScoreAverage> queryData(String projectId) {
        LOG.info("开始查询 QuestTypeScoreAverage 数据 ....");

        List<Row> areaRows = processAreaData(projectId);
        List<Row> cityRows = processCityData(projectId);
        List<Row> provinceRows = processProvinceData(projectId);

        List<Row> classRows = processClassData(projectId);
        List<Row> schoolRows = processSchoolData(projectId);

        List<Row> rows = addAll(areaRows, cityRows, provinceRows, classRows, schoolRows);
        List<QuestTypeScoreAverage> result = rows.stream()
                .map(row -> pakObj(row, projectId))
                .collect(Collectors.toList());
        LOG.info("查询完成 QuestTypeScoreAverage 共 {} 条数据 ....", result.size());
        return result;
    }

    private List<Row> processSchoolData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String sql = QUERY.replace("{{rangeId}}", "school_id").replace("{{rangeType}}", Range.SCHOOL);

        return projectDao.query(sql);
    }

    private List<Row> processClassData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String sql = QUERY.replace("{{rangeId}}", "class_id").replace("{{rangeType}}", Range.CLASS);

        return projectDao.query(sql);
    }

    private List<Row> processProvinceData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String sql = QUERY.replace("{{rangeId}}", Range.PROVINCE).replace("{{rangeType}}", Range.PROVINCE);

        return projectDao.query(sql);
    }

    private List<Row> processCityData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String sql = QUERY.replace("{{rangeId}}", Range.CITY).replace("{{rangeType}}", Range.CITY);

        return projectDao.query(sql);
    }

    private List<Row> processAreaData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String sql = QUERY.replace("{{rangeId}}", Range.AREA).replace("{{rangeType}}", Range.AREA);

        return projectDao.query(sql);
    }

    private List<Row> addAll(List<Row>... lists) {
        List<Row> result = new ArrayList<>();
        for (List<Row> list : lists) {
            result.addAll(list);
        }
        result.removeIf(HashMap::isEmpty);
        return result;
    }


    private QuestTypeScoreAverage pakObj(Row row, String projectId) {
        QuestTypeScoreAverage average = new QuestTypeScoreAverage();

        Range range = new Range();
        range.setName(row.getString("range_type"));
        range.setId(row.getString("range_id"));

        average.setRange(range);
        average.setQuestType(row.getString("quest_type_id"));
        average.setAverage(row.getDouble("average", 0));
        average.setRate(row.getDouble("rate", 0));
        average.setProject(projectId);
        average.setMd5(MD5.digest(UUID.randomUUID().toString()));
        return average;
    }

}
