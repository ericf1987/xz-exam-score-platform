package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.xz.ajiaedu.common.lang.MapBuilder;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.db.MultipleBatchExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScoreService {

    private static final String SCORE_TABLE_COLUMNS = "(" +
            "  student_id varchar(36) primary key, " +
            "  score decimal(4,1) not null, " +
            "  objective_answer varchar(5), " +
            "  is_right varchar(5) not null, " +
            "  missing varchar(5) not null default 'false'" +
            ")";

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

        dao.execute("create table if not exists `" +
                tableName + "`" + SCORE_TABLE_COLUMNS + " comment '" + comment + "'");
    }

    private MultipleBatchExecutor getMultipleBatchExecutor(String projectId) {
        MultipleBatchExecutor batchExecutor = batchExecutorMap.get(projectId);
        if (batchExecutor == null) {
            throw new IllegalStateException("BatchExecutor for project '" + projectId + "' not exists.");
        }
        return batchExecutor;
    }

    public void prepareBatch(String projectId) {
        MultipleBatchExecutor executor = new MultipleBatchExecutor(daoFactory.getProjectDao(projectId), 1000);
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

    public void clearScores(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        List<String> scoreTableNames = projectDao.query("show tables").stream()
                .map(map -> (String) map.values().stream().findFirst().orElse(null))
                .filter(tableName -> tableName != null && tableName.startsWith("score_"))
                .collect(Collectors.toList());

        scoreTableNames.forEach(
                tableName -> projectDao.execute("truncate table `" + tableName + "`"));
    }
}
