package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.cryption.MD5;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
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
 * @createTime 2017-07-28.
 */
@Component
public class TopStudentListQuery {

    private static final String QUERY__DATA = "select list.range_type range_name ,list.range_id, list.student_id,\n" +
            "list.target_type target_name,list.target_id ,\n" +
            "student.class_id,student.school_id,score.score,rank.rank\n" +
            "from top_student_list list,score_project score ,\n" +
            "`{{rankTable}}` rank,student\n" +
            "where list.range_type = '{{rangeType}}'\n" +
            "and score.student_id = list.student_id \n" +
            "and list.student_id = rank.student_id\n" +
            "and rank.student_id = student.id\n" +
            "and rank.subject_id =\"000\"";

    private static final Logger LOG = LoggerFactory.getLogger(TopStudentListQuery.class);

    @Autowired
    private DAOFactory daoFactory;


    public List<Map<String, Object>> queryData(String projectId) {
        LOG.info("开始查询 TopStudentList  数据 ... ");
        List<Row> provinceRows = queryProvinceData(projectId);
        List<Row> schoolRows = querySchoolData(projectId);

        //添加到school列表中....
        schoolRows.addAll(provinceRows);
        List<Map<String, Object>> result = schoolRows.stream()
                .map(row -> packObj(row, projectId))
                .collect(Collectors.toList());

        LOG.info("查询完成 TopStudentList , 共 {} 条数据... ", result.size());
        return result;
    }

    private List<Row> querySchoolData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<Row> school = projectDao.query(QUERY__DATA.replace("{{rankTable}}", "rank_school").replace("{{rangeType}}", Range.SCHOOL));
        return school;
    }

    private List<Row> queryProvinceData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<Row> province = projectDao.query(QUERY__DATA.replace("{{rankTable}}", "rank_province").replace("{{rangeType}}", Range.PROVINCE));
        return province;
    }

    private Map<String, Object> packObj(Row row, String projectId) {
        Map<String, Object> map = new HashMap<>();

        Range range = new Range();
        range.setName(row.getString("range_name"));
        range.setName(row.getString("range_id"));

        Target target = new Target();
        target.setName(row.getString("target_name"));
        target.setId(row.getString("target_id"));

        map.put("range", range);
        map.put("target", target);
        map.put("student", row.getString("student_id"));
        map.put("class", row.getString("class_id"));
        map.put("school", row.getString("school_id"));
        map.put("score", row.getDouble("score", 0));
        map.put("rank", row.getInteger("rank", 0));
        map.put("project", projectId);
        map.put("md5", MD5.digest(UUID.randomUUID().toString()));
        return map;
    }
}
