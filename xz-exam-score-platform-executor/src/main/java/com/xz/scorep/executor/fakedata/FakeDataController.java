package com.xz.scorep.executor.fakedata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class FakeDataController {

    @Autowired
    private FakeDataGenerateService fakeDataGenerateService;

    @PostMapping("/fake_data/generate")
    @ResponseBody
    public String generateData(HttpServletRequest request) {

        String projectId = request.getParameter("projectId");
        int schoolPerProject = getInt(request, "schoolPerProject");
        int classPerSchool = getInt(request, "classPerSchool");
        int studentPerClass = getInt(request, "studentPerClass");
        int subjectPerProject = getInt(request, "subjectPerProject");
        int questPerSubject = getInt(request, "questPerSubject");
        int scorePerQuest = getInt(request, "scorePerQuest");

        FakeDataParameter fakeDataParameter = new FakeDataParameter(
                projectId, schoolPerProject, classPerSchool, studentPerClass,
                subjectPerProject, questPerSubject, scorePerQuest);

        fakeDataGenerateService.generateFakeDataAsync(fakeDataParameter);
        return "项目 " + projectId + " 的生成模拟数据已经开始。";
    }

    private int getInt(HttpServletRequest request, String schoolPerProject) {
        return Integer.parseInt(request.getParameter(schoolPerProject));
    }
}
