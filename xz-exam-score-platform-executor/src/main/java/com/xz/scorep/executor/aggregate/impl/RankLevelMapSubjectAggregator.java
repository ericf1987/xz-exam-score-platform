package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.dao.SQL;
import com.xz.ajiaedu.common.concurrent.Executors;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SubjectService;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author by fengye on 2017/6/20.
 */
@AggregateTypes(AggregateType.Advanced)
@AggregateOrder(76)
@Component
public class RankLevelMapSubjectAggregator extends Aggregator{

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    SubjectService subjectService;

    public static final Logger LOG = LoggerFactory.getLogger(RankLevelMapSubjectAggregator.class);

    public static final String[] RANGES = new String[]{
            Range.PROVINCE, Range.SCHOOL, Range.CLASS
    };

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {

        LOG.info("开始执行 排名等第（科目） 统计 ：{}", this.getClass().getSimpleName());

        long begin = System.currentTimeMillis();

        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        //查询考试科目
        List<String> subjectIds = subjectService.listSubjects(projectId).stream().filter(s -> !BooleanUtils.toBoolean(s.getVirtualSubject()))
                .map(ExamSubject::getId).collect(Collectors.toList());

        projectDao.execute("truncate table rank_level_map_province");
        projectDao.execute("truncate table rank_level_map_school");
        projectDao.execute("truncate table rank_level_map_class");

        ThreadPoolExecutor executor = Executors.newBlockingThreadPoolExecutor(5, 5, 5);

        for (String rangeName : RANGES){
            executor.submit(() -> processData(projectDao, rangeName, subjectIds));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);

        long end = System.currentTimeMillis();

        LOG.info("结束执行 排名等第（科目） 统计:{}， 耗时:{}", this.getClass().getSimpleName(), end - begin);
    }

    private void processData(DAO projectDao, String rangeName, List<String> subjectIds) {
        List<Row> result = projectDao.query(getSelect(rangeName, subjectIds));
        projectDao.insert(result, getTableName(rangeName));
    }

    private String getTableName(String rangeName) {
        String tableName = "";
        switch (rangeName){
            case Range.PROVINCE:
                tableName = "rank_level_map_province";
                break;
            case Range.SCHOOL:
                tableName = "rank_level_map_school";
                break;
            case Range.CLASS:
                tableName = "rank_level_map_class";
                break;
        }
        return tableName;
    }

    public SQL.Select getSelect(String rangeName, List<String> subjectIds){
        SQL.Select select = null;
        switch (rangeName){
            case Range.PROVINCE:
                select = SQL.Select("rank.subject_id", "rank.rank_level", "stu.province", "COUNT(rank_level) cnt")
                        .From("rank_level_province rank, student stu")
                        .Where("subject_id in ?", subjectIds.toArray())
                        .And("rank.student_id = stu.id")
                        .GroupBy("rank_level, province, subject_id");
                break;
            case Range.SCHOOL:
                select = SQL.Select("rank.subject_id", "rank.rank_level", "stu.school_id", "COUNT(rank_level) cnt")
                        .From("rank_level_school rank, student stu")
                        .Where("subject_id in ?", subjectIds.toArray())
                        .And("rank.student_id = stu.id")
                        .GroupBy("rank_level, school_id, subject_id");
                break;
            case Range.CLASS:
                select = SQL.Select("rank.subject_id", "rank.rank_level", "stu.class_id", "COUNT(rank_level) cnt")
                        .From("rank_level_class rank, student stu")
                        .Where("subject_id in ?", subjectIds.toArray())
                        .And("rank.student_id = stu.id")
                        .GroupBy("rank_level, class_id, subject_id");
                break;
        }
        return select;
    }
}
