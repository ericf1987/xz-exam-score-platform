package com.xz.scorep.executor.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class GenerateDataController {

    @PostMapping("/generate_data")
    @ResponseBody
    public String generateData(HttpServletRequest request) {
        return "数据已经生成";
    }
}
