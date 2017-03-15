package com.xz.scorep.executor.aggritems;

import com.hyd.dao.Row;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * (description)
 * created at 2017/2/14
 *
 * @author yidin
 */
public class AverageQueryTest extends BaseTest {

    @Autowired
    private AverageQuery averageQuery;

    @Test
    public void getProvinceSubjectAverages() throws Exception {
        Map<String, String> averages = averageQuery.getProvinceSubjectAverages(PROJECT_ID);
        averages.entrySet().forEach(System.out::println);
    }

    @Test
    public void getSchoolSubjectAverages() throws Exception {
        List<Row> averages = averageQuery.getSchoolSubjectAverages(PROJECT_ID, SCHOOL_ID);
        averages.forEach(System.out::println);
    }

    @Test
    public void getSchoolProjectAverage() throws Exception {
        double average = averageQuery.getSchoolProjectAverage(PROJECT_ID, SCHOOL_ID);
        System.out.println(average);
    }

    @Test
    public void getClassProjectAverages() throws Exception {
        Map<String, Double> averages = averageQuery.getClassProjectAverages(PROJECT_ID, SCHOOL_ID);
        averages.entrySet().forEach(System.out::println);
    }

}