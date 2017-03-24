package com.xz.scorep.executor.report;

import com.hyd.dao.Row;
import com.xz.scorep.executor.aggritems.AverageQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询某个学校全科的平均分
 * 返回字段包含该学校所有科目的平均分
 * Author: luckylo
 * Date : 2017-03-14
 */
@Component
public class SubjectsAverageScoreReport extends AbstractReport {
    private final Logger LOG = LoggerFactory.getLogger(SubjectsAverageScoreReport.class);
    @Autowired
    AverageQuery averageQuery;

    @Override
    public Map<?, ?> generateReport(String projectId, String schoolId, String subjectId) {
        LOG.info("查询学校全科平均分,项目ID{},学校ID{}",projectId,schoolId);
        List<Row> rows = averageQuery.getSchoolSubjectAverages(projectId, schoolId);
        Map<Object, Object> result = new HashMap<>();
        result.put("subjectsAverage", rows);
        return result;
    }
}
