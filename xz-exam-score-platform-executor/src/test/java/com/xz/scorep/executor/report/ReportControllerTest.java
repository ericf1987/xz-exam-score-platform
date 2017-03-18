package com.xz.scorep.executor.report;

import com.xz.ajiaedu.common.debug.ThreadTimer;
import com.xz.ajiaedu.common.http.HttpRequest;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;

import java.io.IOException;

/**
 * (description)
 * created at 2017/3/15
 *
 * @author yidin
 */
public class ReportControllerTest {

    public static final String PROJECT_ID = BaseTest.PROJECT_ID;

    static {
        BaseTest.setupProxy();
    }

    @Test
    public void testURL() throws IOException {
        String url = "http://10.10.22.212:8180/aggr/status/430300-9cef9f2059ce4a36a40a7a60b07c7e00";
        HttpRequest request = new HttpRequest(url);
        String response = request.request();
        System.out.println(response);
    }


    @Test
    public void test() throws IOException {
        String url = "http://10.10.22.212:8180/report/" + PROJECT_ID + "/1b4289a9-58e2-4560-8617-27f791f956b6/000/SubjectsAverageScore";
        HttpRequest request = new HttpRequest(url);
        String response = request.request();
        System.out.println(response);
    }

    @Test
    public void archiveProjectReport() throws Exception {
        HttpRequest archiveRequest = new HttpRequest("http://10.10.22.212:8180/report/archive/" + PROJECT_ID, HttpRequest.DEFAULT_TIMEOUT * 10);
        ThreadTimer.____start____("Generating report archive");
        String response = archiveRequest.requestPost();
        System.out.println(response);
        ThreadTimer.____tag____("Generating report archive finished");
    }

    @Test
    public void queryProjectReport() throws Exception {
        HttpRequest queryRequest = new HttpRequest("http://10.10.22.212:8180/report/archive-status/" + PROJECT_ID);
        System.out.println(queryRequest.request());
    }

    @Test
    public void archiveSubjectReport() throws Exception {

    }


}