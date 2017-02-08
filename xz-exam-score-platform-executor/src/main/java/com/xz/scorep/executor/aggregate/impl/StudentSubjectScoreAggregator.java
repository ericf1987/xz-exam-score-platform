package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.xz.ajiaedu.common.concurrent.Executors;
import com.xz.scorep.executor.aggregate.AggragateOrder;
import com.xz.scorep.executor.aggregate.Aggregator;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.project.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@AggragateOrder(0)
public class StudentSubjectScoreAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(StudentSubjectScoreAggregator.class);

    @Autowired
    private QuestService questService;

    @Autowired
    private SubjectService subjectService;

    @Override
    public void aggregate(String projectId) throws Exception {
        DAO projectDao = daoFactory.getProjectDao(projectId);

        ThreadPoolExecutor executor = Executors.newBlockingThreadPoolExecutor(20, 20, 500);

        subjectService.querySubjectIds(projectId).forEach(subjectId -> {
            String tableName = "score_subject_" + subjectId;

            // 初始化
            projectDao.execute("truncate table " + tableName);
            projectDao.execute("insert into " + tableName + "(student_id,score) select id, 0 from student");
            LOG.info("项目 {} 的科目 {} 总分已清空", projectId, subjectId);

            // 累加分数
            List<ExamQuest> examQuests = questService.queryQuests(projectId, subjectId);
            final AtomicInteger counter = new AtomicInteger(0);
            Runnable whenFinished = () -> LOG.info(
                    "项目 {} 的科目 {} 总分合计已完成 {}/{}", projectId, subjectId, counter.incrementAndGet(), examQuests.size());

            examQuests.forEach(
                    examQuest -> executor.submit(
                            () -> accumulateScore(projectDao, tableName, examQuest, whenFinished)));
        });


        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);
    }

    private void accumulateScore(DAO projectDao, String tableName, ExamQuest examQuest, Runnable whenFinished) {
        String questId = examQuest.getId();

        String combineSql = "update " + tableName + " p \n" +
                "  left join score_" + questId + " q on p.student_id=q.student_id\n" +
                "  set p.score=p.score+q.score";

        projectDao.execute(combineSql);

        if (whenFinished != null) {
            whenFinished.run();
        }
    }
}
