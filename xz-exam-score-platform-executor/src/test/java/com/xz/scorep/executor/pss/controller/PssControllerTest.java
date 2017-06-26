package com.xz.scorep.executor.pss.controller;

import com.xz.ajiaedu.common.http.HttpRequest;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author luckylo
 * @createTime 2017-06-23.
 */
public class PssControllerTest extends BaseTest {

    public static final String URL = "http://10.10.22.154:8180/img/task/start";

    @Test
    public void startPssTask() throws Exception {
        HttpRequest request = new HttpRequest(URL);
        request.setParameter("projectId","430200-13e01c025ac24c6497d916551b3ae7a6");
        request.setParameter("subjectId","");
        String result = request.requestPost();
        System.out.println(result);

    }

    @Test
    public void test() {
        System.out.println(StringUtil.isEmpty(""));
    }
}