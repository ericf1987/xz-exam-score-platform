package com.xz.scorep.executor.importproject;

import com.hyd.dao.DAO;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.xz.ajiaedu.common.lang.Context;
import com.xz.ajiaedu.common.lang.Counter;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.ajiaedu.common.score.ScorePattern;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.db.MultipleBatchExecutor;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

import static com.xz.ajiaedu.common.mongo.MongoUtils.doc;
import static org.apache.commons.lang.StringUtils.defaultString;

/**
 * 从网阅数据库导入成绩记录
 *
 * @author yidin
 */
public class ImportScoreHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ImportScoreHelper.class);

    public static final String MGR_DB_NAME = "project_database";

    private Context context;

    private MongoClient mongoClient;

    private Map<String, ExamQuest> questMap = new HashMap<>();

    private MultipleBatchExecutor scoreBatchExecutor;

    private Counter counter = new Counter(5000,
            count -> LOG.info("已导入成绩 {} 条", count));

    public ImportScoreHelper(Context context, MongoClient mongoClient, DAO projectDao) {
        this.context = context;
        this.mongoClient = mongoClient;
        this.scoreBatchExecutor = new MultipleBatchExecutor(projectDao, 100);

        prepare();
    }

    private void prepare() {

        // 构建 questMap 供将来查询
        this.getQuestList().forEach(quest -> {
            String key = quest.getExamSubject() + ":" + quest.getQuestNo();
            this.questMap.put(key, quest);
        });
    }

    public void importScore() {

        Document projectDoc = findProject();
        Document subjectCodes = projectDoc.get("subjectcodes", Document.class);
        ArrayList<String> subjectIds = new ArrayList<>(subjectCodes.keySet());

        for (String subjectId : subjectIds) {
            importSubjectScore(subjectId);
        }

        scoreBatchExecutor.finish();
        counter.finish();
    }

    // 查询项目信息
    private Document findProject() {
        String projectId = getProjectId();
        return mongoClient.getDatabase(MGR_DB_NAME)
                .getCollection("project").find(doc("projectId", projectId)).first();
    }

    // 导入单科成绩
    private void importSubjectScore(String subjectId) {
        String projectId = getProjectId();
        String subjectDbName = projectId + "_" + subjectId;
        MongoDatabase subjectDb = mongoClient.getDatabase(subjectDbName);

        subjectDb.getCollection("students").find(doc())
                .forEach((Consumer<Document>) doc -> importStudentScore(doc, subjectId));
    }

    // 导入单个考生的单科成绩
    private void importStudentScore(
            Document studentScoreDoc, String subjectId) {

        boolean cheat = studentScoreDoc.getBoolean("isCheating", false);
        boolean absent = studentScoreDoc.getBoolean("isAbsent", false);

        importStudentSubjectiveScore(subjectId, cheat, absent, studentScoreDoc);
        importStudentObjectiveScore(subjectId, cheat, absent, studentScoreDoc);
    }

    // 导入单个考生的单科主观题成绩
    @SuppressWarnings("unchecked")
    private void importStudentSubjectiveScore(
            String subjectId, boolean cheat, boolean absent, Document studentScoreDoc) {

        String studentId = studentScoreDoc.getString("studentId");
        List<Document> subjectiveList = (List<Document>) studentScoreDoc.get("subjectiveList");

        for (Document scoreDoc : subjectiveList) {
            String questNo = scoreDoc.getString("questionNo");
            boolean effective = scoreDoc.getBoolean("isEffective", true);
            double score = Double.parseDouble(scoreDoc.get("score").toString());
            double fullScore = Double.parseDouble(scoreDoc.get("fullScore").toString());

            ExamQuest quest = getQuest(subjectId, questNo);
            score = calculateSbjScore(quest, fullScore, score, cheat, absent, effective);

            ScoreValue scoreValue = new ScoreValue(score, score == fullScore);
            saveScore(studentId, quest, scoreValue, false);
        }
    }

    private double calculateSbjScore(
            ExamQuest quest, double fullScore, double score, boolean cheat, boolean absent, boolean effective) {

        boolean giveFullScore = quest.isGiveFullScore();

        // 缺考、作弊和未选择，都得 0 分；
        // 否则如果强制给分，则得满分；
        // 否则得分为老师的给分。
        if (absent || cheat) {
            score = 0;
        } else if (!effective) {
            score = 0;
        } else if (giveFullScore) {
            score = fullScore;
        }
        return score;
    }

    // 导入单个考生的单科客观题成绩
    @SuppressWarnings("unchecked")
    private void importStudentObjectiveScore(
            String subjectId, boolean cheat, boolean absent, Document studentScoreDoc) {

        String projectId = getProjectId();
        String studentId = studentScoreDoc.getString("studentId");
        List<Document> objectiveList = (List<Document>) studentScoreDoc.get("objectiveList");

        for (Document scoreDoc : objectiveList) {
            String questNo = scoreDoc.getString("questionNo");
            String studentAnswer = readStudentAnswer(scoreDoc);
            ExamQuest quest = getQuest(subjectId, questNo);

            // 客观题必须要有作答，如未作答也应该是 “*”
            if (!absent && !cheat && StringUtil.isBlank(studentAnswer)) {
                saveScore(studentId, quest, new ScoreValue(0, false), true);

            } else {
                boolean giveFullScore = quest.isGiveFullScore();
                ScoreValue scoreValue = calculateObjScore(
                        quest, studentAnswer, giveFullScore, cheat, absent);

                saveScore(studentId, quest, scoreValue, false);
            }
        }

    }

    private void saveScore(String studentId, ExamQuest quest, ScoreValue scoreValue, boolean missing) {
        Map<String, Object> scoreMap = new HashMap<>();
        scoreMap.put("student_id", studentId);
        scoreMap.put("score", scoreValue.getScore());
        scoreMap.put("is_right", Boolean.toString(scoreValue.isRight()));
        scoreMap.put("missing", Boolean.toString(missing));

        String tableName = "score_" + quest.getId();
        counter.incre();
        scoreBatchExecutor.push(tableName, scoreMap);
    }

    /**
     * 计算客观题分数
     *
     * @param quest         客观题题目
     * @param studentAnswer 考生作答
     * @param giveFullScore 是否强制给分（缺考作弊除外）
     * @param cheat         是否作弊
     * @param absent        是否缺考
     *
     * @return 得分
     */
    private static ScoreValue calculateObjScore(
            ExamQuest quest, String studentAnswer, boolean giveFullScore, boolean cheat, boolean absent) {

        double fullScore = quest.getFullScore();

        // 缺考作弊不给分
        if (absent || cheat) {
            return new ScoreValue(0, false);
        }

        // 强制给分（缺考作弊除外，所以放在后面）
        if (giveFullScore) {
            return new ScoreValue(fullScore, true);
        }

        // 按照答案或给分规则打分
        String questAnswer = defaultString(quest.getScoreRule(), quest.getAnswer());
        ScorePattern scorePattern = new ScorePattern(questAnswer, fullScore);

        double score = scorePattern.getScore(studentAnswer);
        return new ScoreValue(score, score == fullScore);
    }

    private String getProjectId() {
        return this.context.getString("projectId");
    }

    private List<ExamQuest> getQuestList() {
        return this.context.get("questList");
    }

    private ExamQuest getQuest(String subjectId, String questNo) {
        return this.questMap.get(subjectId + ":" + questNo);
    }

    //////////////////////////////////////////////////////////////

    private static String readStudentAnswer(Document scoreDoc) {
        String s = defaultString(scoreDoc.getString("answerContent"), "*");
        char[] chars = s.toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }

    private static class ScoreValue {

        private double score;

        private boolean right;

        public ScoreValue() {
        }

        public ScoreValue(double score, boolean right) {
            this.score = score;
            this.right = right;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public boolean isRight() {
            return right;
        }

        public void setRight(boolean right) {
            this.right = right;
        }
    }
}
