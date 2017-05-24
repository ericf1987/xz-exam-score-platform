package com.xz.scorep.executor.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.xz.scorep.executor.BaseTest;
import org.bson.Document;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

import static org.junit.Assert.assertNotNull;

/**
 * (description)
 * created at 2017/2/23
 *
 * @author yidin
 */
public class MongoClientFactoryTest extends BaseTest {

    @Autowired
    private MongoClientFactory mongoClientFactory;

    @Test
    public void getProjectMongoClient() throws Exception {
        String projectId = "430300-9cef9f2059ce4a36a40a7a60b07c7e00";
        MongoClient mongoClient = mongoClientFactory.getProjectMongoClient(projectId);

        assertNotNull(mongoClient);

        mongoClient.getDatabase(projectId).listCollectionNames()
                .forEach((Consumer<? super String>) System.out::println);
    }

    @Test
    public void testGetAggrMongoClient() throws Exception {
        MongoClient aggrMongoClient = mongoClientFactory.getAggrMongoClient();
        MongoDatabase project_data = aggrMongoClient.getDatabase("project_data");
        MongoCollection<Document> project_config = project_data.getCollection("project_config");
        Document first = project_config.find().first();
        System.out.println(first);
    }
}