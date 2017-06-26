package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.bean.SubjectLevel;
import org.springframework.stereotype.Service;

/**
 * @author by fengye on 2017/6/26.
 */
@Service
public class SubjectLevelService {
    public void updateSubjectLevelFullScore(DAO projectDao, SubjectLevel subjectLevel, double fullScore) {
        String sql = "update subject_level set full_score = ? where level_id = ? and subject_id = ?";
        projectDao.execute(sql, subjectLevel.getLevel(), subjectLevel.getSubject());
    }
}
