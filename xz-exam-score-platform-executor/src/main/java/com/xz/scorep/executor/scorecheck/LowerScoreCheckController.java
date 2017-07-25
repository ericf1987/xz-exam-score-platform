package com.xz.scorep.executor.scorecheck;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.aggregate.AggregateType;
import com.xz.scorep.executor.aggregate.AggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 监控平台低分检查
 *
 * @author luckylo
 * @createTime 2017-06-09.
 */
@Controller
public class LowerScoreCheckController {

    private static final Logger LOG = LoggerFactory.getLogger(LowerScoreCheckController.class);

    @Autowired
    private LowerScoreService scoreService;


    @Autowired
    private AggregationService aggregationService;


    @ResponseBody
    @PostMapping("/check")
    public Result lowerScoreCheck(
            @RequestParam(name = "projectId") String projectId,
            @RequestParam(name = "subjectIds") String subjectIds,
            @RequestParam(name = "checkType", required = false, defaultValue = "subject") String checkType,
            @RequestParam(name = "score") double score) {

        List<String> subjectIdList = new ArrayList<>(Arrays.asList(subjectIds.split(",")));
        subjectIdList.removeIf(StringUtil::isBlank);

        LOG.info("subjectIdList .... {}", subjectIdList);
        List<String> collect = subjectIdList.stream().map(str -> "\"" + str + "\"").collect(Collectors.toList());
        String subjectId = String.join(",", collect);
        Result result = aggregationService.checkProjectStatus(projectId, subjectId, AggregateType.Check.name());
        if (!result.isSuccess()) {
            return result;
        }

        Map<String, List<Row>> rows = null;
        try {

            rows = scoreService.querySubjectLowerScoreStudent(projectId, subjectIdList, checkType, score);
        } catch (Exception e) {
            return Result.fail(1, "正在导入项目所需数据,请稍等");
        }
        return Result.success().set("students", rows);
    }

}
