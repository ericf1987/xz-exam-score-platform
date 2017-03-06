package com.xz.scorep.executor.cache;

import com.hyd.simplecache.EhCacheConfiguration;
import com.hyd.simplecache.SimpleCache;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CacheFactory {

    private Map<String, SimpleCache> cacheMap = new HashMap<>();

    public synchronized SimpleCache getProjectCache(String projectId) {
        return cacheMap.computeIfAbsent(projectId, k -> createProjectCache(projectId));
    }

    private SimpleCache createProjectCache(String projectId) {
        EhCacheConfiguration conf = new EhCacheConfiguration();
        conf.setName("project:" + projectId);
        conf.setMaxEntriesLocalHeap(100000);
        conf.setTimeToLiveSeconds(3600);
        conf.setTimeToIdleSeconds(3600);
        return new SimpleCache(conf);
    }
}
