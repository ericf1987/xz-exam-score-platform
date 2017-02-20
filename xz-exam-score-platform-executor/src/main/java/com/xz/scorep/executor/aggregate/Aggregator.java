package com.xz.scorep.executor.aggregate;

import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * (description)
 * created at 2017/2/8
 *
 * @author yidin
 */
public abstract class Aggregator {

    @Autowired
    private AggregateService aggregateService;

    @Autowired
    protected DAOFactory daoFactory;

    @PostConstruct
    public void aggregatorInitialization() {
        this.aggregateService.registerAggregator(this);
    }

    public abstract void aggregate(String projectId) throws Exception;

    public String getAggrName() {
        String className = this.getClass().getSimpleName();
        return className.substring(0, className.length() - "Aggregator".length());
    }

    public int getAggregateOrder() {
        if (!this.getClass().isAnnotationPresent(AggragateOrder.class)) {
            return 0;
        } else {
            return this.getClass().getAnnotation(AggragateOrder.class).value();
        }
    }
}
