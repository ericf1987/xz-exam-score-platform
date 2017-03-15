package com.xz.scorep.executor;

import com.xz.ajiaedu.common.aliyun.OSSFileClient;
import com.xz.ajiaedu.common.aliyun.OSSTempCridentialKeeper2;
import com.xz.ajiaedu.common.appauth.AppAuthClient;
import com.xz.scorep.executor.config.AppAuthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class ScorePlatformExecutorApplication {

    public static void main(String[] args) {
        new SpringApplication(ScorePlatformExecutorApplication.class).run(args);
    }

    @Autowired
    private AppAuthConfig appAuthConfig;

    @Bean
    public AppAuthClient appAuthClient() {
        return new AppAuthClient(
                appAuthConfig.getUrl(),
                appAuthConfig.getAppKey(),
                appAuthConfig.getAppSecret()
        );
    }

    @Bean
    public OSSFileClient ossFileClient() {
        return new OSSFileClient(
                new OSSTempCridentialKeeper2(appAuthClient(), "znxunzhi-ajiaedu-update"));
    }
}
