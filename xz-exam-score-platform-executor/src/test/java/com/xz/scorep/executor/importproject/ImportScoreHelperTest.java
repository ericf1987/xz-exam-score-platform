package com.xz.scorep.executor.importproject;

import com.hyd.dao.DAO;
import com.mongodb.MongoClient;
import com.xz.ajiaedu.common.lang.Context;
import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.mongo.MongoClientFactory;
import com.xz.scorep.executor.project.AbsentService;
import com.xz.scorep.executor.project.CheatService;
import com.xz.scorep.executor.project.QuestService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yiding_he
 */
public class ImportScoreHelperTest extends BaseTest {

    @Autowired
    private MongoClientFactory mongoClientFactory;

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private QuestService questService;

    @Autowired
    private AbsentService absentService;

    @Autowired
    private CheatService cheatService;

    @Test
    public void importScore() throws Exception {
        String projectId = "430500-d9497f79c88540518a5a272a97af750c";

        Context context = new Context();
        context.put("projectId", projectId);
        context.put("questList", questService.queryQuests(projectId));

        MongoClient mongoClient = mongoClientFactory.getProjectMongoClient(projectId);
        DAO dao = daoFactory.getProjectDao(projectId);

        ImportScoreHelper helper = new ImportScoreHelper(context, mongoClient, dao);
        helper.setAbsentService(absentService);
        helper.setCheatService(cheatService);

        helper.importScore();
    }

}