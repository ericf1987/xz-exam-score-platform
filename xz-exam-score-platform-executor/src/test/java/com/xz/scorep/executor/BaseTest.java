package com.xz.scorep.executor;

import com.xz.scorep.executor.bean.Target;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource("/application-test.properties")
public abstract class BaseTest {

    public static final Target TARGET_SUBJECT_001 = Target.subject("001", "语文");

    public static final String PROJECT_ID = "430300-9cef9f2059ce4a36a40a7a60b07c7e00";

    public static final String PROJECT2_ID = "430300-564140e278df4e92a2a739a6f27ac391";

    public static final String PROJECT3_ID = "430900-9e8f3c054d72414b81cdd99bd48da695";

    public static final String PROJECT4_ID = "430100-5d2142085fc747c9b5b230203bbfd402";

    public static final String PROJECT5_ID = "430100-4bc2ffbf50214ebc8ec34dd5166ef5b5";

    public static final String SCHOOL_ID = "002e02d6-c036-4780-85d4-e54e3f1fbf9f";

    public static final String SCHOOL_NAME = "湘潭市第十八中学";

    public static final String 湘潭20170328联考 = "430300-f39dc20ba0044b8b9cb302d7910c87c4";

    static {
        setupProxy();
    }

    public static void setupProxy() {
        System.setProperty("unit_testing", "true");
        System.setProperty("socksProxyHost", "127.0.0.1");
        System.setProperty("socksProxyPort", "2346");
    }

}
