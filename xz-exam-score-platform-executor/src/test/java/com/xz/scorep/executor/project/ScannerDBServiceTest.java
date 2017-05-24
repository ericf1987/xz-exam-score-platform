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
        String projectId = "430100-62fb00af4f04407e9e4383aa7cd4fdf0";
        String subjectId = "006";
        String studentId = "d1f7e433-be35-4c9a-a0ed-42816426892c";
        Map<String, Object> oneStudentCardSlice = scannerDBService.getOneStudentCardSlice(projectId, studentId, subjectId);
        System.out.println(oneStudentCardSlice.toString());
    }
}