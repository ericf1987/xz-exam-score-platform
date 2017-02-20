package com.xz.scorep.executor.project;

import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestService {

    @Autowired
    private DAOFactory daoFactory;

    public void clearQuests(String projectId) {
        daoFactory.getProjectDao(projectId).execute("truncate table quest");
    }

    public void saveQuest(String projectId, ExamQuest examQuest) {
        daoFactory.getProjectDao(projectId).insert(examQuest, "quest");
    }

    public void saveQuest(String projectId, List<ExamQuest> examQuests) {
        daoFactory.getProjectDao(projectId).insert(examQuests, "quest");
    }

    public List<ExamQuest> queryQuests(String projectId) {
        return daoFactory.getProjectDao(projectId).query(ExamQuest.class, "select * from quest");
    }

    public List<ExamQuest> queryQuests(String projectId, String subjectId) {
        return daoFactory.getProjectDao(projectId).query(
                ExamQuest.class, "select * from quest where exam_subject=?", subjectId);
    }
}
