package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.xz.ajiaedu.common.concurrent.Executors;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.ProjectClass;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 学生主客观题排名统计(只统计班级维度)
 * 必须在主客观题分数统计之后(应用于答题留痕)
 *
 * @author luckylo
 */
@AggregateTypes(AggregateType.Quick)
@AggregateOrder(7)
@Component
public class StudentObjectiveRankAggregator extends Aggregator {

    private static Logger LOG = LoggerFactory.getLogger(StudentObjectiveRankAggregator.class);

    private static String INSERT = "insert into {{targetTable}} select student_id,'{{subjectId}}' subject_id,rank from(" +
            "select student_id,score,@last := @current,@current :=score," +
            "@rank := if(@last = @current,@rank,@rank+@step) as `rank`," +
            "@step := if(@last = @current,@step+1,1) as step from " +
            "(select * from {{sourceTable}} where student_id in (select id from student where class_id = '{{classId}}')) score," +
            "(select @last := null,@current := null,@rank :=0,@step := 1) temp order by score desc" +
            ") tmp";

    private static String OBJECTIVE = "rank_objective";

    private static String SUBJECTIVE = "rank_subjective";

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ClassService classService;

    @Autowired
    private DAOFactory daoFactory;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table rank_objective ");
        projectDao.execute("truncate table rank_subjective ");
        List<ExamSubject> subjects = subjectService
                .listSubjects(projectId)
                .stream()
                .filter(subject -> !Boolean.valueOf(subject.getVirtualSubject()))
                .collect(Collectors.toList());

        LOG.info("开始统计项目ID  {} 主客观题排名....", projectId);
        ThreadPoolExecutor pool = Executors.newBlockingThreadPoolExecutor(10, 10, 1);
        subjects.forEach(subject -> processRank(projectDao, subject, projectId, pool));
        LOG.info("项目ID  {} 主客观题排名统计完成....", projectId);
    }

    private void processRank(DAO projectDao, ExamSubject subject, String projectId, ThreadPoolExecutor pool) {
        String subjectId = subject.getId();
        String objectiveTable = "score_objective_" + subjectId;
        String subjectiveTable = "score_subjective_" + subjectId;

        List<ProjectClass> classList = classService.listClasses(projectId);
        classList.forEach(clazz -> {
            String classId = clazz.getId();

            //班级  主观题 得分排名
            String objectiveSql = INSERT
                    .replace("{{targetTable}}", OBJECTIVE)
                    .replace("{{subjectId}}", subjectId)
                    .replace("{{sourceTable}}", objectiveTable)
                    .replace("{{classId}}", classId);
            pool.submit(() -> projectDao.execute(objectiveSql));

            //班级  客观题 得分排名
            String subjectiveSql = INSERT
                    .replace("{{targetTable}}", SUBJECTIVE)
                    .replace("{{subjectId}}", subjectId)
                    .replace("{{sourceTable}}", subjectiveTable)
                    .replace("{{classId}}", classId);
            pool.submit(() -> projectDao.execute(subjectiveSql));

        });

    }
}
