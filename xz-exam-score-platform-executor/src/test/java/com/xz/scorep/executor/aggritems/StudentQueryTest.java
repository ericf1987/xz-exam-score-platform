package com.xz.scorep.executor.aggritems;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * (description)
 * created at 2017/2/14
 *
 * @author yidin
 */
public class StudentQueryTest extends BaseTest {

    @Autowired
    private StudentQuery studentQuery;

    @Test
    public void testGetSchoolStudentCount() throws Exception {
        String schoolId = "207ae6fd_2e1d_41d7_96e8_d2bb68de3cb4";
        String projectId = "fake_project";
        int count = studentQuery.getSchoolStudentCount(projectId, schoolId);
        System.out.println(count);
    }

    @Test
    public void testCountSchoolClassStudent() throws Exception {
        String schoolId = "207ae6fd_2e1d_41d7_96e8_d2bb68de3cb4";
        String projectId = "fake_project";
        Map<String, Integer> count = studentQuery.getClassStudentCount(projectId, schoolId);
        System.out.println(count);
    }

    @Test
    public void testQueryStudentList() throws Exception {
        String classId = "9afd461b_85e4_4617_8d11_a0765c5aa053";
        studentQuery.listStudentInfo(
                "fake_project_small", Range.clazz(classId))
                .forEach(System.out::println);
    }
}