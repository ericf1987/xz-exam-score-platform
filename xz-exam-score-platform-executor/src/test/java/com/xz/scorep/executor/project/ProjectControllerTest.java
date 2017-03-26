package com.xz.scorep.executor.project;

import com.xz.ajiaedu.common.http.HttpRequest;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Author: luckylo
 * Date : 2017-03-25
 */
public class ProjectControllerTest extends BaseTest {

    @Test
    public void test() throws IOException {
        String url = "http://10.10.22.212:8180/project/status?projectId=430300-564140e278df4e92a2a739a6f27ac391";
        HttpRequest request = new HttpRequest(url);
        String response = request.request();
        System.out.printf(response);
    }

}