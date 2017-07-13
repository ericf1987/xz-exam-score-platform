package com.xz.scorep.executor.project;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.xz.ajiaedu.common.mongo.DocumentUtils;
import com.xz.ajiaedu.common.mongo.MongoUtils;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.config.MongoConfig;
import com.xz.scorep.executor.mongo.MongoClientFactory;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 获取网阅数据库相关数据查询
 *
 * @author by fengye on 2017/5/23.
 */
@Service
public class ScannerDBService {
    @Autowired
    MongoConfig mongoConfig;

    @Autowired
    MongoClientFactory mongoClientFactory;

    @Autowired
    QuestService questService;

    /**
     * 查询单个学生的留痕数据
     *
     * @param databaseName  项目ID
     * @param studentId 学生ID
     * @param subjectId 科目ID
     * @return 返回结果
     */
    public Map<String, Object> getOneStudentCardSlice(String databaseName, String studentId, String subjectId) {
        MongoCollection<Document> studentCollection = getStudentCollection(databaseName, subjectId);
        Document query = MongoUtils.doc("studentId", studentId);
        Document first = studentCollection.find(query).first();
        return processOneStudent(databaseName, subjectId, first);
    }

    private Map<String, Object> processOneStudent(String databaseName, String subjectId, Document document) {
        Map<String, Object> map = new HashMap<>();
        if (null != document) {
            //考虑到网阅数据中每个小题的满分数据可能不准确，所以满分数据从Mysql的quest列表中获取
            List<Document> objectiveList = fixFullScore0(databaseName, subjectId, document.get("objectiveList", List.class));
            List<Document> subjectiveList = fixFullScore0(databaseName, subjectId, document.get("subjectiveList", List.class));

            List<Document> newObjectiveList = objectiveList.stream().filter(doc -> doc.getBoolean("isEffective")).collect(Collectors.toList());
            List<Document> newSubjectiveList = subjectiveList.stream().filter(doc -> doc.getBoolean("isEffective")).collect(Collectors.toList());

            map.put("studentId", DocumentUtils.getString(document, "studentId", ""));
            map.put("paper_positive", DocumentUtils.getString(document, "paper_positive", ""));
            map.put("paper_reverse", DocumentUtils.getString(document, "paper_reverse", ""));
            map.put("objectiveList", newObjectiveList);
            map.put("subjectiveList", newSubjectiveList);
            //只要主观题和客观题有一项目为空，则返回有数据坐标
        }
        return map;
    }

    private List<Document> fixFullScore0(String databaseName, String subjectId, List<Document> questList) {
        List<ExamQuest> examQuests = questService.queryQuests(databaseName);
        for (Document questDoc : questList) {
            double fullScore = examQuests.stream().filter(q -> q.getExamSubject().equals(subjectId) && q.getQuestNo().equals(questDoc.getString("questionNo")))
                    .mapToDouble(q -> q.getFullScore()).sum();
            questDoc.put("fullScore", fullScore);
        }
        return questList;
    }

    private MongoCollection<Document> getStudentCollection(String database, String subjectId) {
        String projectId = database.indexOf("_") == -1 ? database : database.substring(0, database.indexOf("_"));
        MongoClient scannerMongoClient = mongoClientFactory.getProjectMongoClient(projectId);
        return scannerMongoClient.getDatabase(getDbName(projectId, subjectId)).getCollection("students");
    }

    private String getDbName(String projectId, String subjectId) {
        return projectId + "_" + subjectId;
    }

}
