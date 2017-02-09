package com.xz.scorep.executor.aggregate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AggregateService {

    private static final Logger LOG = LoggerFactory.getLogger(AggregateService.class);

    private Map<String, Aggregator> aggregatorMap = new HashMap<>();

    public void registerAggregator(String aggrName, Aggregator aggregator) {
        this.aggregatorMap.put(aggrName, aggregator);
    }

    public void runAggregate(String projectId, String aggrName) {
        Aggregator aggregator = this.aggregatorMap.get(aggrName);

        try {
            if (aggregator != null) {
                LOG.info("项目 " + projectId + " 的 " + aggrName + " 统计开始...");
                aggregator.aggregate(projectId);
                LOG.info("项目 " + projectId + " 的 " + aggrName + " 统计完毕。");
            } else {
                LOG.info("找不到统计项 " + aggrName + " 的统计类");
            }
        } catch (Exception e) {
            LOG.error("项目 " + projectId + " 的 " + aggrName + " 统计失败");
        }
    }

    public void runAllAggregate(String projectId) {
        // todo implement this
    }
}
