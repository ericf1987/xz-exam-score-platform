package com.xz.scorep.executor;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource("/application-test.properties")
public abstract class BaseTest {

    public static final String PROJECT_ID = "fake_project";

    public static final String SCHOOL_ID = "324e11fa_b825_4695_9ba2_ae0d6312f99d";

    static {
        setupProxy();
    }

    public static void setupProxy() {
        System.setProperty("unit_testing", "true");
        System.setProperty("socksProxyHost", "127.0.0.1");
        System.setProperty("socksProxyPort", "2346");
    }

}
