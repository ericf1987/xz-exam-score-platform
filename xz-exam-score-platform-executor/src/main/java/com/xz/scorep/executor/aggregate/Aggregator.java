package com.xz.scorep.executor.aggregate;

import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Arrays;

/**
 * (description)
 * created at 2017/2/8
 *
 * @author yidin
 */
public abstract class Aggregator {

    @Autowired
    private AggregateService aggregateService;              // 用于管理统计状态和流程

    @Autowired
    protected DAOFactory daoFactory;

    @PostConstruct
    public void aggregatorInitialization() {
        this.aggregateService.registerAggregator(this);
    }

    public abstract void aggregate(AggregateParameter aggregateParameter) throws Exception;

    public String getAggrName() {
        String className = this.getClass().getSimpleName();
        return className.substring(0, className.length() - "Aggregator".length());
    }

    public int getAggregateOrder() {
        if (!this.getClass().isAnnotationPresent(AggregateOrder.class)) {
            return 0;
        } else {
            return this.getClass().getAnnotation(AggregateOrder.class).value();
        }
    }

    public boolean isOfType(AggregateType aggregateType) {
        if (!this.getClass().isAnnotationPresent(AggregateTypes.class)) {
            return false;
        } else {
            AggregateTypes types = this.getClass().getAnnotation(AggregateTypes.class);
            return Arrays.asList(types.value()).contains(aggregateType);
        }
    }
}
