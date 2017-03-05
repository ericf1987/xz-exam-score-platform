package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.DAOException;
import com.xz.scorep.executor.aggregate.AggragateOrder;
import com.xz.scorep.executor.aggregate.Aggregator;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.ProjectClass;
import com.xz.scorep.executor.bean.ProjectSchool;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.utils.AsyncCounter;
import com.xz.scorep.executor.utils.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@AggragateOrder(4)
@Component
public class RankAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(RankAggregator.class);

    public static final String RANGE_CLASS_TEMPLATE = "where student_id in(" +
            "    select student.id from student where student.class_id='{{class}}'\n" +
            ")";

    public static final String RANGE_SCHOOL_TEMPLATE = "where student_id in(" +
            "    select student.id from student where student.school_id='{{school}}'\n" +
            ")";

    public static final String INSERT_TEMPLATE = "insert into {{rank_table}}\n" +
            "select student_id, '{{subject}}' as subject_id, `rank` from (\n" +
            "  select \n" +
            "    student_id, score,\n" +
            "    @prev := @curr,\n" +
            "    @curr := score,\n" +
            "    @rank := IF(@prev = @curr, @rank, @rank+@step) as `rank`,\n" +
            "    @step := IF(@prev = @curr, (@step+1), 1) as step\n" +
            "  from \n" +
            "    (select * from {{score_table}} {{range_template}}) score,\n" +
            "    (select @curr := null, @prev := null, @rank := 0, @step := 1) tmp1\n" +
            "  order by score desc\n" +
            ") tmp2";

    public static final int POOL_SIZE = 20;

    public static final int QUEUE_SIZE = 1;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private ClassService classService;

    @Autowired
    private DAOFactory daoFactory;

    @Override
    public void aggregate(String projectId) throws Exception {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table rank_province");
        projectDao.execute("truncate table rank_school");
        projectDao.execute("truncate table rank_class");

        LOG.info("排名表清空完毕。");

        ThreadPools.createAndRunThreadPool(
                POOL_SIZE, QUEUE_SIZE,
                pool -> startAggregation(projectId, projectDao, pool));

    }

    private void startAggregation(String projectId, DAO projectDao, ThreadPoolExecutor pool) {

        List<String> subjectIdList = subjectService.listSubjects(projectId)
                .stream().map(ExamSubject::getId).collect(Collectors.toList());


        ////////////////////////////////////////////////////////////// 整体排名

        AsyncCounter provinceCounter = new AsyncCounter("考试整体排名", subjectIdList.size() + 1);

        submitExecution(pool, provinceCounter, projectDao, INSERT_TEMPLATE
                .replace("{{score_table}}", "score_project")
                .replace("{{rank_table}}", "rank_province")
                .replace("{{subject}}", "000")
                .replace("{{range_template}}", ""));

        subjectIdList.forEach(subjectId ->
                submitExecution(pool, provinceCounter, projectDao, INSERT_TEMPLATE
                        .replace("{{score_table}}", "score_subject_" + subjectId)
                        .replace("{{rank_table}}", "rank_province")
                        .replace("{{subject}}", subjectId)
                        .replace("{{range_template}}", "")));

        ////////////////////////////////////////////////////////////// 学校排名

        List<String> schoolIds = schoolService.listSchool(projectId)
                .stream().map(ProjectSchool::getId).collect(Collectors.toList());

        int schoolTotal = schoolIds.size() * (subjectIdList.size() + 1);
        AsyncCounter schoolCounter = new AsyncCounter("考试学校排名", schoolTotal);

        schoolIds.forEach(schoolId -> {

            String rangeTemplate = RANGE_SCHOOL_TEMPLATE.replace("{{school}}", schoolId);

            submitExecution(pool, schoolCounter, projectDao, INSERT_TEMPLATE
                    .replace("{{score_table}}", "score_project")
                    .replace("{{rank_table}}", "rank_school")
                    .replace("{{subject}}", "000")
                    .replace("{{range_template}}", rangeTemplate));

            subjectIdList.forEach(subjectId ->
                    submitExecution(pool, schoolCounter, projectDao, INSERT_TEMPLATE
                            .replace("{{score_table}}", "score_subject_" + subjectId)
                            .replace("{{rank_table}}", "rank_school")
                            .replace("{{subject}}", subjectId)
                            .replace("{{range_template}}", rangeTemplate)));
        });

        ////////////////////////////////////////////////////////////// 班级排名

        List<String> classIds = classService.listClasses(projectId)
                .stream().map(ProjectClass::getId).collect(Collectors.toList());

        int classTotal = classIds.size() * (subjectIdList.size() + 1);
        AsyncCounter classCounter = new AsyncCounter("考试班级排名", classTotal);

        classIds.forEach(classId -> {

            String rangeTemplate = RANGE_CLASS_TEMPLATE.replace("{{class}}", classId);

            submitExecution(pool, classCounter, projectDao, INSERT_TEMPLATE
                    .replace("{{score_table}}", "score_project")
                    .replace("{{rank_table}}", "rank_class")
                    .replace("{{subject}}", "000")
                    .replace("{{range_template}}", rangeTemplate));

            subjectIdList.forEach(subjectId ->
                    submitExecution(pool, projectDao, rangeTemplate, subjectId, classCounter));
        });
    }

    private void submitExecution(
            ThreadPoolExecutor pool, DAO projectDao, String rangeTemplate, String subjectId, AsyncCounter counter) {

        pool.submit(() -> {
            aggregateClassSubjectRank(projectDao, rangeTemplate, subjectId);
            counter.count();
        });
    }

    protected void aggregateClassSubjectRank(DAO projectDao, String rangeTemplate, String subjectId) {
        projectDao.execute(INSERT_TEMPLATE
                .replace("{{score_table}}", "score_subject_" + subjectId)
                .replace("{{rank_table}}", "rank_class")
                .replace("{{subject}}", subjectId)
                .replace("{{range_template}}", rangeTemplate));
    }

    private void submitExecution(ExecutorService pool, AsyncCounter counter, final DAO projectDao, final String sql) {
        pool.submit(() -> {
            try {
                projectDao.execute(sql);
                counter.count();
            } catch (DAOException e) {
                LOG.error("统计排名失败", e);
            }
        });
    }
}
