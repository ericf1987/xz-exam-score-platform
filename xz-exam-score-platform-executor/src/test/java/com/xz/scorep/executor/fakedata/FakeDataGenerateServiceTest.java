package com.xz.scorep.executor.fakedata;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 生成测试数据
 *
 * @author yidin
 */
public class FakeDataGenerateServiceTest extends BaseTest {

    @Autowired
    private FakeDataGenerateService fakeDataGenerateService;

    @Test
    public void generateFakeData() throws Exception {
        fakeDataGenerateService.generateFakeData(new FakeDataParameter(
                "project2", 5,10,40,
                3, 20, 5
        ));
    }

}