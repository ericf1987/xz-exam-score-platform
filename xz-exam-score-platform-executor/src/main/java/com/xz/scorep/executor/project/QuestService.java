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

    private static final String SQL_INSERT = "INSERT INTO quest(" +
            "  id, exam_subject, quest_subject, question_type_id, " +
            "  question_type_name, objective, quest_no, full_score, " +
            "  answer, score_rule, `options`" +
            ")VALUES(?,?,?,?,?,?,?,?,?,?,?)";

    public void clearQuests(String projectId) {
        daoFactory.getProjectDao(projectId).execute("truncate table quest");
    }

    public void saveQuest(String projectId, ExamQuest examQuest) {
        daoFactory.getProjectDao(projectId).execute(SQL_INSERT,
                examQuest.getId(), examQuest.getExamSubject(), examQuest.getQuestSubject(),
                examQuest.getQuestionTypeId(), examQuest.getQuestionTypeName(),
                Boolean.toString(examQuest.isObjective()), examQuest.getQuestNo(),
                examQuest.getFullScore(), examQuest.getAnswer(), examQuest.getScoreRule(),
                examQuest.getOptions());
    }

    public List<ExamQuest> queryQuests(String projectId) {
        return daoFactory.getProjectDao(projectId).query(ExamQuest.class, "select * from quest");
    }

    public List<ExamQuest> queryQuests(String projectId, String subjectId) {
        return daoFactory.getProjectDao(projectId).query(
                ExamQuest.class, "select * from quest where exam_subject=?", subjectId);
    }
}
