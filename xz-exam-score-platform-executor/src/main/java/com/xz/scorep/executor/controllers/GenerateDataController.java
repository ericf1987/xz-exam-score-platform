package com.xz.scorep.executor.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class GenerateDataController {

    @PostMapping("/generate_data")
    public String generateData() {
        return "数据已经生成";
    }
}
