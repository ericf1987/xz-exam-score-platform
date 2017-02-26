package com.xz.scorep.executor.aggregate;

import com.xz.ajiaedu.common.lang.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AggregateController {

    @Autowired
    private AggregateService aggregateService;

    @PostMapping("/aggr/start")
    @ResponseBody
    public String runAggregate(
            @RequestParam("projectId") String projectId,
            @RequestParam(required = false, name = "aggrName") String aggrName,
            @RequestParam(required = false, name = "async", defaultValue = "false") boolean async
    ) {
        if (StringUtil.isNotBlank(aggrName)) {
            aggregateService.runAggregate(projectId, aggrName);
        } else {
            if (async) {
                aggregateService.runAggregateAsync(projectId);
            } else {
                aggregateService.runAggregate(projectId);
            }
        }
        return "统计执行完毕。";
    }
}
