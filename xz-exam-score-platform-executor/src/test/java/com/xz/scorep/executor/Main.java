package com.xz.scorep.executor;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Test;

import java.util.ArrayList;
import java.util.function.Consumer;

import static com.xz.ajiaedu.common.mongo.MongoUtils.doc;

/**
 * Author: luckylo
 * Date : 2017-04-12
 */
public class Main extends BaseTest {
    @Test
    public void test() {
        String projectId = "430300-c582131e66b64fe38da7d0510c399ec4";

        ServerAddress serverAddress = new ServerAddress("10.10.22.127", Integer.parseInt("30000"));
        MongoClient mongoClient = new MongoClient(serverAddress);
        assert (mongoClient != null);

        Document document = mongoClient.getDatabase("project_database")
                .getCollection("project")
                .find(doc("projectId", projectId))
                .first();
        assert (document != null);

        Document subjectCodes = document.get("subjectcodes", Document.class);
        ArrayList<String> subjectIds = new ArrayList<>(subjectCodes.keySet());
        assert (subjectIds.size() != 0);

        for (String subjectId : subjectIds) {
            String subjectDbName = projectId + "_" + subjectId;
            MongoDatabase subjectDb = mongoClient.getDatabase(subjectDbName);
            MongoCollection<Document> students = subjectDb.getCollection("students");
//            System.out.println(students.count());
            FindIterable<Document> findIterable = students.find(doc());
            assert (findIterable != null);
            findIterable.forEach((Consumer<Document>) doc -> {
                assert (doc == null);
            });
        }

    }
}
