package com.xz.scorep.executor.aggregate;

import com.xz.ajiaedu.common.http.HttpRequest;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * Author: luckylo
 * Date : 2017-03-20
 */
public class AggregateControllerTest extends BaseTest {


    @Test
    public void test() throws IOException {
        String url = "http://10.10.22.212:8180/aggr/start";
        HttpRequest request = new HttpRequest(url)
                .setParameter("projectId", PROJECT2_ID)
                .setParameter("aggrType", AggregateType.Quick.name())
                .setParameter("async", "true")
                .setParameter("importScore", "false");

        String response = request.requestPost();
        System.out.println(response);
    }

    @Test
    public void test111() throws IOException {
        String  url = "http://10.10.22.212:8180/aggr/start/430300-564140e278df4e92a2a739a6f27ac391/Quick/true/true/true/001";
        HttpRequest request = new HttpRequest(url);
        String post = request.requestPost();
        System.out.println(post);
    }

    @Test
    public void testSplit() throws Exception {
        System.out.println(Arrays.asList("001".split(",")));
    }
}