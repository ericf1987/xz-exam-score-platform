package com.xz.scorep.executor.scorecheck;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xz.ajiaedu.common.http.HttpRequest;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author luckylo
 * @createTime 2017-07-11.
 */
public class LowerScoreCheckControllerTest extends BaseTest {

    @Test
    public void testQueryLowerScoreStudent() throws IOException {
        String url = "http://10.10.22.154:8180/check";
        HttpRequest request = new HttpRequest(url);
        request.setParameter("projectId","430100-dd3013ab961946fb8a3668e5ccc475b6");
        request.setParameter("subjectIds","001");       //多个科目Id  用逗号隔开
        request.setParameter("checkType","subject");    //默认subject,{subject,subjective,objective}
        request.setParameter("score","60");             //分数

        String result = request.requestPost();
        JSONObject json = JSON.parseObject(result);
        System.out.println(json);
    }
}