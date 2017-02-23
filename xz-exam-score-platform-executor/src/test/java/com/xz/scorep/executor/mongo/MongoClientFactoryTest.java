package com.xz.scorep.executor.mongo;

import com.mongodb.MongoClient;
import com.xz.scorep.executor.BaseTest;
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

}