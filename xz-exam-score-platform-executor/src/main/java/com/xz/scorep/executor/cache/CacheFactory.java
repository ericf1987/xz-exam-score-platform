package com.xz.scorep.executor.cache;

import com.hyd.simplecache.EhCacheConfiguration;
import com.hyd.simplecache.SimpleCache;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

@Component
public class CacheFactory {

    public static final int ONE_HOUR = 3600000;

    // 项目常用信息缓存
    private final Map<String, SimpleCacheWrapper> projectCache = new HashMap<>();

    // 生成 Excel 期间使用的缓存，完毕后应删除
    private final Map<String, SimpleCacheWrapper> reportCache = new HashMap<>();

    private SimpleCacheWrapper globalCache;

    @PostConstruct
    private void initCacheFactory() {
        globalCache = createCache("global");
    }

    public SimpleCache getGlobalCache() {
        return globalCache.getSimpleCache();
    }

    public synchronized SimpleCache getReportCache(String projectId) {
        shrink(reportCache);

        SimpleCacheWrapper result = reportCache.get(projectId);
        if (result != null) {
            return result.getSimpleCache();
        } else {
            Function<String, SimpleCacheWrapper> creator = k -> createCache("report:" + projectId);
            return reportCache.computeIfAbsent(projectId, creator).getSimpleCache();
        }
    }

    public synchronized SimpleCache getProjectCache(String projectId) {
        shrink(projectCache);

        SimpleCacheWrapper result = projectCache.get(projectId);
        if (result != null) {
            return result.getSimpleCache();
        } else {
            Function<String, SimpleCacheWrapper> creator = k -> createCache("project:" + projectId);
            return projectCache.computeIfAbsent(projectId, creator).getSimpleCache();
        }
    }

    public synchronized void removeReportCache(String projectId) {
        SimpleCacheWrapper cacheWrapper = reportCache.get(projectId);
        if (cacheWrapper != null) {
            cacheWrapper.close();
            reportCache.remove(projectId);
        }
    }

    private SimpleCacheWrapper createCache(String cacheName) {
        EhCacheConfiguration conf = new EhCacheConfiguration();
        conf.setName(cacheName);
        conf.setMaxEntriesLocalHeap(100000);
        conf.setTimeToLiveSeconds(3600);
        conf.setTimeToIdleSeconds(3600);
        return new SimpleCacheWrapper(new SimpleCache(conf));
    }

    @PreDestroy
    private void close() {
        this.globalCache.close();
        this.projectCache.values().forEach(SimpleCacheWrapper::close);
        this.reportCache.values().forEach(SimpleCacheWrapper::close);
    }

    //////////////////////////////////////////////////////////////

    private static class SimpleCacheWrapper {

        private SimpleCache simpleCache;

        private long lastAccess = System.currentTimeMillis();

        public SimpleCacheWrapper(SimpleCache simpleCache) {
            this.simpleCache = simpleCache;
        }

        public SimpleCache getSimpleCache() {
            lastAccess = System.currentTimeMillis();
            return simpleCache;
        }

        public boolean expired() {
            return System.currentTimeMillis() - lastAccess > ONE_HOUR;
        }

        public void close() {
            if (this.simpleCache != null) {
                this.simpleCache.close();
            }
        }

    }

    //////////////////////////////////////////////////////////////

    private void shrink(Map<String, SimpleCacheWrapper> cacheMap) {
        Iterator<Map.Entry<String, SimpleCacheWrapper>> iterator = cacheMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SimpleCacheWrapper> entry = iterator.next();
            if (entry.getValue().expired()) {
                entry.getValue().getSimpleCache().close();
                iterator.remove();
            }
        }
    }
}
