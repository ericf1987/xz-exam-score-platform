package com.xz.scorep.executor.aggregate;

import com.xz.scorep.executor.utils.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AggregateService {

    private static final Logger LOG = LoggerFactory.getLogger(AggregateService.class);

    private Map<String, Aggregator> aggregatorMap = new HashMap<>();

    /**
     * 注册 Aggregator 实例
     *
     * @param aggregator Aggregator 实例
     */
    void registerAggregator(Aggregator aggregator) {
        this.aggregatorMap.put(aggregator.getAggrName(), aggregator);
    }

    /**
     * 执行一个特定的统计项
     *
     * @param projectId 项目ID
     * @param aggrName  统计项名称，即 Aggregator 类的前缀
     */
    public void runAggregate(String projectId, String aggrName) {
        Aggregator aggregator = this.aggregatorMap.get(aggrName);
        runAggregate(projectId, aggregator);
    }

    /**
     * 执行所有的统计项
     *
     * @param projectId 项目ID
     */
    public void runAggregate(String projectId) {
        Stopwatch stopwatch = Stopwatch.start();

        List<Aggregator> aggregators = new ArrayList<>(this.aggregatorMap.values());
        aggregators.sort(Comparator.comparingInt(Aggregator::getAggregateOrder));
        aggregators.forEach(aggregator -> runAggregate(projectId, aggregator));

        LOG.info(stopwatch.stop("项目 " + projectId + " 统计完成，耗时 {0} ms"));
    }

    private void runAggregate(String projectId, Aggregator aggregator) {
        if (aggregator != null) {
            String aggrName = aggregator.getAggrName();
            try {
                LOG.info("项目 " + projectId + " 的 " + aggrName + " 统计开始...");
                aggregator.aggregate(projectId);
                LOG.info("项目 " + projectId + " 的 " + aggrName + " 统计完毕。");
            } catch (Exception e) {
                LOG.error("项目 " + projectId + " 的 " + aggrName + " 统计失败");
            }
        }
    }
}
