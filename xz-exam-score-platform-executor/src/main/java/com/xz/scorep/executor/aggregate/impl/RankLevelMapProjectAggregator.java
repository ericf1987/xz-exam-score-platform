package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.dao.SQL;
import com.xz.ajiaedu.common.concurrent.Executors;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author by fengye on 2017/6/20.
 */
@AggregateTypes(AggregateType.Basic)
@AggregateOrder(74)
@Component
public class RankLevelMapProjectAggregator extends Aggregator {
    @Autowired
    DAOFactory daoFactory;

    @Autowired
    SubjectService subjectService;

    public static final Logger LOG = LoggerFactory.getLogger(RankLevelMapProjectAggregator.class);

    public static final String[] RANGES = new String[]{
            Range.PROVINCE, Range.SCHOOL, Range.CLASS
    };

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {

        LOG.info("开始执行 排名等第（项目） 统计 ：{}", this.getClass().getSimpleName());

        long begin = System.currentTimeMillis();

        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table rank_level_map_project");

        processData(projectDao, Range.PROVINCE);

        ThreadPoolExecutor executor = Executors.newBlockingThreadPoolExecutor(5, 5, 5);

        for (String rangeName : RANGES) {
            executor.submit(() -> processData(projectDao, rangeName));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);

        long end = System.currentTimeMillis();

        LOG.info("结束执行 排名等第（项目） 统计:{}， 耗时:{}", this.getClass().getSimpleName(), end - begin);
    }

    private void processData(DAO projectDao, String rangeName) {
        List<Row> result = projectDao.query(getSelect(rangeName));
        result.stream().forEach(r -> {
            r.put("range_type", rangeName);
            r.put("project_id", "000");
        });
        projectDao.insert(result, "rank_level_map_project");
    }

    public SQL.Select getSelect(String rangeName) {
        SQL.Select select = null;
        switch (rangeName) {
            case Range.PROVINCE:
                select = SQL.Select("rank.rank_level", "stu.province range_id", "COUNT(rank_level) cnt")
                        .From("rank_level_project rank, student stu")
                        .Where("rank.student_id = stu.id")
                        .GroupBy("rank_level, province");
                break;
            case Range.SCHOOL:
                select = SQL.Select("rank.rank_level", "stu.school_id range_id", "COUNT(rank_level) cnt")
                        .From("rank_level_project rank, student stu")
                        .Where("rank.student_id = stu.id")
                        .GroupBy("rank_level, school_id");
                break;
            case Range.CLASS:
                select = SQL.Select("rank.rank_level", "stu.class_id range_id", "COUNT(rank_level) cnt")
                        .From("rank_level_project rank, student stu")
                        .Where("rank.student_id = stu.id")
                        .GroupBy("rank_level, class_id");
                break;
        }
        return select;
    }
}
