package com.xz.scorep.executor.scorecheck;

import com.hyd.dao.Row;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author luckylo
 * @createTime 2017-06-13.
 */
@Service
public class LowerScoreService {

    public static final String QUERY_LOWER_SCORE = "" +
            "select su.student_id,su.score subject_score,\n" +
            "  sub.score subjective_score,obj.score objective_score\n" +
            "  from score_subject_{{subjectId}} su,score_subjective_{{subjectId}} sub,score_objective_{{subjectId}} obj\n" +
            "where \n" +
            "  su.student_id = sub.student_id\n" +
            "  and su.student_id = obj.student_id\n" +
            "  and {{condition}}\n" + // su.score <= {{lowerScore}};su -->subject;sub -->subjective;obj -->objective
            "  order by su.score desc\n" +
            "  limit {{count}}";

    @Autowired
    private DAOFactory daoFactory;

    public Map<String, List<Row>> querySubjectLowerScoreStudent(
            String projectId, List<String> subjectIds, String checkType, double score) {

        Map<String, List<Row>> result = new HashMap<>();
        subjectIds.forEach(subjectId -> {
            List<Row> rows = queryLowerScoreStudent(projectId, checkType, subjectId, score);
            result.put(subjectId, rows);
        });

        return result;
    }

    private List<Row> queryLowerScoreStudent(String projectId, String checkType, String subjectId, double score) {
        String tmp = QUERY_LOWER_SCORE.replace("{{subjectId}}", subjectId)
                .replace("{{count}}", "1000");

        if ("objective".equals(checkType)) {
            return daoFactory.getProjectDao(projectId)
                    .query(tmp.replace("{{condition}}", "obj.score <= " + score));
        } else if ("subjective".equals(checkType)) {
            return daoFactory.getProjectDao(projectId)
                    .query(tmp.replace("{{condition}}", "sub.score <= " + score));
        } else {
            return daoFactory.getProjectDao(projectId)
                    .query(tmp.replace("{{condition}}", "su.score <= " + score));

        }
    }


}
