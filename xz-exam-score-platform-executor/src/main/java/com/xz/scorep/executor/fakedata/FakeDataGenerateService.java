package com.xz.scorep.executor.fakedata;

import com.xz.ajiaedu.common.lang.MapBuilder;
import com.xz.scorep.executor.db.DBIHandle;
import com.xz.scorep.executor.db.DbiHandleFactory;
import com.xz.scorep.executor.db.DbiHandleFactoryManager;
import com.xz.scorep.executor.db.MultipleBatchExecutor;
import com.xz.scorep.executor.utils.UuidUtils;
import org.skife.jdbi.v2.util.StringColumnMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * (description)
 * created at 16/12/28
 *
 * @author yidin
 */
@Service
public class FakeDataGenerateService {

    static final Logger LOG = LoggerFactory.getLogger(FakeDataGenerateService.class);

    private static final String SCORE_TABLE_COLUMNS = "(student_id varchar(36), quest_id varchar(36), score decimal(4,1))";

    @Autowired
    DbiHandleFactoryManager dbiHandleFactoryManager;

    public void generateFakeData(FakeDataParameter fakeDataParameter) {
        String projectId = fakeDataParameter.getProjectId();

        DbiHandleFactory dbiHandleFactory = dbiHandleFactoryManager.getDefaultDbiHandleFactory();
        dbiHandleFactory.dropProjectDatabase(projectId);
        dbiHandleFactory.createProjectDatabase(projectId);

        DBIHandle handle = dbiHandleFactory.getProjectDBIHandle(projectId);
        createTables(handle);

        try {
            Thread studentThread = createStudents(handle, fakeDataParameter);
            Thread questThread = createQuests(handle, fakeDataParameter);
            studentThread.join();
            questThread.join();

            createScores(handle);
        } catch (InterruptedException e) {
            LOG.error("Error generating fake data", e);
        }
    }

    private void createTables(DBIHandle dbiHandle) {
        dbiHandle.runHandle(handle -> {
            handle.execute("create table school (id varchar(36))");
            handle.execute("create table class  (id varchar(36), school varchar(36))");
            handle.execute("create table student(id varchar(36), class  varchar(36))");
            handle.execute("create table subject(id varchar(9))");
            handle.execute("create table quest  (id varchar(36), questNo varchar(10), subject varchar(9), full_score decimal(4,1))");
        });
    }

    //////////////////////////////////////////////////////////////

    private void createScores(DBIHandle dbiHandle) {
        Random random = new Random();
        AtomicInteger counter = new AtomicInteger();

        MultipleBatchExecutor batchExecutor = new MultipleBatchExecutor(dbiHandle);
        batchExecutor.setBatchSize(50);

        dbiHandle.runHandle(tableListHandle -> {
            tableListHandle.createQuery("select id from quest").map(StringColumnMapper.INSTANCE).list()
                    .forEach(questId -> {
                        String table = "score_" + questId;
                        String sql = "insert into score_" + questId + "(student_id,quest_id,score)values" +
                                "(:student_id, :quest_id, :score)";
                        batchExecutor.prepareBatch(table, sql);
                    });
        });

        dbiHandle.runHandle(queryHandle -> dbiHandle.runHandle(insertHandle -> {

            for (Map<String, Object> map : queryHandle.createQuery(
                    "select s.id as student_id, q.id as quest_id from student s, quest q"
            )) {
                String studentId = (String) map.get("student_id");
                String questId = (String) map.get("quest_id");
                double score = random.nextInt(5) / 2.0;
                String table = "score_" + questId;

                Map<String, Object> row = MapBuilder.<String, Object>start("student_id", studentId)
                        .and("quest_id", questId).and("score", score).get();

                batchExecutor.push(table, row);

                if (counter.incrementAndGet() % 200 == 0) {
                    LOG.debug("%6d score inserted.\n", counter.get());
                }
            }
        }));

        batchExecutor.finish();
    }

    private Thread createQuests(final DBIHandle dbiHandle, final FakeDataParameter parameter) {
        Runnable runnable = () -> {

            for (int i = 0; i < parameter.getSubjectPerProject(); i++) {
                String subjectId = String.format("%03d", (i + 1));
                dbiHandle.runHandle(handle -> handle.insert("insert into subject(id)values(?)", subjectId));

                for (int j = 0; j < parameter.getQuestPerSubject(); j++) {
                    String questId = UuidUtils.uuid();
                    String questNo = String.valueOf(j + 1);
                    double fullScore = 2;

                    dbiHandle.runHandle(handle -> handle.insert(
                            "insert into quest(id,questNo,subject,full_score)values(?,?,?,?)",
                            questId, questNo, subjectId, fullScore));

                    dbiHandle.runHandle(handle -> {
                        String tableName = "score_" + questId;
                        handle.execute("create table " + tableName + SCORE_TABLE_COLUMNS);
                        handle.execute("create index idx_score_stu_" + tableName + " on " + tableName + "(student_id)");
                        handle.execute("create index idx_score_qst_" + tableName + " on " + tableName + "(quest_id)");
                    });
                }
            }

            LOG.info("------------------ Quests created.");
        };

        Thread thread = new Thread(runnable);
        thread.start();
        return thread;
    }

    private Thread createStudents(final DBIHandle dbiHandle, final FakeDataParameter parameter) {
        Runnable runnable = () -> {

            for (int i = 0; i < parameter.getSchoolPerProject(); i++) {
                String schoolId = UuidUtils.uuid();
                dbiHandle.runHandle(handle -> {
                    handle.insert("insert into school(id) values(?)", schoolId);
                });

                for (int j = 0; j < parameter.getClassPerSchool(); j++) {
                    String classId = UuidUtils.uuid();
                    dbiHandle.runHandle(handle -> {
                        handle.insert("insert into class(id,school) values(?,?)", classId, schoolId);
                    });

                    for (int k = 0; k < parameter.getStudentPerClass(); k++) {
                        String studentId = UuidUtils.uuid();
                        dbiHandle.runHandle(handle -> {
                            handle.insert("insert into student(id,class)values(?,?)", studentId, classId);
                        });
                    }
                }
            }

            LOG.info("------------------ Students created.");
        };

        Thread thread = new Thread(runnable);
        thread.start();
        return thread;
    }
}
