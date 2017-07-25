package com.xz.scorep.executor.exportaggrdata.controller;

import com.xz.ajiaedu.common.http.HttpRequest;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author luckylo
 * @createTime 2017-07-19.
 */
public class ExportControllerTest extends BaseTest {
    @Test
    public void test() throws IOException {
        String URL = "http://10.10.22.154:8180/export/json/";
        String projectId = "430200-13e01c025ac24c6497d916551b3ae7a6";
        HttpRequest request = new HttpRequest(URL);
        request.setParameter("projectId", projectId);
        String res = request.requestPost();
        System.out.println(res);
    }

}