package com.xz.scorep.executor.scorecheck;

import com.hyd.dao.Row;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author luckylo
 * @createTime 2017-06-13.
 */
@Service
public class LowerScoreService {

    public static final String QUERY_LOWER_SCORE = "select * from {{table}} where score < {{score}} or score = {{score}}";

    @Autowired
    private DAOFactory daoFactory;


    public List<Row> queryProjectLowerScoreStudent(String projectId, double score) {
        String sql = QUERY_LOWER_SCORE.replace("{{score}}", String.valueOf(score))
                .replace("{{table}}", "score_project");
        return daoFactory.getProjectDao(projectId).query(sql);
    }


    public List<Row> querySubjectLowerScoreStudent(String projectId, String subjectId, double score) {
        String replace = QUERY_LOWER_SCORE.replace("{{table}}", "score_subject_" + subjectId)
                .replace("{{score}}", String.valueOf(score));

        return daoFactory.getProjectDao(projectId).query(replace);
    }


    public List<Row> querySubjectiveLowerScoreStudent(String projectId, String subjectId, double score) {
        String replace = QUERY_LOWER_SCORE.replace("{{table}}", "score_subjective_" + subjectId)
                .replace("{{score}}", String.valueOf(score));

        return daoFactory.getProjectDao(projectId).query(replace);
    }


    public List<Row> queryObjectiveLowerScoreStudent(String projectId, String subjectId, double score) {
        String replace = QUERY_LOWER_SCORE.replace("{{table}}", "score_objective_" + subjectId)
                .replace("{{score}}", String.valueOf(score));

        return daoFactory.getProjectDao(projectId).query(replace);
    }

}
