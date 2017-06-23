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
 * 根据学生各科排名等级，汇总考试排名等级
 *
 * @author by fengye on 2017/6/20.
 */
@AggregateTypes(AggregateType.Basic)
@AggregateOrder(71)
@Component
public class RankLevelProjectAggregator extends Aggregator {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    SubjectService subjectService;

    public static final Logger LOG = LoggerFactory.getLogger(RankLevelProjectAggregator.class);

    public static final String[] RANGES = new String[]{
            Range.PROVINCE, Range.SCHOOL, Range.CLASS
    };

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {

        LOG.info("开始执行 排名等级（项目） 统计 ：{}", this.getClass().getSimpleName());

        long begin = System.currentTimeMillis();

        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        //查询考试科目
        List<String> subjectIds = subjectService.listSubjects(projectId).stream().filter(s -> !BooleanUtils.toBoolean(s.getVirtualSubject()))
                .map(ExamSubject::getId).collect(Collectors.toList());

        projectDao.execute("truncate table rank_level_project");

        ThreadPoolExecutor executor = Executors.newBlockingThreadPoolExecutor(5, 5, 5);

        for (String rangeName : RANGES){
            executor.submit(() -> processData(projectDao, rangeName, subjectIds));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);

        long end = System.currentTimeMillis();

        LOG.info("结束执行 排名等级（项目） 统计:{}， 耗时:{}", this.getClass().getSimpleName(), end - begin);
    }

    public SQL.Select getSelect(String rangeName, List<String> subjectIds){
        SQL.Select select = null;
        switch (rangeName){
            case Range.PROVINCE:
                select = SQL.Select("student_id", "group_concat(rank_level separator '') as rank_level")
                        .From("rank_level_province")
                        .Where("subject_id in ?", subjectIds.toArray())
                        .GroupBy("student_id");
                break;
            case Range.SCHOOL:
                select = SQL.Select("student_id", "group_concat(rank_level separator '') as rank_level")
                        .From("rank_level_school")
                        .Where("subject_id in ?", subjectIds.toArray())
                        .GroupBy("student_id");
                break;
            case Range.CLASS:
                select = SQL.Select("student_id", "group_concat(rank_level separator '') as rank_level")
                        .From("rank_level_class")
                        .Where("subject_id in ?", subjectIds.toArray())
                        .GroupBy("student_id");
                break;
        }
        return select;
    }

    private void processData(DAO projectDao, String rangeName, List<String> subjectIds) {
        List<Row> result = projectDao.query(getSelect(rangeName, subjectIds));
        result.stream().forEach(r -> r.put("range_type", rangeName));
        projectDao.insert(result, "rank_level_project");
    }

}
