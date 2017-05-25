package com.xz.scorep.executor.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.xz.scorep.executor.config.MongoConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static com.xz.ajiaedu.common.mongo.MongoUtils.doc;

@Service
public class MongoClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(MongoClientFactory.class);

    @Autowired
    private MongoConfig mongoConfig;

    private List<MongoClient> scannerMongoClients = new ArrayList<>();

    private MongoClient aggrMongoClient;

    public void setAggrMongoClient(MongoClient aggrMongoClient) {
        this.aggrMongoClient = aggrMongoClient;
    }

    @PostConstruct
    private void initMongoClients() {
        //初始化网阅Mongo
        initScannerMongoClients();
        //初始化统计Mongo
        initAggrMongoClient();
    }

    /**
     * 初始化网阅Mongo数据库的连接
     */
    private void initScannerMongoClients() {
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
     * 初始化统计Mongo数据库的连接
     */
    private void initAggrMongoClient() {
        String dbString = mongoConfig.getAggrDbs();
        List<ServerAddress> serverAddresses = readAggrMongoServerAddress(dbString);
        MongoClientOptions options = MongoClientOptions.builder().build();//默认连接池大小为100
        setAggrMongoClient(new MongoClient(serverAddresses, options));
    }

    private List<ServerAddress> readAggrMongoServerAddress(String dbString) {
        String[] split = dbString.split(",");
        List<ServerAddress> seeds = new ArrayList<>();

        for (String s : split) {
            if (StringUtils.isBlank(s) || !s.contains(":")) {
                continue;
            }

            String[] host_port = s.split(":");
            seeds.add(new ServerAddress(host_port[0], Integer.parseInt(host_port[1])));
        }
        return seeds;
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

    public MongoClient getAggrMongoClient() {
        return aggrMongoClient;
    }

    private boolean projectExists(MongoClient client, String project) {
        return client
                .getDatabase("project_database")
                .getCollection("project")
                .count(doc("projectId", project)) > 0;
    }


}
