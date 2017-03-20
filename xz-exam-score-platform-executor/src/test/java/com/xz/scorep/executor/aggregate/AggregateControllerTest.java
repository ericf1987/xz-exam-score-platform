package com.xz.scorep.executor.aggregate;

import com.xz.ajiaedu.common.http.HttpRequest;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;

import java.io.IOException;

/**
 * Author: luckylo
 * Date : 2017-03-20
 */
public class AggregateControllerTest extends BaseTest {


    @Test
    public void test() throws IOException {
        String url = "http://10.10.22.212:8180/aggr/start";
        HttpRequest request = new HttpRequest(url)
                .setParameter("projectId", PROJECT_ID)
                .setParameter("aggrType", AggregateType.Quick.name())
                .setParameter("async", "true")
                .setParameter("importScore", "true")
                .setParameter("subjects", "001");

        String response = request.requestPost();
        System.out.println(response);
    }

}