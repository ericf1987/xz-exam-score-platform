package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.DAOException;
import com.xz.scorep.executor.aggregate.AggragateOrder;
import com.xz.scorep.executor.aggregate.AggregateType;
import com.xz.scorep.executor.aggregate.AggregateTypes;
import com.xz.scorep.executor.aggregate.Aggregator;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.utils.AsyncCounter;
import com.xz.scorep.executor.utils.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 统计客观题选项选择率
 *
 * @author yidin
 */
@Component
@AggregateTypes(AggregateType.Basic)
@AggragateOrder(6)
public class ObjectiveOptionAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectiveOptionAggregator.class);

    private static final String PROVINCE_OPTION_RATE_TEMPLATE = "insert into objective_option_rate\n" +
            "select\n" +
            "  '{{quest}}' as quest_id,\n" +
            "  a.`option`,\n" +
            "  'province' as range_type,\n" +
            "  '{{province}}' as range_id,\n" +
            "  a.option_count,\n" +
            "  a.option_count/b.total as option_rate\n" +
            "from (\n" +
            "  select \n" +
            "    objective_answer as `option`, \n" +
            "    count(1) as option_count\n" +
            "  from `score_{{quest}}` score\n" +
            "  group by objective_answer\n" +
            ") a, (\n" +
            "  select count(1) as total from student\n" +
            ") b";

    private static final String SCHOOL_OPTION_RATE_TEMPLATE = "insert into objective_option_rate\n" +
            "select\n" +
            "  '{{quest}}' as quest_id,\n" +
            "  a.`option`,\n" +
            "  'school' as range_type,\n" +
            "  a.school_id as range_id,\n" +
            "  a.option_count,\n" +
            "  a.option_count/b.total as option_rate\n" +
            "from (\n" +
            "  select\n" +
            "    student.school_id,\n" +
            "    objective_answer as `option`, \n" +
            "    count(1) as option_count\n" +
            "  from `score_{{quest}}` score,student\n" +
            "  where score.student_id=student.id\n" +
            "  group by school_id, objective_answer\n" +
            ") a, (\n" +
            "  select school_id, count(1) as total from student\n" +
            "  group by school_id\n" +
            ") b\n" +
            "where a.school_id=b.school_id";

    private static final String CLASS_OPTION_RATE_TEMPLATE = "insert into objective_option_rate\n" +
            "select\n" +
            "  '{{quest}}' as quest_id,\n" +
            "  a.`option`,\n" +
            "  'class' as range_type,\n" +
            "  a.class_id as range_id,\n" +
            "  a.option_count,\n" +
            "  a.option_count/b.total as option_rate\n" +
            "from (\n" +
            "  select\n" +
            "    student.class_id,\n" +
            "    objective_answer as `option`, \n" +
            "    count(1) as option_count\n" +
            "  from `score_{{quest}}` score,student\n" +
            "  where score.student_id=student.id\n" +
            "  group by class_id, objective_answer\n" +
            ") a, (\n" +
            "  select class_id, count(1) as total from student\n" +
            "  group by class_id\n" +
            ") b\n" +
            "where a.class_id=b.class_id";

    @Autowired
    private QuestService questService;

    @Autowired
    private DAOFactory daoFactory;

    @Override
    public void aggregate(String projectId) throws Exception {

        ThreadPools.createAndRunThreadPool(20, 1, pool -> {
            try {
                aggregate0(pool, projectId);
            } catch (Exception e) {
                LOG.error("客观题选项统计失败", e);
            }
        });
    }

    private void aggregate0(ThreadPoolExecutor pool, String projectId) {
        List<ExamQuest> examQuests = questService.queryQuests(projectId, true);
        String province = Range.PROVINCE_RANGE.getId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table objective_option_rate");
        LOG.info("客观题选项统计结果已清空。");

        AsyncCounter counter = new AsyncCounter("客观题选项统计", examQuests.size());
        for (ExamQuest quest : examQuests) {
            String provinceSql = PROVINCE_OPTION_RATE_TEMPLATE.replace("{{quest}}", quest.getId()).replace("{{province}}", province);
            String schoolSql = SCHOOL_OPTION_RATE_TEMPLATE.replace("{{quest}}", quest.getId());
            String classSql = CLASS_OPTION_RATE_TEMPLATE.replace("{{quest}}", quest.getId());

            pool.submit(() -> {
                try {
                    projectDao.execute(provinceSql);
                    projectDao.execute(schoolSql);
                    projectDao.execute(classSql);
                } catch (DAOException e) {
                    LOG.error("客观题选项统计失败", e);
                } finally {
                    counter.count();
                }
            });
        }
    }
}
