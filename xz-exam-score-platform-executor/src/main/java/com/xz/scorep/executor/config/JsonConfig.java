package com.xz.scorep.executor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 导出json文件保存路径
 *
 * @author luckylo
 * @createTime 2017-07-18.
 */
@Component
@ConfigurationProperties(prefix = "json")
public class JsonConfig {

    private String dumpPath;

    public String getDumpPath() {
        return dumpPath;
    }

    public void setDumpPath(String dumpPath) {
        this.dumpPath = dumpPath;
    }
}
