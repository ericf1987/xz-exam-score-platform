package com.xz.scorep.executor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mongo")
public class MongoConfig {

    private String scannerDbs;

    private String aggrDbs;

    public String getAggrDbs() {
        return aggrDbs;
    }

    public void setAggrDbs(String aggrDbs) {
        this.aggrDbs = aggrDbs;
    }

    public String getScannerDbs() {
        return scannerDbs;
    }

    public void setScannerDbs(String scannerDbs) {
        this.scannerDbs = scannerDbs;
    }
}
