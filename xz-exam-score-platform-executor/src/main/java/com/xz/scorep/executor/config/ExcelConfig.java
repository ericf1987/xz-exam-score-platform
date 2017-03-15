package com.xz.scorep.executor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "excel")
public class ExcelConfig {

    private int poolSize;

    private String savePath;

    private String archiveUrlPrefix;

    public String getArchiveUrlPrefix() {
        return archiveUrlPrefix;
    }

    public void setArchiveUrlPrefix(String archiveUrlPrefix) {
        this.archiveUrlPrefix = archiveUrlPrefix;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }
}
