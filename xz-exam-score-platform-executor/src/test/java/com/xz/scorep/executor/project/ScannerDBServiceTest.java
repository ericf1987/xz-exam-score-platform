package com.xz.scorep.executor.project;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author by fengye on 2017/5/23.
 */
public class ScannerDBServiceTest extends BaseTest {

    @Autowired
    ScannerDBService scannerDBService;

    @Test
    public void testGetOneStudentCardSlice() throws Exception {
        String projectId = "430300-29c4d40d93bf41a5a82baffe7e714dd9";
        String schoolId = "710269b3-e856-4dcb-93d4-dd274bfc0b53";
        String classId = "42e34943-29e9-4784-b865-585de017b56b";
        String subjectId = "003";
        String studentId = "0000bf2f-ecee-4745-b2bb-ee9e1abb2fd1";
        Map<String, Object> oneStudentCardSlice = scannerDBService.getOneStudentCardSlice(projectId, studentId, subjectId);
        System.out.println(oneStudentCardSlice.toString());
    }
}