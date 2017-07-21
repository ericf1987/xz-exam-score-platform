package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.cryption.MD5;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.OverAverage;
import com.xz.scorep.executor.project.SubjectService;
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
 * @createTime 2017-07-20.
 */
@Component
public class OverAverageQuery {

    private static final String QUERY = "select * from {{table}}";

    private static final Logger LOG = LoggerFactory.getLogger(OverAverageQuery.class);

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private SubjectService subjectService;


    public List<OverAverage> queryData(String projectId) {
        LOG.info("开始查询 OverAverage  数据....");
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<Row> projectOverAverage = projectDao.query(QUERY.replace("{{table}}", "over_average_project"));

        List<Row> subjectOverAverage = subjectService.listSubjects(projectId)
                .stream()
                .map(subject -> querySubjectData(projectDao, subject))
                .flatMap(x -> x.stream())
                .collect(Collectors.toList());
        List<Row> list = addAll(projectOverAverage, subjectOverAverage);
        List<OverAverage> result = list.stream()
                .map(li -> pakObj(li, projectId))
                .collect(Collectors.toList());
        LOG.info("查询完成 OverAverage  共 {} 条数据....", result.size());
        return result;
    }

    private List<Row> addAll(List<Row>... rows) {
        List<Row> result = new ArrayList<>();
        for (List<Row> r : rows) {
            result.addAll(r);
        }
        result.removeIf(HashMap::isEmpty);
        return result;
    }

    private List<Row> querySubjectData(DAO projectDao, ExamSubject subject) {
        String table = "over_average_" + subject.getId();
        return projectDao.query(QUERY.replace("{{table}}", table));
    }

    private OverAverage pakObj(Row row, String projectId) {
        OverAverage overAverage = new OverAverage();

        Range range = new Range();
        range.setName(row.getString("range_type"));
        range.setId(row.getString("range_id"));

        Target target = new Target();
        target.setName(row.getString("target_type"));
        target.setId(row.getString("target_id"));

        overAverage.setRange(range);
        overAverage.setTarget(target);
        overAverage.setOverAverage(row.getDouble("over_average", 0));
        overAverage.setProject(projectId);
        overAverage.setMd5(MD5.digest(UUID.randomUUID().toString()));
        return overAverage;
    }

}
