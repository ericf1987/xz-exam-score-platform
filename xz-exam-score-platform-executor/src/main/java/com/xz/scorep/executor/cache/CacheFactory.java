package com.xz.scorep.executor.cache;

import com.hyd.simplecache.EhCacheConfiguration;
import com.hyd.simplecache.SimpleCache;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

@Component
public class CacheFactory {

    private final Map<String, SimpleCache> cacheMap = new HashMap<>();

    private SimpleCache globalCache;

    @PostConstruct
    private void initCacheFactory() {
        globalCache = createCache("global");
    }

    public SimpleCache getGlobalCache() {
        return globalCache;
    }

    public SimpleCache getProjectCache(String projectId) {
        SimpleCache result = cacheMap.get(projectId);
        if (result != null) {
            return result;
        } else {
            synchronized (cacheMap) {
                return cacheMap.computeIfAbsent(projectId, k -> createProjectCache(projectId));
            }
        }
    }

    private SimpleCache createProjectCache(String projectId) {
        return createCache("project:" + projectId);
    }

    private SimpleCache createCache(String cacheName) {
        EhCacheConfiguration conf = new EhCacheConfiguration();
        conf.setName(cacheName);
        conf.setMaxEntriesLocalHeap(100000);
        conf.setTimeToLiveSeconds(3600);
        conf.setTimeToIdleSeconds(3600);
        return new SimpleCache(conf);
    }

    @PreDestroy
    private void close() {
        this.globalCache.close();
        this.cacheMap.values().forEach(SimpleCache::close);
    }
}
