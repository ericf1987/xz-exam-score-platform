package com.xz.scorep.manager;

import com.hyd.simplecache.EhCacheConfiguration;
import com.hyd.simplecache.SimpleCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class ScorePlatformManagerApplication {

    public static void main(String[] args) {
        new SpringApplication(ScorePlatformManagerApplication.class).run(args);
    }

    @Bean
    public SimpleCache simpleCache() {
        EhCacheConfiguration conf = new EhCacheConfiguration();
        conf.setName("global");
        return new SimpleCache(conf);
    }
}
