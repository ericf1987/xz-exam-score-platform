package com.xz.scorep.manager.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xz.ajiaedu.common.http.HttpRequest;
import com.xz.scorep.manager.BaseTest;
import org.junit.Test;

import java.io.IOException;

/**
 * (description)
 * created at 2017/4/6
 *
 * @author yidin
 */
public class ManagerControllerTest extends BaseTest {

    @Test
    public void testHeartBeat() throws Exception {
        HttpRequest httpRequest = new HttpRequest("http://10.10.22.154:8280/agent/heartbeat");
        httpRequest.setParameter("host", "localhost");
        httpRequest.setParameter("port", "8888");
        httpRequest.setParameter("status", "[]");

        String response = httpRequest.requestPost();
        System.out.println(response);
    }

    @Test
    public void testGetProjectServer() throws Exception {
        getProjectServer("430100-62fb00af4f04407e9e4383aa7cd4fdf0", false);
//        getProjectServer("1111111111", true);
    }

    private void getProjectServer(String projectId, boolean autoAssign) throws IOException {
        HttpRequest httpRequest = new HttpRequest("http://10.10.22.154:8280/getProjectServer")
                .setParameter("projectId", projectId)
                .setParameter("autoAssign", String.valueOf(autoAssign));

        String post = httpRequest.requestPost();
        System.out.println(post);
        JSONObject json = JSON.parseObject(post);
        JSONObject data = json.getJSONObject("data");
        System.out.println("url :" + data.getString("host") + ":" + data.getString("port"));
    }
}