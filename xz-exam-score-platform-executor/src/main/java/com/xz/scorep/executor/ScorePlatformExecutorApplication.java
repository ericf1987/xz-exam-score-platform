package com.xz.scorep.executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class ScorePlatformExecutorApplication {

    public static void main(String[] args) {
        new SpringApplication(ScorePlatformExecutorApplication.class).run(args);
    }
}
