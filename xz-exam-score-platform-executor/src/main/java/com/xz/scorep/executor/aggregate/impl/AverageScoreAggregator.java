package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.aggregate.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 平均分统计,(个人觉得,快速报表也有平均分,建议提前)
 *
 * @author luckylo
 */
@AggregateTypes({AggregateType.Advanced, AggregateType.Advanced})
@AggragateOrder(51)
public class AverageScoreAggregator extends Aggregator {

    private static Logger LOG = LoggerFactory.getLogger(AverageScoreAggregator.class);


    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {

    }
}
