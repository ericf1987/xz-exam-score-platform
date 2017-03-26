package com.xz.scorep.executor.exportexcel;

import com.xz.ajiaedu.common.http.HttpRequest;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;

import java.io.IOException;

/**
 * Author: luckylo
 * Date : 2017-03-26
 */
public class ExportExcelControllerTest extends BaseTest {
    @Test
    public void test() throws IOException {
        String url = "http://10.10.22.212:8180/export/excel";
        HttpRequest request = new HttpRequest(url)
                .setParameter("projectId", "430900-9e8f3c054d72414b81cdd99bd48da695");
        String response = request.requestPost();
        System.out.println(response);
    }

}