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

    /**
     * 执行统计
     *
     * @param projectId 项目ID
     * @param aggrName  统计名称（可选，不为空时仅执行该项统计）
     * @param aggrType  统计类型（可选，当 aggrName 不为空时将被忽略）
     * @param async     是否异步统计
     *
     * @return 统计结果
     */
    @PostMapping("/aggr/start")
    @ResponseBody
    public String runAggregate(
            @RequestParam("projectId") String projectId,
            @RequestParam(required = false, name = "aggrName") String aggrName,
            @RequestParam(required = false, name = "Basic", defaultValue = "Basic") String aggrType,
            @RequestParam(required = false, name = "async", defaultValue = "false") boolean async
    ) {
        if (StringUtil.isNotBlank(aggrName)) {
            aggregateService.runAggregate(projectId, aggrName);
        } else {

            AggregateType aggregateType = AggregateType.valueOf(aggrType);

            if (async) {
                aggregateService.runAggregateAsync(projectId, aggregateType);
            } else {
                aggregateService.runAggregate(projectId, aggregateType);
            }
        }
        return "统计执行完毕。";
    }
}
