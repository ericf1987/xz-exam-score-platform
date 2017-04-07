package com.xz.scorep.executor.exportexcel;

import com.xz.scorep.executor.cache.CacheFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReportCacheInitializer {

    @Autowired
    private CacheFactory cacheFactory;

    public void initReportCache(String projectId) {

    }
}
