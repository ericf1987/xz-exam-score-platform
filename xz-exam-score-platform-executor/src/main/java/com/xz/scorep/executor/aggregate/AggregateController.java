package com.xz.scorep.executor.aggregate;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.bean.ExamProject;
import com.xz.scorep.executor.project.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class AggregateController {

    @Autowired
    private AggregateService aggregateService;

    @Autowired
    private AggregationService aggregationService;

    @Autowired
    private ProjectService projectService;
    private static final Logger LOG = LoggerFactory.getLogger(AggregateController.class);

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
        LOG.info("projectId={},aggrType={},async={},importProject={},importScore={},subjects={}",
                projectId, aggrType, async, importProject, importScore, subjects);

        if (StringUtil.isNotBlank(aggrName)) {
            aggregateService.runAggregate(projectId, aggrName);  // 调试用
            return Result.success("统计执行完毕。");

        } else {

            ExamProject project = projectService.findProject(projectId);
            if (project == null) {
                importProject = true;
                importScore = true;
            }

            AggregateType aggregateType = AggregateType.valueOf(aggrType);

            AggregateParameter parameter = new AggregateParameter();
            parameter.setProjectId(projectId);
            parameter.setAggregateType(aggregateType);
            parameter.setAggrName(aggrName);
            parameter.setImportScore(importScore);
            parameter.setImportProject(importProject);

            if (subjects != null) {
                List<String> list = new ArrayList<>(Arrays.asList(subjects.split(",")));
                list.removeIf(StringUtil::isEmpty);
                parameter.setSubjects(list);
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
        Row row = aggregationService.getAggregateStatus(projectId, subjectId);
        return Result.success().set("status", row);
    }


    @GetMapping("/aggr/status/{projectId}")
    @ResponseBody
    public Result getAggregationStatus(
            @PathVariable("projectId") String projectId) {
        Row row = aggregationService.getAggregateStatus(projectId);
        return Result.success().set("status", row);
    }
}
