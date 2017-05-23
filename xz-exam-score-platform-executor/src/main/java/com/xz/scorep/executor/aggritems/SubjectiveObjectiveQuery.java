package com.xz.scorep.executor.aggritems;

import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.project.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 主观题客观题得分详情查询
 *
 * @author luckylo
 */
@Component
public class SubjectiveObjectiveQuery {

    private static final String QUERY_SUBJECTIVE_SCORE_DETAIL = "";

    private static final String QUERY_OBJECTIVE_SCORE_DETAIL = "";

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private QuestService questService;

    public List<String> querySubjectiveScoreDetail(String projectId, String subjectId, String studentId) {
        return null;
    }

    public List<String> queryObjectiveScoreDetail(String projectId, String subjectId, String studentId) {
        return null;
    }
}
