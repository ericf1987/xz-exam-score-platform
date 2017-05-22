package com.xz.scorep.executor;

import com.xz.ajiaedu.common.aliyun.OSSFileClient;
import com.xz.ajiaedu.common.aliyun.OSSTempCridentialKeeper2;
import com.xz.ajiaedu.common.appauth.AppAuthClient;
import com.xz.scorep.executor.api.context.App;
import com.xz.scorep.executor.api.server.ServerConsole;
import com.xz.scorep.executor.config.AppAuthConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class ScorePlatformExecutorApplication {

    private static final Logger LOG = LoggerFactory.getLogger(ScorePlatformExecutorApplication.class);

    public static void main(String[] args) {
        String serverAddress = System.getProperty("server.address");
        String serverPort = System.getProperty("server.port");
        if (StringUtils.isBlank(serverAddress) || StringUtils.isBlank(serverPort)) {
            throw new IllegalStateException("请在运行时加上 -Dserver.address 和 -Dserver.port 两个参数");
        }

        LOG.info("==== server address ====");
        LOG.info("    http://" + serverAddress + ":" + serverPort);
        LOG.info("========================");

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

    @Bean
    public ServletContextListener servletContextListener() {
        return new ServletContextListener() {

            @Override
            public void contextInitialized(ServletContextEvent event) {
                WebApplicationContext webApplicationContext =
                        WebApplicationContextUtils.getRequiredWebApplicationContext(event.getServletContext());
                App.setApplicationContext(webApplicationContext);
                ServerConsole.start();
            }

            @Override
            public void contextDestroyed(ServletContextEvent event) {}
        };
    }
}
