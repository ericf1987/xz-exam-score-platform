package com.xz.scorep.executor.exportaggrdata.controller;

import com.xz.ajiaedu.common.http.HttpRequest;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author luckylo
 * @createTime 2017-07-19.
 */
public class ExportControllerTest extends BaseTest {
    @Test
    public void test() throws IOException {
        String URL = "http://10.10.22.154:8180/export/json/";
        String projectId = "430000-6c4add56e5fb42b09f9de5387dfa59c0";
        HttpRequest request = new HttpRequest(URL);
        request.setParameter("projectId",projectId);
        String res = request.requestPost();
        System.out.println(res);
    }
}