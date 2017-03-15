package com.xz.scorep.executor.report;

import com.xz.ajiaedu.common.http.HttpRequest;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;

/**
 * (description)
 * created at 2017/3/15
 *
 * @author yidin
 */
public class ReportControllerTest {

    public static final String PROJECT_ID = BaseTest.PROJECT_ID;

    @Test
    public void archiveProjectReport() throws Exception {
        HttpRequest request = new HttpRequest("http://localhost:8080/report/archive/" + PROJECT_ID);
        String response = request.requestPost();
        System.out.println("RESPONSE: " + response);
    }

    @Test
    public void archiveSubjectReport() throws Exception {

    }


}