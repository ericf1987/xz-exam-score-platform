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
import com.xz.scorep.executor.project.AbsentService;
import com.xz.scorep.executor.project.CheatService;
import com.xz.scorep.executor.project.LostService;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

import static com.xz.ajiaedu.common.mongo.MongoUtils.doc;
import static org.apache.commons.lang.StringUtils.defaultString;

/**
 * 从网阅数据库导入成绩记录
 * <p>
 * 关于缺考和作弊的处理：
 * <p>
 * 缺考：缺考考生的成绩会在合并科目分数之后进行删除，详见 {@link com.xz.scorep.executor.aggregate.impl.StudentSubjectScoreAggregator}
 * 作弊：作弊考生的成绩在这里不会导入，于是在进行统计时，该考生所有科目都为零分
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

    private AbsentService absentService;

    private CheatService cheatService;

    private LostService lostService;

    private ReportConfig reportConfig;

    private Counter counter = new Counter(5000,
            count -> LOG.info("已导入成绩 {} 条", count));

    public ImportScoreHelper(Context context, MongoClient mongoClient, DAO projectDao) {
        this.context = context;
        this.mongoClient = mongoClient;
        this.scoreBatchExecutor = new MultipleBatchExecutor(projectDao, 100);

        prepare();
    }

    public void setAbsentService(AbsentService absentService) {
        this.absentService = absentService;
    }

    public void setCheatService(CheatService cheatService) {
        this.cheatService = cheatService;
    }

    public void setLostService(LostService lostService) {
        this.lostService = lostService;
    }

    public void setReportConfig(ReportConfig reportConfig) {
        this.reportConfig = reportConfig;
    }

    private void prepare() {

        // 构建 questMap 供将来查询
        this.getQuestList().forEach(quest -> {
            String key = quest.getExamSubject() + ":" + quest.getQuestNo();
            this.questMap.put(key, quest);
        });
    }

    public void importScore() {

        if (absentService == null || cheatService == null || lostService == null) {
            throw new IllegalStateException("请先设置 absentService 、cheatService 、lostService");
        }

        Document projectDoc = findProject();
        Document subjectCodes = projectDoc.get("subjectcodes", Document.class);
        ArrayList<String> subjectIds = new ArrayList<>(subjectCodes.keySet());

        //重新导数据之前清空cheat absent lost 表
        String projectId = getProjectId();
        clearCALData(projectId);

        for (String subjectId : subjectIds) {
            importSubjectScore(subjectId);
        }

        scoreBatchExecutor.finish();
        counter.finish();

        if (counter.getValue() == 0) {
            throw new IllegalStateException("网阅库没有项目 " + projectId + " 的分数记录。");
        }
    }

    private void clearCALData(String projectId) {
        cheatService.clearCheat(projectId);
        absentService.clearAbsent(projectId);
        lostService.clearLost(projectId);
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
                .forEach((Consumer<Document>) doc -> importStudentScore(projectId, doc, subjectId));
    }

    // 导入单个考生的单科成绩
    private void importStudentScore(
            String projectId, Document studentScoreDoc, String subjectId) {

        String studentId = studentScoreDoc.getString("studentId");
        boolean cheat = studentScoreDoc.getBoolean("isCheating", false);
        boolean absent = studentScoreDoc.getBoolean("isAbsent", false);
        boolean lost = studentScoreDoc.getBoolean("isLost", false);


        if (cheat) {
            //添加到作弊表
            cheatService.saveCheat(projectId, studentId, subjectId);
        }

        if (absent) {
            //添加到缺考表
            absentService.saveAbsent(projectId, studentId, subjectId);
        }

        if (lost) {
            //添加到缺卷表
            lostService.saveLost(projectId, studentId, subjectId);
        }

        // 同一学生不可能同时有存在两种及以上的状态
        //缺考、作弊、缺卷先处理为0分
        if (cheat) {
            importStudentSubjectiveScore(subjectId, studentScoreDoc, true, false);
            importStudentObjectiveScore(subjectId, studentScoreDoc, true, false);
            return;
        }

        if (absent || lost) {
            importStudentSubjectiveScore(subjectId, studentScoreDoc, false, true);
            importStudentObjectiveScore(subjectId, studentScoreDoc, false, true);
            return;
        }

        //无缺考、缺卷、作弊学生
        importStudentSubjectiveScore(subjectId, studentScoreDoc, false, false);
        importStudentObjectiveScore(subjectId, studentScoreDoc, false, false);

    }

    // 导入单个考生的单科主观题成绩
    @SuppressWarnings("unchecked")
    private void importStudentSubjectiveScore(
            String subjectId, Document studentScoreDoc, boolean cheat, boolean absent) {

        String studentId = studentScoreDoc.getString("studentId");
        List<Document> subjectiveList = (List<Document>) studentScoreDoc.get("subjectiveList");

        if (subjectiveList == null) {
            return;
        }

        for (Document scoreDoc : subjectiveList) {
            String questNo = scoreDoc.getString("questionNo");
            boolean effective = scoreDoc.getBoolean("isEffective", true);
            double score = Double.parseDouble(scoreDoc.get("score").toString());
            double fullScore = Double.parseDouble(scoreDoc.get("fullScore").toString());

            ExamQuest quest = getQuest(subjectId, questNo);
            score = calculateSbjScore(quest, fullScore, score, effective);
            ScoreValue scoreValue;
            if (cheat || absent) {    //作弊、缺考、缺卷强行置为0分
                scoreValue = new ScoreValue(0, false);
            } else {
                scoreValue = new ScoreValue(score, score == fullScore);
            }

            saveScore(studentId, quest, scoreValue, false);
        }
    }

    private double calculateSbjScore(
            ExamQuest quest, double fullScore, double score, boolean effective) {

        boolean giveFullScore = quest.isGiveFullScore();

        // 未选择该题得 0 分；
        // 否则如果强制给分，则得满分；
        // 否则得分为老师的给分。
        if (!effective) {
            score = 0;
        } else if (giveFullScore) {
            score = fullScore;
        }
        return score;
    }

    // 导入单个考生的单科客观题成绩
    @SuppressWarnings("unchecked")
    private void importStudentObjectiveScore(
            String subjectId, Document studentScoreDoc, boolean cheat, boolean absent) {

        String studentId = studentScoreDoc.getString("studentId");
        List<Document> objectiveList = (List<Document>) studentScoreDoc.get("objectiveList");

        if (objectiveList == null) {
            return;
        }

        for (Document scoreDoc : objectiveList) {
            String questNo = scoreDoc.getString("questionNo");
            ExamQuest quest = getQuest(subjectId, questNo);

            String studentAnswer = readStudentAnswer(quest, scoreDoc);

            if (cheat || absent) {//作弊强、缺考、缺卷强行置为0分
                saveScore(studentId, quest, new ScoreValue(0, false), true);
                continue;
            } else {
                boolean giveFullScore = quest.isGiveFullScore();
                // 客观题必须要有作答，如未作答也应该是 “*”
                if (!giveFullScore && StringUtil.isBlank(studentAnswer)) {
                    saveScore(studentId, quest, new ScoreValue(0, false), true);
                    continue;
                } else {
                    ScoreValue scoreValue = calculateObjScore(quest, studentAnswer, giveFullScore);
                    saveScore(studentId, quest, scoreValue, false);
                }
            }
        }

    }

    private void saveScore(String studentId, ExamQuest quest, ScoreValue scoreValue, boolean missing) {
        Map<String, Object> scoreMap = new HashMap<>();
        scoreMap.put("student_id", studentId);
        scoreMap.put("score", scoreValue.getScore());
        scoreMap.put("is_right", Boolean.toString(scoreValue.isRight()));
        scoreMap.put("missing", Boolean.toString(missing));

        if (quest.isObjective()) {
            scoreMap.put("objective_answer", scoreValue.getAnswer());
        }

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
     * @return 得分
     */
    private static ScoreValue calculateObjScore(
            ExamQuest quest, String studentAnswer, boolean giveFullScore) {

        double fullScore = quest.getFullScore();

        // 强制给分
        if (giveFullScore) {
            return new ScoreValue(fullScore, true);
        }

        // 按照答案或给分规则打分
        String questAnswer = quest.effectiveScoreRule().toUpperCase();
        ScorePattern scorePattern = new ScorePattern(questAnswer, fullScore);

        double score = scorePattern.getScore(studentAnswer.toUpperCase());
        return new ScoreValue(score, studentAnswer, score == fullScore);
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

    private static String readStudentAnswer(ExamQuest quest, Document scoreDoc) {
        String s = defaultString(scoreDoc.getString("answerContent"), "*").toUpperCase();

        // 单选题出现多个选择时，一律置为 "*"
        // 旧版答题卡可能没有 multiChoice 属性，此时根据答案的长度来判断
        if (!quest.isMultiChoice() && quest.getAnswer().length() == 1 && s.length() > 1) {
            s = "*";
        }

        //判断答案  是否在选项列表中......
        String options = quest.getOptions().toUpperCase();
        if (!StringUtil.isBlank(s)) {
            char[] chars = s.toCharArray();
            for (Character ch : chars) {
                if (!options.contains(String.valueOf(ch))) {
                    s = "*";
                    break;
                }
            }
        }

        char[] chars = s.toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }

    private static class ScoreValue {

        private double score;

        private String answer;      // 客观题专用

        private boolean right;

        public ScoreValue() {
        }

        public ScoreValue(double score, boolean right) {
            this.score = score;
            this.right = right;
        }

        public ScoreValue(double score, String answer, boolean right) {
            this.score = score;
            this.answer = answer;
            this.right = right;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
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
