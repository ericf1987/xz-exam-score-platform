package com.xz.scorep.executor.aggritems;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * (description)
 * created at 2017/2/14
 *
 * @author yidin
 */
public class StudentCountQueryTest extends BaseTest {

    @Autowired
    private StudentCountQuery studentCountQuery;

    @Test
    public void testGetSchoolStudentCount() throws Exception {
        String schoolId = "207ae6fd_2e1d_41d7_96e8_d2bb68de3cb4";
        String projectId = "fake_project";
        int count = studentCountQuery.getSchoolStudentCount(projectId, schoolId);
        System.out.println(count);
    }

    @Test
    public void testCountSchoolClassStudent() throws Exception {
        String schoolId = "207ae6fd_2e1d_41d7_96e8_d2bb68de3cb4";
        String projectId = "fake_project";
        Map<String, Integer> count = studentCountQuery.getClassStudentCount(projectId, schoolId);
        System.out.println(count);
    }

}