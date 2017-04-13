package com.xz.scorep.executor.mongo;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.xz.scorep.executor.config.MongoConfig;
import com.xz.scorep.executor.importproject.ImportProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.xz.ajiaedu.common.mongo.MongoUtils.doc;

@Service
public class MongoClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(MongoClientFactory.class);

    @Autowired
    private MongoConfig mongoConfig;

    private List<MongoClient> scannerMongoClients = new ArrayList<>();

    @PostConstruct
    private void initMongoClients() {
        String dbString = mongoConfig.getScannerDbs();
        String[] clusters = dbString.split(";");

        for (String cluster : clusters) {
            String[] servers = cluster.split(",");
            List<ServerAddress> addresses = new ArrayList<>();

            for (String server : servers) {
                String[] serverAndPort = server.split(":");
                addresses.add(new ServerAddress(serverAndPort[0], Integer.parseInt(serverAndPort[1])));
            }
            scannerMongoClients.add(new MongoClient(addresses));
        }
    }

    /**
     * 根据项目 ID 查询其对应的 MongoClient
     *
     * @param projectId 项目ID
     * @return 包含该项目的 MongoClient
     */
    public MongoClient getProjectMongoClient(String projectId) {

        MongoClient mongoClient = this.scannerMongoClients.stream()
                .filter(client -> projectExists(client, projectId))
                .findFirst()
                .orElse(null);

        LOG.info("项目 {} 所属网阅数据库：{}", projectId, mongoClient.getAddress());

        return mongoClient;
    }

    private boolean projectExists(MongoClient client, String project) {
        return client
                .getDatabase("project_database")
                .getCollection("project")
                .count(doc("projectId", project)) > 0;
    }


    public List<MongoClient> getScannerMongoClients(String projectId) {
        return this.scannerMongoClients.stream()
                .filter(client -> projectExists(client, projectId))
                .collect(Collectors.toList());
    }

}
