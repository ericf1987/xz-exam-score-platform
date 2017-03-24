package com.xz.scorep.executor.report;

import com.hyd.dao.Row;
import com.xz.scorep.executor.aggritems.SchoolDetailReportQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询学校单科详细报表
 * Author: luckylo
 * Date : 2017-03-15
 */
@Component
public class SchoolSubjectReport extends AbstractReport {
    private final Logger LOG = LoggerFactory.getLogger(SchoolSubjectReport.class);
    @Autowired
    SchoolDetailReportQuery query;

    @Override
    public Map<?, ?> generateReport(String projectId, String schoolId, String subjectId) {
        LOG.info("开始查询项目ID:{},学校ID{},科目ID{}",projectId,schoolId,subjectId);
        Map<Object, Object> result = new HashMap<>();

        Row totalDetail = query.getSchoolSubjectTotalDetail(projectId, schoolId, subjectId);
        result.put("school", totalDetail);
        List<Row> classRows = query.getClassSubjectTotalDetail(projectId, schoolId, subjectId);
        result.put("class", classRows);
        return result;
    }
}
