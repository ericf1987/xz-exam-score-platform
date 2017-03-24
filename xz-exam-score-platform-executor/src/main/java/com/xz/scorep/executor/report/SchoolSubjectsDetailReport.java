package com.xz.scorep.executor.report;

import com.hyd.dao.Row;
import com.xz.scorep.executor.aggritems.SchoolDetailReportQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询某个学校全科详情报表
 * Author: luckylo
 * Date : 2017-03-14
 */
@Component
public class SchoolSubjectsDetailReport extends AbstractReport {

    @Autowired
    SchoolDetailReportQuery query;

    @Override
    public Map<?, ?> generateReport(String projectId, String schoolId, String subjectId) {
        Map<Object,Object> result = new HashMap<>();
        Row schoolTotalRow = query.getSchoolSubjectsTotalDetail(projectId,schoolId);
        List<Row> classRows = query.getClassSubjectsTotalDetail(projectId,schoolId);
        result.put("school",schoolTotalRow);
        result.put("class",classRows);
        return result;
    }
}
