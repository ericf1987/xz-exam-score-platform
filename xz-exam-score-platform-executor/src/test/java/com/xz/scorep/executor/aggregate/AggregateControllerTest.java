package com.xz.scorep.executor.aggregate;

import com.xz.ajiaedu.common.http.HttpRequest;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Author: luckylo
 * Date : 2017-03-20
 */
public class AggregateControllerTest extends BaseTest {


    @Test
    public void test() throws IOException {
        String url = "http://10.10.22.212:8180/aggr/start/430300-9cef9f2059ce4a36a40a7a60b07c7e00/Basic/false/true/true";
        HttpRequest request = new HttpRequest(url);
        String response = request.requestPost();
        System.out.println(response);
    }

}