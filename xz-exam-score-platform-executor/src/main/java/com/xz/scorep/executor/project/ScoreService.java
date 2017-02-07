package com.xz.scorep.executor.project;

import com.xz.ajiaedu.common.lang.MapBuilder;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.db.DBIHandle;
import com.xz.scorep.executor.db.DbiHandleFactory;
import com.xz.scorep.executor.db.MultipleBatchExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScoreService {

    private static final String SCORE_TABLE_COLUMNS = "(student_id varchar(36), score decimal(4,1))";

    @Autowired
    private DbiHandleFactory dbiHandleFactory;

    private Map<String, MultipleBatchExecutor> batchExecutorMap = new HashMap<>();

    private String getTableName(String questId) {
        return "score_" + questId;
    }

    public void createScoreTable(String projectId, ExamQuest examQuest) {
        dbiHandleFactory.getProjectDBIHandle(projectId).runHandle(handle -> {
            String questId = examQuest.getId();
            String tableName = getTableName(questId);
            String comment = examQuest.getExamSubject() + ":" + examQuest.getQuestNo();
            handle.execute("create table " + tableName + SCORE_TABLE_COLUMNS + " comment '" + comment + "'");
            handle.execute("create index idx_score_stu_" + tableName + " on " + tableName + "(student_id)");
        });
    }

    private MultipleBatchExecutor getMultipleBatchExecutor(String projectId) {
        MultipleBatchExecutor batchExecutor = batchExecutorMap.get(projectId);
        if (batchExecutor == null) {
            throw new IllegalStateException("BatchExecutor for project '" + projectId + "' not exists.");
        }
        return batchExecutor;
    }

    public void prepareBatch(String projectId, List<String> questIds) {
        DBIHandle dbiHandle = dbiHandleFactory.getProjectDBIHandle(projectId);
        MultipleBatchExecutor executor = new MultipleBatchExecutor(dbiHandle, 500);

        for (String questId : questIds) {
            String table = "score_" + questId;
            String sql = "insert into score_" + questId + "(student_id,score)values" +
                    "(:student_id, :score)";
            executor.prepareBatch(table, sql);
        }

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
