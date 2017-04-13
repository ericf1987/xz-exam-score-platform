package com.xz.scorep.executor.debug;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.mongo.MongoClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DebugController {

    @Autowired
    private MongoClientFactory mongoClientFactory;

    @GetMapping("/scanner-students")
    @ResponseBody
    public Result getScannerStudentList(
            @RequestParam("project") String projectId
    ) {
        MongoClient mongoClient = mongoClientFactory.getProjectMongoClient(projectId);
        MongoDatabase database = mongoClient.getDatabase(projectId + "_001");
        long count = database.getCollection("students").count();

        return Result.success()
                .set("db", mongoClient.getServerAddressList())
                .set("database", database.getName())
                .set("students_count", count);
    }
}
