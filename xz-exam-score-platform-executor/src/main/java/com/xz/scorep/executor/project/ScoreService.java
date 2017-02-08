package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.xz.ajiaedu.common.lang.MapBuilder;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.db.MultipleBatchExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ScoreService {

    private static final String SCORE_TABLE_COLUMNS = "(student_id varchar(36), score decimal(4,1))";

    @Autowired
    private DAOFactory daoFactory;

    private Map<String, MultipleBatchExecutor> batchExecutorMap = new HashMap<>();

    private String getTableName(String questId) {
        return "score_" + questId;
    }

    public void createQuestScoreTable(String projectId, ExamQuest examQuest) {
        String questId = examQuest.getId();
        String tableName = getTableName(questId);
        String comment = examQuest.getExamSubject() + ":" + examQuest.getQuestNo();
        DAO dao = daoFactory.getProjectDao(projectId);
        dao.execute("create table " + tableName + SCORE_TABLE_COLUMNS + " comment '" + comment + "'");
        dao.execute("create index idx_score_stu_" + tableName + " on " + tableName + "(student_id)");
    }

    private MultipleBatchExecutor getMultipleBatchExecutor(String projectId) {
        MultipleBatchExecutor batchExecutor = batchExecutorMap.get(projectId);
        if (batchExecutor == null) {
            throw new IllegalStateException("BatchExecutor for project '" + projectId + "' not exists.");
        }
        return batchExecutor;
    }

    public void prepareBatch(String projectId) {
        MultipleBatchExecutor executor = new MultipleBatchExecutor(daoFactory.getProjectDao(projectId), 500);
        batchExecutorMap.put(projectId, executor);
    }

    public void saveScoreBatch(String projectId, String questId, String studentId, double score) {

        String table = "score_" + questId;
        Map<String, Object> row = MapBuilder.<String, Object>start("student_id", studentId).and("score", score).get();

        getMultipleBatchExecutor(projectId).push(table, row);
    }

    public void finishBatch(String projectId) {
        getMultipleBatchExecutor(projectId).finish();
    }
}
