package com.xz.scorep.executor.fakedata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class FakeDataController {

    @Autowired
    FakeDataGenerateService fakeDataGenerateService;

    @PostMapping("/generate_data")
    @ResponseBody
    public String generateData(HttpServletRequest request) {
        FakeDataParameter fakeDataParameter = new FakeDataParameter(
                "FAKE_PROJECT", 3, 5, 20, 3, 50, 3
        );
        fakeDataGenerateService.generateFakeData(fakeDataParameter);
        return "数据已经生成";
    }
}
