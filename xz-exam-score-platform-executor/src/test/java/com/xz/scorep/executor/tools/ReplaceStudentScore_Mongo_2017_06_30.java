package com.xz.scorep.executor.tools;

import com.alibaba.druid.pool.DruidDataSource;
import com.hyd.dao.DataSources;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.xz.ajiaedu.common.io.FileUtils;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.config.MongoConfig;
import com.xz.scorep.executor.mongo.MongoClientFactory;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * 由于学生试卷错位，导致一些试卷背面与正面不是同一个学生的，现在需要将分数对应的学生纠正。
 *
 * @author yidin
 */
@SuppressWarnings("unchecked")
public class ReplaceStudentScore_Mongo_2017_06_30 {

    private static final Logger LOG = LoggerFactory.getLogger(ReplaceStudentScore_Mongo_2017_06_30.class);

    public static final String PROJECT_ID = "430100-85ebb706b7fd4554a8085bd462aba646";

    public static final String SUBJECT_ID = "002";

    public static final List<String> QUEST_IDS = Arrays.asList("100010237-20", "100010237-21", "100010237-22");

    public static final List<String> QUEST_NOS = Arrays.asList("20", "21", "22");

    public static final String FILES_FOLDER = "U:\\screenshots\\2222";

    static {
        System.setProperty("socksProxyHost", "127.0.0.1");
        System.setProperty("socksProxyPort", "2346");
    }

    public static void main(String[] args) {
        MongoClientFactory mongoClientFactory = new MongoClientFactory();
        MongoConfig mongoConfig = new MongoConfig();
        mongoConfig.setScannerDbs("10.10.22.127:30000;10.10.22.101:30000,10.10.22.102:30000,10.10.22.103:30000");
        mongoClientFactory.setMongoConfig(mongoConfig);
        mongoClientFactory.initMongoClients();

        MongoClient mongoClient = mongoClientFactory.getProjectMongoClient(PROJECT_ID);
        if (mongoClient == null) {
            throw new IllegalStateException("找不到网阅数据库");
        } else {
            LOG.info("项目 {} 所属网阅数据库：{}", PROJECT_ID, mongoClient.getAddress());
        }

        Map<String, String> examNoMap = readExamNoMap(mongoClient);           // examNo -> studentId
        Map<String, String> corrections = readCorrectionMap(examNoMap);       // fakeStudentId -> realStudentId
        List<String> needFixStudentIds = new ArrayList<>(corrections.keySet());
        LOG.info(corrections.size() + " corrections");

        Map<List<String>, Double> scores = readScores(mongoClient);
        Map<List<String>, Double> fixedScores = fixScores(scores, corrections);

        saveFixedScores(mongoClient, fixedScores, needFixStudentIds);

        LOG.info("-- All finished.");
    }

    private static void saveFixedScores(MongoClient mongoClient, Map<List<String>, Double> fixedScores, List<String> needFixStudentIds) {
        MongoCollection<Document> c = mongoClient
                .getDatabase(PROJECT_ID + "_" + SUBJECT_ID)
                .getCollection("students");

        Consumer<? super Document> documentConsumer = doc -> {
            String studentId = doc.getString("studentId");
            if (!needFixStudentIds.contains(studentId)) {
                LOG.info("Skip student " + studentId);
                return;
            }

            List<Document> subjectiveList = (List<Document>) doc.get("subjectiveList");

            for (Document subjective : subjectiveList) {
                String questNo = subjective.getString("questionNo");
                if (QUEST_NOS.contains(questNo)) {
                    Double realScore = fixedScores.get(Arrays.asList(studentId, questNo));
                    subjective.put("score", realScore == null? 0: realScore);
                }
            }

            LOG.info("Updating student " + studentId);
            c.deleteOne(new Document("studentId", studentId));
            c.insertOne(doc);
        };

        c.find().forEach(documentConsumer);
    }

