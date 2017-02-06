package com.xz.scorep.executor.project;

import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.db.DbiHandleFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestService {

    @Autowired
    private DbiHandleFactory dbiHandleFactory;

    public static final String SQL_INSERT = "INSERT INTO quest(" +
            "  id, exam_subject, quest_subject, question_type_id, " +
            "  question_type_name, objective, quest_no, full_score, " +
            "  answer, score_rule, `options`" +
            ")VALUES(?,?,?,?,?,?,?,?,?,?,?)";

    public void clearQuests(String projectId) {
        dbiHandleFactory.getProjectDBIHandle(projectId).runHandle(handle -> {
            handle.execute("truncate table quest");
        });
    }

    public void saveQuest(String projectId, ExamQuest examQuest) {

        dbiHandleFactory.getProjectDBIHandle(projectId).runHandle(handle -> {
            handle.insert(SQL_INSERT,
                    examQuest.getId(), examQuest.getExamSubject(), examQuest.getQuestSubject(),
                    examQuest.getQuestionTypeId(), examQuest.getQuestionTypeName(),
                    Boolean.toString(examQuest.isObjective()), examQuest.getQuestNo(),
                    examQuest.getFullScore(), examQuest.getAnswer(), examQuest.getScoreRule(),
                    examQuest.getOptions());
        });
    }
}
