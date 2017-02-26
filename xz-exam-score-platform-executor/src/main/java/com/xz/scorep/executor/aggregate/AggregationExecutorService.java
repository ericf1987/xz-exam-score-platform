package com.xz.scorep.executor.aggregate;

import com.xz.ajiaedu.common.concurrent.Executors;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;

@Service
public class AggregationExecutorService {

    private ExecutorService executorService;

    @PostConstruct
    private void init() {
        executorService = Executors.newBlockingThreadPoolExecutor(5, 5, 1);
    }

    @PreDestroy
    private void shutdown() {
        this.executorService.shutdownNow();
    }

    public void submit(Runnable aggrTask) {
        this.executorService.submit(aggrTask);
    }
}
