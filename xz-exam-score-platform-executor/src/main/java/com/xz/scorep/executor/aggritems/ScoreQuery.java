package com.xz.scorep.executor.aggritems;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.dao.SQL;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScoreQuery {

    @Autowired
    private DAOFactory daoFactory;

    public List<Row> listStudentScore(String projectId, Range range, Target target) {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        String tableName, scoreAlias;

        if (target.match(Target.PROJECT)) {
            tableName = "score_project";
            scoreAlias = "score_000";
        } else if (target.match(Target.SUBJECT) || target.match(Target.SUBJECT_COMBINATION)) {
            tableName = "score_subject_" + target.getId();
            scoreAlias = "score_" + target.getId();
        } else if (target.match(Target.QUEST)) {
            tableName = "score_" + target.getId();
            scoreAlias = "score_" + target.getId();
        } else {
            throw new IllegalArgumentException("Unsupported target " + target);
        }

        return projectDao.query(SQL.Select("score.student_id", "score.score as " + scoreAlias)
                .From(tableName + " score", "student")
                .Where("score.student_id=student.id")
                .And(range.match(Range.CLASS), "student.class_id=?", range.getId())
                .And(range.match(Range.SCHOOL), "student.school_id=?", range.getId()));
    }
}
