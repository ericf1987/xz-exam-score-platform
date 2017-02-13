package com.xz.scorep.executor.importproject;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.appauth.AppAuthClient;
import com.xz.ajiaedu.common.json.JSONUtils;
import com.xz.ajiaedu.common.lang.Context;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.bean.ExamProject;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.project.*;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigParser;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ImportProjectService {

    @Autowired
    private AppAuthClient appAuthClient;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private ClassService classService;

    @Autowired
    private StudentService studentService;

    @Autowired
    QuestService questService;

    @Autowired
    private ReportConfigService reportConfigService;

    public void importProject(ImportProjectParameters parameters) {
        Context context = new Context();
        context.put("projectId", parameters.getProjectId());

        // 初始化数据库
        projectService.initProjectDatabase(parameters.getProjectId());

        // 导入项目数据
        if (parameters.isImportProjectInfo()) {
            importProjectInfo(context);
        }

        if (parameters.isImportReportConfig()) {
            importReportConfig(context);
        }

        if (parameters.isImportStudents()) {
            importStudents(context);
        }

        if (parameters.isImportQuests()) {
            importQuests(context);
        }
    }

    private void importProjectInfo(Context context) {
        String projectId = context.get("projectId");
        Result result = appAuthClient.callApi("QueryProjectById",
                new Param().setParameter("projectId", projectId));

        ExamProject project = projectResultParser.parse(context, result);
        context.put("project", project);
        projectService.saveProject(project);
    }

    private void importReportConfig(Context context) {
        Result result = appAuthClient.callApi("QueryProjectReportConfig",
                new Param().setParameter("projectId", context.getString("projectId")));

        ReportConfig reportConfig = reportConfigParser.parse(context, result);
        reportConfigService.saveReportConfig(reportConfig);
    }

    private void importStudents(Context context) {
        ImportStudentHelper.importStudentList(
                appAuthClient, context, schoolService, classService, studentService);
    }

    private void importQuests(Context context) {
        String projectId = context.get("projectId");
        Result result = appAuthClient.callApi("QueryQuestionByProject",
                new Param().setParameter("projectId", projectId));

        questService.clearQuests(projectId);

        JSONArray quests = result.get("quests");
        JSONUtils.<JSONObject>forEach(quests, quest -> {
            ExamQuest examQuest = new ExamQuest(quest);
            questService.saveQuest(projectId, examQuest);
        });
    }

    //////////////////////////////////////////////////////////////

    @FunctionalInterface
    public interface ResultParser<T> {

        T parse(Context context, Result result);
    }

    //////////////////////////////////////////////////////////////

    private ResultParser<ExamProject> projectResultParser = (context, result) -> {
        JSONObject obj = result.get("result");
        ExamProject project = new ExamProject();
        project.setId(context.getString("projectId"));
        project.setName(obj.getString("name"));
        project.setGrade(obj.getInteger("grade"));
        project.setCreateTime(new Date());
        return project;
    };

    // 解析报表配置的逻辑比较复杂，提取到单独的类当中，而不是内嵌类
    private ReportConfigParser reportConfigParser = new ReportConfigParser();
}
