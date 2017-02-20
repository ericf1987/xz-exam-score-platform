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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ImportProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ImportProjectService.class);

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
    private QuestService questService;

    @Autowired
    private ReportConfigService reportConfigService;

    public void importProject(ImportProjectParameters parameters) {
        Context context = new Context();
        context.put("projectId", parameters.getProjectId());

        // 初始化数据库
        if (parameters.isRecreateDatabase()) {
            LOG.info("重新创建项目 {} 的数据库...", parameters.getProjectId());
            projectService.initProjectDatabase(parameters.getProjectId());
        }

        // 导入项目数据
        if (parameters.isImportProjectInfo()) {
            LOG.info("导入项目 {} 基本信息...", parameters.getProjectId());
            importProjectInfo(context);
        }

        if (parameters.isImportReportConfig()) {
            LOG.info("导入项目 {} 报表配置...", parameters.getProjectId());
            importReportConfig(context);
        }

        if (parameters.isImportStudents()) {
            LOG.info("导入项目 {} 考生信息...", parameters.getProjectId());
            importStudents(context);
        }

        if (parameters.isImportQuests()) {
            LOG.info("导入项目 {} 题目信息...", parameters.getProjectId());
            importQuests(context);
        }

        LOG.info("导入项目 {} 阅卷分数...", parameters.getProjectId());
        importScore(context);

        LOG.info("导入项目 {} 完成。", parameters.getProjectId());
    }

    private void importScore(Context context) {

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

        if (reportConfig != null) {
            reportConfigService.saveReportConfig(reportConfig);
        }
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
        List<ExamQuest>  questList = new ArrayList<>();
        JSONUtils.<JSONObject>forEach(quests, quest -> {
            ExamQuest examQuest = new ExamQuest(quest);
            questList.add(examQuest);
        });

        questService.saveQuest(projectId, questList);
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
