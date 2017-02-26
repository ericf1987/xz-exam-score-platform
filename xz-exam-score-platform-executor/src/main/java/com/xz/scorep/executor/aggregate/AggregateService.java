package com.xz.scorep.executor.aggregate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AggregateService {

    private static final Logger LOG = LoggerFactory.getLogger(AggregateService.class);

    @Autowired
    private AggregationExecutorService executorService;

    @Autowired
    private AggregationService aggregationService;

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
        runAggregate(new Aggregation(projectId), aggregator);
    }

    /**
     * 执行所有统计项（异步）
     *
     * @param projectId 项目ID
     */
    public void runAggregateAsync(final String projectId) {
        Aggregation aggregation = new Aggregation(projectId);

        Runnable runnable = () -> {
            runAggregate(projectId);
        };

        executorService.submit(runnable);
    }

    /**
     * 执行所有的统计项
     *
     * @param projectId 项目ID
     */
    public void runAggregate(final String projectId) {

        Aggregation aggregation = new Aggregation(projectId);

        try {
            aggregation.setStatus(AggregateStatus.Running);
            aggregation.setStartTime(new Date());
            aggregationService.insertAggregation(aggregation);

            List<Aggregator> aggregators = new ArrayList<>(this.aggregatorMap.values());
            aggregators.sort(Comparator.comparingInt(Aggregator::getAggregateOrder));

            for (Aggregator aggregator : aggregators) {
                aggregator.aggregate(projectId);
            }
        } catch (Exception e) {
            LOG.error("项目 " + projectId + " 统计失败", e);
        } finally {
            aggregation.setStatus(AggregateStatus.Finished);
            aggregation.setEndTime(new Date());

            aggregationService.endAggregation(
                    aggregation.getId(), aggregation.getStatus(), aggregation.getEndTime());

            LOG.info("项目 " + projectId + " 统计完成，耗时 " + aggregation.duration() + " ms");
        }
    }

    private void runAggregate(Aggregation aggregation, Aggregator aggregator) {
        if (aggregator != null) {

            String projectId = aggregation.getProjectId();
            String aggrName = aggregator.getAggrName();

            try {
                LOG.info("项目 " + projectId + " 的 " + aggrName + " 统计开始...");
                aggregator.aggregate(projectId);
                LOG.info("项目 " + projectId + " 的 " + aggrName + " 统计完毕。");
            } catch (Exception e) {
                LOG.error("项目 " + projectId + " 的 " + aggrName + " 统计失败", e);
            }
        }
    }
}
