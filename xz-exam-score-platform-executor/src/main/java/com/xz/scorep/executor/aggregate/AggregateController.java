package com.xz.scorep.executor.aggregate;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.ajiaedu.common.lang.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Controller
public class AggregateController {

    @Autowired
    private AggregateService aggregateService;

    /**
     * 执行统计
     *
     * @param projectId     项目ID
     * @param aggrName      统计名称（可选，不为空时仅执行该项统计）
     * @param aggrType      统计类型（可选，当 aggrName 不为空时将被忽略）
     * @param async         是否异步统计
     * @param importProject 是否（重新）导入考试信息，当项目不存在时强制重新导入
     * @param importScore   是否（重新）导入考试成绩，当项目不存在时强制重新导入
     * @param subjects      科目列表，科目之间逗号隔开（可选）
     * @return 统计结果
     */
    @PostMapping("/aggr/start")
    @ResponseBody
    public Result runAggregate(
            @RequestParam("projectId") String projectId,
            @RequestParam(required = false, name = "aggrName") String aggrName,
            @RequestParam(required = false, name = "aggrType", defaultValue = "Basic") String aggrType,
            @RequestParam(required = false, name = "async", defaultValue = "false") boolean async,
            @RequestParam(required = false, name = "importProject", defaultValue = "false") boolean importProject,
            @RequestParam(required = false, name = "importScore", defaultValue = "false") boolean importScore,
            @RequestParam(required = false, name = "subjects", defaultValue = "") String subjects
    ) {

        if (StringUtil.isNotBlank(aggrName)) {
            aggregateService.runAggregate(projectId, aggrName);  // 调试用
            return Result.success("统计执行完毕。");

        } else {

            AggregateType aggregateType = AggregateType.valueOf(aggrType);

            AggregateParameter parameter = new AggregateParameter();
            parameter.setProjectId(projectId);
            parameter.setAggregateType(aggregateType);
            parameter.setAggrName(aggrName);
            parameter.setImportScore(importScore);
            parameter.setImportProject(importProject);

            if (subjects != null) {
                parameter.setSubjects(Arrays.asList(subjects.split(",")));
            }

            if (async) {
                aggregateService.runAggregateAsync(parameter);
                return Result.success("统计已经开始执行。");
            } else {
                aggregateService.runAggregate(parameter);
                return Result.success("统计执行完毕。");
            }
        }
    }

    @GetMapping("/aggr/status/{projectId}/{subjectId}")
    @ResponseBody
    public Result getAggregationStatus(
            @PathVariable("projectId") String projectId,
            @PathVariable("subjectId") String subjectId) {
        Row row = aggregateService.getAggregationStatus(projectId, subjectId);
        return Result.success().set("status", row);
    }


    @GetMapping("/aggr/status/{projectId}")
    @ResponseBody
    public Result getAggregationStatus(
            @PathVariable("projectId") String projectId) {
        return getAggregationStatus(projectId, null);
    }
}
