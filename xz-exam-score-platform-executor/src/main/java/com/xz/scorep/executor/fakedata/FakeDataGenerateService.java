package com.xz.scorep.executor.fakedata;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.dao.database.RowIterator;
import com.xz.scorep.executor.bean.*;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.*;
import com.xz.scorep.executor.utils.ChineseName;
import com.xz.scorep.executor.utils.UuidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
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

    private static final String[] AREAS = {"430101", "430102", "430103"};

    public static final String CITY = "430100";

    public static final String PROVINCE = "430000";

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private ClassService classService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private QuestService questService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private DAOFactory daoFactory;

    public void generateFakeDataAsync(FakeDataParameter fakeDataParameter) {
        Thread generateThread = new Thread(() -> generateFakeData(fakeDataParameter));
        generateThread.setDaemon(true);
        generateThread.start();
    }

    public void generateFakeData(FakeDataParameter fakeDataParameter) {
        String projectId = fakeDataParameter.getProjectId();
        projectService.saveProject(new ExamProject(
                projectId, projectId, "", new Date(), 11, fakeDataParameter.getProjectFullScore()
        ));
        projectService.initProjectDatabase(projectId);

        try {
            Thread studentThread = createStudents(fakeDataParameter);
            Thread questThread = createQuests(fakeDataParameter);
            studentThread.join();
            questThread.join();

            createScores(fakeDataParameter, daoFactory.getProjectDao(projectId));
            LOG.info("Fake project generation completed.");
        } catch (InterruptedException e) {
            LOG.error("Error generating fake data", e);
        }
    }

    //////////////////////////////////////////////////////////////

    private void createScores(FakeDataParameter fakeDataParameter, DAO projectDao) {

        String projectId = fakeDataParameter.getProjectId();
        Random random = new Random();
        AtomicInteger counter = new AtomicInteger();

        scoreService.prepareBatch(projectId);

        String queryScoreItemTemplate = "select s.id as student_id, q.id as quest_id from student s, quest q";

        try (RowIterator rowIterator = projectDao.queryIterator(queryScoreItemTemplate)) {
            while (rowIterator.next()) {
                Row row = rowIterator.getRow();
                String studentId = row.getString("student_id");
                String questId = row.getString("quest_id");
                double score = random.nextInt((int)fakeDataParameter.getScorePerQuest() * 2 + 1) / 2.0;
                scoreService.saveScoreBatch(projectId, questId, studentId, score);

                if (counter.incrementAndGet() % 2000 == 0) {
                    LOG.info(String.format("%8d score inserted.", counter.get()));
                }
            }
        }

        scoreService.finishBatch(projectId);
    }

    private Thread createQuests(final FakeDataParameter parameter) {
        Runnable runnable = () -> {

            LOG.info("Generating subjects and quests...");
            String projectId = parameter.getProjectId();

            for (int i = 0; i < parameter.getSubjectPerProject(); i++) {
                String subjectId = String.format("%03d", (i + 1));
                subjectService.saveSubject(projectId, subjectId);
                subjectService.createSubjectScoreTable(projectId, subjectId);

                for (int j = 0; j < parameter.getQuestPerSubject(); j++) {
                    String questId = UuidUtils.uuid();
                    String questNo = String.valueOf(j + 1);
                    double fullScore = parameter.getScorePerQuest();

                    ExamQuest quest = new ExamQuest(questId, subjectId, j < 10, questNo, fullScore);
                    questService.saveQuest(projectId, quest);
                    scoreService.createQuestScoreTable(projectId, quest);
                }
            }

            LOG.info("Quests generated.");
        };

        Thread thread = new Thread(runnable);
        thread.start();
        return thread;
    }

    private Thread createStudents(final FakeDataParameter parameter) {


        Runnable runnable = () -> {

            String projectId = parameter.getProjectId();
            int studentCount = 0;

            for (int i = 0; i < parameter.getSchoolPerProject(); i++) {
                String schoolId = UuidUtils.uuid();
                String schoolName = "SCHOOL" + (i + 1);
                String area = AREAS[i % AREAS.length];

                ProjectSchool school = new ProjectSchool(
                        schoolId, schoolName, area, CITY, PROVINCE);

                schoolService.saveSchool(projectId, school);

                for (int j = 0; j < parameter.getClassPerSchool(); j++) {
                    String classId = UuidUtils.uuid();
                    String className = schoolName + ":CLASS" + (j + 1);

                    ProjectClass projectClass = new ProjectClass(
                            classId, className, schoolId, area, CITY, PROVINCE);

                    classService.saveClass(projectId, projectClass);

                    for (int k = 0; k < parameter.getStudentPerClass(); k++) {
                        String studentId = UuidUtils.uuid();
                        String studentName = ChineseName.nextRandomName();
                        String examNo = String.format("%06d", studentCount);

                        ProjectStudent student = new ProjectStudent(
                                studentId, examNo, examNo,
                                studentName, classId, schoolId, area, CITY, PROVINCE);

                        studentService.getMultiSaver(projectId).push("student", student);
                        studentCount++;
                    }
                }
            }

            studentService.getMultiSaver(projectId).finish();
            LOG.info(studentCount + " Students created.");
        };

        Thread thread = new Thread(runnable);
        thread.start();
        return thread;
    }
}
