package com.xz.scorep.executor.aggregate;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AggregateController {

    @PostMapping("/aggr/start")
    public void runAggregate(String projectId) {

    }
}
