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
public class ReportControllerTest extends BaseTest {

    public static final String PROJECT_ID = BaseTest.PROJECT_ID;

    static {
        BaseTest.setupProxy();
    }

    @Test
    public void testURL() throws IOException {
        String url = "http://10.10.22.212:8180/aggr/status/430900-9e8f3c054d72414b81cdd99bd48da695";
        HttpRequest request = new HttpRequest(url);
        String response = request.request();
        System.out.println(response);
    }


    @Test
    public void test() {
        String url = "http://10.10.22.212:8180/aggr/start/430300-564140e278df4e92a2a739a6f27ac391/Quick/true/false/false";
        HttpRequest request = new HttpRequest(url);
        try {
            String post = request.requestPost();
            System.out.println(post);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test111() throws IOException {
        String url = "http://10.10.22.212:8180/report/" + PROJECT2_ID + "/1b4289a9-58e2-4560-8617-27f791f956b6/000/SubjectsAverageScore";
        HttpRequest request = new HttpRequest(url);
        String response = request.request();
        System.out.println(response);
    }

    @Test
    public void archiveProjectReport() throws Exception {
        HttpRequest archiveRequest = new HttpRequest("http://10.10.22.212:8180/report/archive", HttpRequest.DEFAULT_TIMEOUT * 10);
        archiveRequest.setParameter("projectId","430900-9e8f3c054d72414b81cdd99bd48da695");
//        archiveRequest.setParameter("subjectId","002");
        ThreadTimer.____start____("Generating report archive");
        String response = archiveRequest.requestPost();
        System.out.println(response);
        ThreadTimer.____tag____("Generating report archive finished");
    }

    @Test
    public void queryProjectReport() throws Exception {
        HttpRequest queryRequest = new HttpRequest("http://10.10.22.212:8180/report/archive-status/" + "430300-564140e278df4e92a2a739a6f27ac391");
        System.out.println(queryRequest.request());
    }

    @Test
    public void archiveSubjectReport() throws Exception {

    }


}