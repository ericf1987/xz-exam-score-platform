package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ScoreDetailService {

    @Autowired
    private QuestService questService;

    @Autowired
    private DAOFactory daoFactory;

    public Map<String, Double> getStudentSubjectScoreDetail(String projectId, String studentId, String subjectId) {
        Map<String, Double> result = new HashMap<>();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        questService.queryQuests(projectId, subjectId).forEach(quest -> {
            String tableName = "score_" + quest.getId();
            Row row = projectDao.queryFirst("select score from `" + tableName + "` where student_id=?", studentId);
            if (row != null) {
                result.put(quest.getId(), row.getDouble("score", 0));
            }
        });

        return result;
    }
}
