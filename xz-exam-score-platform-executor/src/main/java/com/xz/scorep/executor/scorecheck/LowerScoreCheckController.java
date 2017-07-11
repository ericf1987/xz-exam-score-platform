package com.xz.scorep.executor.scorecheck;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

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
    @PostMapping("/check")
    public Result lowerScoreCheck(@RequestParam(name = "projectId") String projectId,
                                  @RequestParam(name = "subjectIds") String subjectIds,
                                  @RequestParam(name = "checkType", required = false, defaultValue = "subject") String checkType,
                                  @RequestParam(name = "score") double score) {
        Map<String, List<Row>> rows = scoreService.querySubjectLowerScoreStudent(projectId, subjectIds, checkType, score);
        return Result.success().set("students", rows);
    }

}
