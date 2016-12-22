package com.xz.scorep.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class ScorePlatformManagerApplication {

    public static void main(String[] args) {
        new SpringApplication(ScorePlatformManagerApplication.class).run(args);
    }
}
