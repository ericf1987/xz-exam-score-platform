package com.xz.scorep.executor.fakedata;

import com.xz.ajiaedu.common.lang.MapBuilder;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.ProjectClass;
import com.xz.scorep.executor.bean.ProjectSchool;
import com.xz.scorep.executor.bean.ProjectStudent;
import com.xz.scorep.executor.db.DBIHandle;
import com.xz.scorep.executor.db.DbiHandleFactory;
import com.xz.scorep.executor.db.DbiHandleFactoryManager;
import com.xz.scorep.executor.db.MultipleBatchExecutor;
import com.xz.scorep.executor.project.*;
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

    private static final Logger LOG = LoggerFactory.getLogger(FakeDataGenerateService.class);

    private static final String SCORE_TABLE_COLUMNS = "(student_id varchar(36), quest_id varchar(36), score decimal(4,1))";

    @Autowired
    private DbiHandleFactoryManager dbiHandleFactoryManager;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private ClassService classService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private QuestService questService;

    public void generateFakeData(FakeDataParameter fakeDataParameter) {
        String projectId = fakeDataParameter.getProjectId();
        projectService.initProjectDatabase(projectId);

        DbiHandleFactory dbiHandleFactory = dbiHandleFactoryManager.getDefaultDbiHandleFactory();
        DBIHandle handle = dbiHandleFactory.getProjectDBIHandle(projectId);

        try {
            Thread studentThread = createStudents(fakeDataParameter);
            Thread questThread = createQuests(handle, fakeDataParameter);
            studentThread.join();
            questThread.join();

            createScores(handle);
        } catch (InterruptedException e) {
            LOG.error("Error generating fake data", e);
        }
    }

    //////////////////////////////////////////////////////////////

    private void createScores(DBIHandle dbiHandle) {
        Random random = new Random();
        AtomicInteger counter = new AtomicInteger();

        MultipleBatchExecutor batchExecutor = new MultipleBatchExecutor(dbiHandle);
        batchExecutor.setBatchSize(500);

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
                    double fullScore = parameter.getScorePerQuest();

                    ExamQuest quest = new ExamQuest(questId, subjectId, j < 10, questNo, fullScore);
                    questService.saveQuest(parameter.getProjectId(), quest);

                    // create score table
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

    private Thread createStudents(final FakeDataParameter parameter) {
        Runnable runnable = () -> {

            String projectId = parameter.getProjectId();

            for (int i = 0; i < parameter.getSchoolPerProject(); i++) {
                String schoolId = UuidUtils.uuid();
                String schoolName = "SCHOOL" + (i + 1);
                ProjectSchool school = new ProjectSchool(schoolId, schoolName, "430101", "430100", "430000");
                schoolService.saveSchool(projectId, school);

                for (int j = 0; j < parameter.getClassPerSchool(); j++) {
                    String classId = UuidUtils.uuid();
                    String className = schoolName + ":CLASS" + (j + 1);
                    ProjectClass projectClass = new ProjectClass(classId, className, schoolId);
                    classService.saveClass(projectId, projectClass);

                    for (int k = 0; k < parameter.getStudentPerClass(); k++) {
                        String studentId = UuidUtils.uuid();
                        String studentName = className + ":STU" + (k + 1);
                        studentService.saveStudent(projectId, new ProjectStudent(studentId, studentName, classId));
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
