package com.xz.scorep.executor.scorecheck;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.ajiaedu.common.lang.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

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

    @ResponseBody
    @GetMapping("check/{projectId}/{subjectId}/{score}")
    public Result lowerScoreCheck(@PathVariable("projectId") String projectId,
                                  @PathVariable("subjectId") String subjectId,
                                  @PathVariable("score") double score) {
        return check(projectId, subjectId, score);
    }


    @ResponseBody
    @GetMapping("check/{projectId}/{score}")
    public Result lowerScoreCheck(@PathVariable("projectId") String projectId,
                                  @PathVariable("score") double score) {
        return check(projectId, null, score);
    }

    private Result check(String projectId, String subjectId, double score) {

        if (StringUtil.isEmpty(subjectId)) {
            List<Row> rows = scoreService.queryProjectLowerScoreStudent(projectId, score);
            return Result.success().set("students", rows);
        } else {
            List<Row> rows = scoreService.querySubjectLowerScoreStudent(projectId, subjectId, score);
            return Result.success().set("students", rows);
        }

    }
}
