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
    protected AggregateService aggregateService;

    @Autowired
    protected DAOFactory daoFactory;

    @PostConstruct
    public void aggregatorInitialization() {
        String className = this.getClass().getSimpleName();
        String aggrName = className.substring(0, className.length() - "Aggregator".length());
        this.aggregateService.registerAggregator(aggrName, this);
    }

    public abstract void aggregate(String projectId) throws Exception;
}
