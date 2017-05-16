package com.xz.scorep.executor.importproject;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.aliyun.Api;
import com.xz.ajiaedu.common.appauth.AppAuthClient;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author by fengye on 2017/5/2.
 */
public class InterfaceClientTest extends BaseTest {
    @Autowired
    AppAuthClient appAuthClient;

    public static final String PROJECT_ID = "430100-b05a111c72c740f4898660a057c48e28";

    public static final Param param = new Param().setParameter("projectId", PROJECT_ID);

    @Test
    public void testQueryProjectById() throws Exception {
        Result result = appAuthClient.callApi(Api.ApiName.QueryProjectById.name(), param);
        Map<String, Object> data = result.getData();
        System.out.println(data.toString());
    }

    @Test
    public void testQueryProjectReportConfig() throws Exception {
        Result result = appAuthClient.callApi(Api.ApiName.QueryProjectReportConfig.name(), param);
        System.out.println(result.getData());
    }

    @Test
    public void testQueryPaperScreenConfig() throws Exception {
        Result result = appAuthClient.callApi("QueryProjectConfig",
                param.setParameter("settingKey", "paperBuild"));
        System.out.println(result.getData());
    }

    @Test
    public void testQuerySubjectListByProjectId() throws Exception {
        Result result = appAuthClient.callApi(Api.ApiName.QuerySubjectListByProjectId.name(), param);
        System.out.println(result.getData());
    }

    @Test
    public void testQueryQuestionByProject() throws Exception {
        Result result = appAuthClient.callApi(Api.ApiName.QueryQuestionByProject.name(), param);
        System.out.println(result.getData());
    }
}
