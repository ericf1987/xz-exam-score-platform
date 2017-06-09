package com.xz.scorep.executor.scorecheck;

import com.xz.ajiaedu.common.lang.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 监控平台低分检查
 *
 * @author luckylo
 * @createTime 2017-06-09.
 */
@Controller
public class LowerScoreCheckController {

    private static final Logger LOG = LoggerFactory.getLogger(LowerScoreCheckController.class);

    @ResponseBody
    @GetMapping("check/{projectId}/{subjectId}/{score}")
    public Result lowerScoreCheck(@PathVariable("projectId") String projectId,
                                  @PathVariable("subjectId") String subjectId,
                                  @PathVariable("score") double score) {
        // TODO: 2017-06-09 todo something
        return Result.success();
    }
}