    private static Map<List<String>, Double> fixScores(
            Map<List<String>, Double> scores, Map<String, String> corrections) {

        Map<List<String>, Double> result = new HashMap<>();

        scores.forEach((key, value) -> {
            String fakeStudentId = key.get(0);

            if (corrections.containsKey(fakeStudentId)) {
                String realStudentId = corrections.get(fakeStudentId);
                result.put(Arrays.asList(realStudentId, key.get(1)), value);
            }
        });

        return result;
    }

    private static Map<List<String>, Double> readScores(MongoClient mongoClient) {
        Map<List<String>, Double> result = new HashMap<>();

        Consumer<? super Document> documentConsumer = doc -> {
            String studentId = doc.getString("studentId");
            List<Document> subjectiveList = (List<Document>) doc.get("subjectiveList");

            for (Document subjective : subjectiveList) {
                String questNo = subjective.getString("questionNo");
                if (QUEST_NOS.contains(questNo)) {
                    List<String> key = Arrays.asList(studentId, questNo);
                    double score = subjective.getDouble("score");
                    result.put(key, score);
                }
            }
        };

        MongoCollection<Document> c = mongoClient
                .getDatabase(PROJECT_ID + "_" + SUBJECT_ID)
                .getCollection("students");

        c.find().forEach(documentConsumer);

        return result;
    }

    private static Map<String, String> readExamNoMap(MongoClient mongoClient) {
        Map<String, String> result = new HashMap<>();
        MongoCollection<Document> stuCollection = mongoClient.getDatabase(PROJECT_ID).getCollection("studentForProject");

        stuCollection.find().forEach((Consumer<Document>) document -> {
            result.put(document.getString("schoolStudentNo"), document.getString("studentId"));
        });

        return result;
    }

    private static Map<String, String> readCorrectionMap(Map<String, String> examNoMap) {
        Map<String, String> correctionMap = new HashMap<>();
        File dir = new File(FILES_FOLDER);

        Optional.ofNullable(dir.listFiles()).ifPresent(files -> {
            LOG.info(files.length + " files found.");
            for (File file : files) {
                try {
                    LOG.info("Reading file " + file.getName() + "...");
                    FileUtils.readLines(file).forEach(line -> parseLine(correctionMap, line, examNoMap));
                } catch (IOException e) {
                    LOG.error("", e);
                }
            }
        });

        return correctionMap;
    }

    private static void parseLine(Map<String, String> map, String line, Map<String, String> examNoMap) {
        line = line.trim();
        String[] split = line.split("\\s+");

        if (split.length == 2) {
            map.put(examNoMap.get(split[0]), examNoMap.get(split[1]));
        }
    }

    //////////////////////////////////////////////////////////////

    private static List<Correction> buildCorrectionSequence(Map<String, String> correctionMap) {
        List<Correction> correctionSequence = new ArrayList<>();
        correctionMap.forEach((fake, real) -> correctionSequence.add(new Correction(fake, real)));
        return correctionSequence;
    }

    private static DataSources initDataSources() {
        String username = StringUtil.substring(PROJECT_ID, 15);

        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://10.10.22.154:3306/" + PROJECT_ID + "?useSSL=false&useUnicode=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false");
        dataSource.setUsername(username);
        dataSource.setPassword(username);

        DataSources dataSources = new DataSources();
        dataSources.setDataSource("default", dataSource);
        return dataSources;
    }

    private static class Correction {

        private String fakeStudent;

        private String realStudent;

        public Correction(String fakeStudent, String realStudent) {
            this.fakeStudent = fakeStudent;
            this.realStudent = realStudent;
        }

        public String getFakeStudent() {
            return fakeStudent;
        }

        public void setFakeStudent(String fakeStudent) {
            this.fakeStudent = fakeStudent;
        }

        public String getRealStudent() {
            return realStudent;
        }

        public void setRealStudent(String realStudent) {
            this.realStudent = realStudent;
        }

        @Override
        public String toString() {
            return "Correction{" +
                    "fakeStudent='" + fakeStudent + '\'' +
                    ", realStudent='" + realStudent + '\'' +
                    '}';
        }
    }
}
