package com.xz.scorep.executor.aggritems;

import com.hyd.dao.Row;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Author: luckylo
 * Date : 2017-03-15
 */
public class SchoolDetailReportQueryTest extends BaseTest {
    @Autowired
    SchoolDetailReportQuery query;
    @Test
    public void getSchoolSubjectsTotalDetail() throws Exception {
        Row row = query.getSchoolSubjectsTotalDetail(PROJECT_ID,"1b4289a9-58e2-4560-8617-27f791f956b6");
        System.out.println(row);
    }

    @Test
    public void getSchoolClassTotalDetail() throws Exception {
        List<Row> rows = query.getClassSubjectsTotalDetail(PROJECT_ID, "1b4289a9-58e2-4560-8617-27f791f956b6");
        rows.forEach(s->System.out.println(s));
    }

    @Test
    public void getSubjectTotalDetail() throws Exception {
        Row row = query.getSchoolSubjectTotalDetail(PROJECT_ID,"1b4289a9-58e2-4560-8617-27f791f956b6","001");
        System.out.println(row);

    }

    @Test
    public void getClassSubjectTotalDetail() throws Exception {
        List<Row> rows = query.getClassSubjectTotalDetail(PROJECT_ID, "1b4289a9-58e2-4560-8617-27f791f956b6","001");
        rows.forEach(s->System.out.println(s));
    }

}