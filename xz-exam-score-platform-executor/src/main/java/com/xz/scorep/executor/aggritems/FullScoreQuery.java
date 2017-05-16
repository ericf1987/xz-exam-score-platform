package com.xz.scorep.executor.aggritems;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.utils.SqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author by fengye on 2017/5/7.
 */
@Component
public class FullScoreQuery {

    @Autowired
    DAOFactory daoFactory;

    public static final String PROJECT_SCORE_SQL = "SELECT full_score FROM project";

    public static final String SUBJECT_SCORE_SQL = "SELECT full_score from subject where id = {{subjectId}}";

    public Double getFullScore(String projectId, Target target){
        DAO managerDao = daoFactory.getManagerDao();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        if(target.match(Target.PROJECT)){
            Row row = managerDao.queryFirst(PROJECT_SCORE_SQL);
            return row.getDouble("full_score", 0);
        }else if(target.match(Target.SUBJECT)){
            Row row = projectDao.queryFirst(SqlUtils.replaceSubjectId(SUBJECT_SCORE_SQL, "{{subjectId}}", target.getId().toString()));
            return row.getDouble("full_score", 0);
        }
        return 0d;
    }
}
