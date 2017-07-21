package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.ScoreRate;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.utils.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.xz.scorep.executor.exportaggrdata.utils.AggrBeanUtils.setTarget;

/**
 * @author by fengye on 2017/7/21.
 */
@Component
public class ScoreRateQuery {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    CacheFactory cacheFactory;

    @Autowired
    SubjectService subjectService;

    public static final String QUERY_DATA_SUBJECT = "select * from score_rate_{{subject_id}}";

    public static final String QUERY_DATA_PROJECT = "select * from score_rate_project";

    static final Logger LOG = LoggerFactory.getLogger(ScoreRateQuery.class);

    public List<ScoreRate> queryObj(String projectId){

        LOG.info("开始查询 score_rate 数据.....");

        DAO projectDao = daoFactory.getProjectDao(projectId);

        List<ExamSubject> examSubjects = subjectService.listSubjects(projectId);

        List<ScoreRate> scoreRates = new ArrayList<>();

        for (ExamSubject examSubject : examSubjects) {
            String subjectId = examSubject.getId();

            String sql = SqlUtils.replaceSubjectId(QUERY_DATA_SUBJECT, subjectId);

            List<Row> rows = projectDao.query(sql);

            scoreRates.addAll(packScoreRates(projectId, subjectId, rows));
        }

        List<Row> rows = projectDao.query(QUERY_DATA_PROJECT);

        scoreRates.addAll(packScoreRates(projectId, null, rows));

        LOG.info("查询完成 score_rate 共 {} 条.....", scoreRates.size());

        return scoreRates;
    }

    private List<ScoreRate> packScoreRates(String projectId, String subjectId, List<Row> rows) {
        return rows.stream().map(row -> packOneScoreRate(projectId, subjectId, row)).collect(Collectors.toList());
    }

    private ScoreRate packOneScoreRate(String projectId, String subjectId, Row row) {

        ScoreRate scoreRate = new ScoreRate();

        Range range = new Range();
        range.setId(row.getString("student_id"));
        range.setName(Range.STUDENT);

        Target target = new Target();
        setTarget(projectId, subjectId, target);

        scoreRate.setAggrObject(scoreRate, projectId);
        scoreRate.setRange(range);
        scoreRate.setTarget(target);
        scoreRate.setScoreLevel(row.getString("score_level"));
        scoreRate.setScoreRate(row.getDouble("score_rate", 0));

        return scoreRate;
    }

}
