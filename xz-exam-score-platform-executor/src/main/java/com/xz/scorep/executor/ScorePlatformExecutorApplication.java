package com.xz.scorep.executor;

import com.xz.ajiaedu.common.appauth.AppAuthClient;
import com.xz.scorep.executor.config.AppAuthConfig;
import com.xz.scorep.executor.db.DbiHandleFactory;
import com.xz.scorep.executor.db.DbiHandleFactoryManager;
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

    @Autowired
    private DbiHandleFactoryManager dbiHandleFactoryManager;

    @Bean
    public AppAuthClient appAuthClient() {
        return new AppAuthClient(
                appAuthConfig.getUrl(),
                appAuthConfig.getAppKey(),
                appAuthConfig.getAppSecret()
        );
    }

    @Bean
    public DbiHandleFactory dbiHandleFactory() {
        return dbiHandleFactoryManager.getDefaultDbiHandleFactory();
    }
}
