package com.xz.scorep.executor.pss.controller;

import com.xz.scorep.executor.pss.service.PssService;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author by fengye on 2017/5/24.
 */

@Controller
public class PssController {
    @Autowired
    PssService pssService;

    @PostMapping("/img/showImg")
    @ResponseBody
    public ModelAndView showImg(
            @RequestParam("projectId") String projectId,
            @RequestParam("subjectId") String subjectId,
            @RequestParam("studentId") String studentId,
            @RequestParam("isPositive") String isPositive
    ){
        String imgString = pssService.getOneStudentOnePage(projectId, subjectId, studentId,
                BooleanUtils.toBoolean(isPositive), null);
        return new ModelAndView("img").addObject("imgString", imgString);
    }
}
