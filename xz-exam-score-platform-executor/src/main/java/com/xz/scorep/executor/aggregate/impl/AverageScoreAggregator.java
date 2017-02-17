package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.xz.scorep.executor.aggregate.AggragateOrder;
import com.xz.scorep.executor.aggregate.Aggregator;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.utils.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@AggragateOrder(2)
public class AverageScoreAggregator extends Aggregator {

    public static final String PROVINCE_QUEST_AVG_TEMPLATE = "insert into " +
            "  average_quest(range_type,range_id,quest_id,score)\n" +
            "select 'province', '{{province}}', '{{quest}}', avg(score.score)\n" +
            "  from score_{{quest}} score";

    public static final String SCHOOL_QUEST_AVG_TEMPLATE = "insert into " +
            "  average_quest(range_type,range_id,quest_id,score)\n" +
            "select 'school', school.id, '{{quest}}', avg(score.score) \n" +
            "  from score_{{quest}} score, student, class, school\n" +
            "  where score.student_id=student.id and student.class_id=class.id and class.school_id=school.id\n" +
            "  group by school.id";

    public static final String CLASS_QUEST_AVG_TEMPLATE = "insert into " +
            "  average_quest(range_type,range_id,quest_id,score)\n" +
            "select 'class', class.id, '{{quest}}' as quest_id, avg(score.score) \n" +
            "  from score_{{quest}} score, student, class\n" +
            "  where score.student_id=student.id and student.class_id=class.id\n" +
            "  group by class.id";

    //////////////////////////////////////////////////////////////

    public static final String PROVINCE_SUBJECT_AVG_TEMPLATE = "insert into " +
            "  average_subject(range_type,range_id,subject_id,score)\n" +
            "select 'province','{{province}}','{{subject}}',AVG(score) from score_subject_{{subject}}";

    public static final String SCHOOL_SUBJECT_AVG_TEMPLATE = "insert into " +
            "  average_subject(range_type,range_id,subject_id,score)\n" +
            "select 'school', school.id as range_id, '{{subject}}', avg(score.score) as score\n" +
            "  from score_subject_{{subject}} score, student, class, school\n" +
            "  where score.student_id=student.id and student.class_id=class.id and class.school_id=school.id\n" +
            "  group by school.id";

    public static final String CLASS_SUBJECT_AVG_TEMPLATE = "insert into " +
            "  average_subject(range_type,range_id,subject_id,score)\n" +
            "select 'class', class.id, '{{subject}}', avg(score.score)\n" +
            "  from score_subject_{{subject}} score, student, class\n" +
            "  where score.student_id=student.id and student.class_id=class.id\n" +
            "  group by class.id ";

    //////////////////////////////////////////////////////////////

    public static final String PROVINCE_TOTAL_AVG_TEMPLATE = "insert into " +
            "  average_project(range_type,range_id,score) " +
            "select 'province','{{province}}',AVG(score) from score_project";

    public static final String SCHOOL_TOTAL_AVG_TEMPLATE = "insert into " +
            "  average_project(range_type,range_id,score)\n" +
            "select 'school', school.id as range_id, avg(score.score) as score\n" +
            "  from score_project score, student, class, school\n" +
            "  where score.student_id=student.id and student.class_id=class.id and class.school_id=school.id\n" +
            "  group by school.id";

    public static final String CLASS_TOTAL_AVG_TEMPLATE = "insert into " +
            "  average_project(range_type,range_id,score)\n" +
            "select 'class', class.id, avg(score.score)\n" +
            "  from score_project score, student, class\n" +
            "  where score.student_id=student.id and student.class_id=class.id\n" +
            "  group by class.id";

    //////////////////////////////////////////////////////////////

    private static final Logger LOG = LoggerFactory.getLogger(AverageScoreAggregator.class);

    public static final int CONCURRENCY = 10;

    public static final int QUEUE_SIZE = 500;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private QuestService questService;

    @Override
    public void aggregate(String projectId) throws Exception {

        DAO projectDao = daoFactory.getProjectDao(projectId);
        deleteData(projectDao);
        LOG.info("项目 {} 的平均分数据已清空", projectId);

        aggregateProjectAverage(projectId, projectDao);
        LOG.info("项目 {} 的总分平均分统计完毕", projectId);

        aggregateSubjectAverage(projectId, projectDao);
        LOG.info("项目 {} 的科目平均分统计完毕", projectId);

        aggregateQuestAverage(projectId, projectDao);
        LOG.info("项目 {} 的题目平均分统计完毕", projectId);
    }

    private void deleteData(DAO projectDao) {
        projectDao.execute("truncate table average_project");
        projectDao.execute("truncate table average_subject");
        projectDao.execute("truncate table average_quest");
    }

    private void aggregateQuestAverage(final String projectId, final DAO projectDao) throws Exception {
        final AtomicInteger counter = new AtomicInteger(0);
        final List<ExamQuest> examQuests = questService.queryQuests(projectId);
        int questCount = examQuests.size();

        ThreadPools.createAndRunThreadPool(CONCURRENCY, QUEUE_SIZE, pool ->
                examQuests.forEach(examQuest -> {
                    final String questId = examQuest.getId();
                    pool.submit(() -> aggregateQuestAverage0(projectId, projectDao, counter, questId, questCount));
                })
        );
    }

    private void aggregateQuestAverage0(String projectId, DAO projectDao, AtomicInteger counter, String questId, int questCount) {
        projectDao.execute(PROVINCE_QUEST_AVG_TEMPLATE.replace("{{quest}}", questId).replace("{{province}}", "430000"));
        projectDao.execute(SCHOOL_QUEST_AVG_TEMPLATE.replace("{{quest}}", questId));
        projectDao.execute(CLASS_QUEST_AVG_TEMPLATE.replace("{{quest}}", questId));
        LOG.info("项目 {} 的题目平均分统计完成 {}/{}", projectId, counter.incrementAndGet(), questCount);
    }

    private void aggregateSubjectAverage(String projectId, DAO projectDao) throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);

        List<String> subjectIds = subjectService.listSubjects(projectId)
                .stream().map(ExamSubject::getId).collect(Collectors.toList());

        int subjectCount = subjectIds.size();

        ThreadPools.createAndRunThreadPool(subjectCount, 1,
                pool -> subjectIds.forEach(
                        subject -> pool.submit(
                                () -> aggregateSubjectAverage0(projectId, projectDao, subject, counter, subjectCount))));
    }

    private void aggregateSubjectAverage0(String projectId, DAO projectDao, String subject, AtomicInteger counter, int subjectCount) {
        projectDao.execute(PROVINCE_SUBJECT_AVG_TEMPLATE.replace("{{subject}}", subject).replace("{{province}}", "430000"));
        projectDao.execute(SCHOOL_SUBJECT_AVG_TEMPLATE.replace("{{subject}}", subject));
        projectDao.execute(CLASS_SUBJECT_AVG_TEMPLATE.replace("{{subject}}", subject));
        LOG.info("项目 {} 的科目平均分统计完成 {}/{}", projectId, counter.incrementAndGet(), subjectCount);
    }

    private void aggregateProjectAverage(String projectId, DAO projectDao) {
        projectDao.execute(PROVINCE_TOTAL_AVG_TEMPLATE.replace("{{province}}", "430000"));
        projectDao.execute(SCHOOL_TOTAL_AVG_TEMPLATE);
        projectDao.execute(CLASS_TOTAL_AVG_TEMPLATE);
    }
}
