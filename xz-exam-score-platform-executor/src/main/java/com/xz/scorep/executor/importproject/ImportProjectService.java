package com.xz.scorep.executor.importproject;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hyd.dao.DAO;
import com.mongodb.MongoClient;
import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.appauth.AppAuthClient;
import com.xz.ajiaedu.common.json.JSONUtils;
import com.xz.ajiaedu.common.lang.Context;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.bean.ExamProject;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.mongo.MongoClientFactory;
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

    public static final String PROJECT_ID_KEY = "projectId";

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
    private SubjectService subjectService;

    @Autowired
    private QuestService questService;

    @Autowired
    private ReportConfigService reportConfigService;

    @Autowired
    private MongoClientFactory mongoClientFactory;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private DAOFactory daoFactory;

    public void importProject(ImportProjectParameters parameters) {

        String projectId = parameters.getProjectId();
        Context context = new Context();
        context.put(PROJECT_ID_KEY, projectId);

        // 初始化数据库
        if (parameters.isRecreateDatabase()) {
            LOG.info("重新创建项目 {} 的数据库...", projectId);
            projectService.initProjectDatabase(projectId);
        }

        // 导入项目数据
        if (parameters.isImportProjectInfo()) {
            LOG.info("导入项目 {} 基本信息...", projectId);
            importProjectInfo(context);
            importSubjects(context);
        }

        if (parameters.isImportReportConfig()) {
            LOG.info("导入项目 {} 报表配置...", projectId);
            importReportConfig(context);
        }

        if (parameters.isImportStudents()) {
            LOG.info("导入项目 {} 考生信息...", projectId);
            importStudents(context);
        }

        if (parameters.isImportQuests()) {
            LOG.info("导入项目 {} 题目信息...", projectId);
            importQuests(context);
        } else {
            context.put("questList", questService.queryQuests(projectId));
        }

        LOG.info("导入项目 {} 阅卷分数...", projectId);
        importScore(context);

        LOG.info("导入项目 {} 完成。", projectId);
    }

    private void importScore(Context context) {

        String projectId = context.get(PROJECT_ID_KEY);
        scoreService.clearScores(projectId);

        MongoClient mongoClient = mongoClientFactory.getProjectMongoClient(projectId);
        if (mongoClient == null) {
            throw new IllegalArgumentException("项目 " + projectId + " 在网阅数据库中不存在");
        }

        DAO projectDao = daoFactory.getProjectDao(projectId);
        ImportScoreHelper helper = new ImportScoreHelper(context, mongoClient, projectDao);
        helper.importScore();
    }

    private void importSubjects(Context context) {
        String projectId = context.getString(PROJECT_ID_KEY);
        Result result = appAuthClient.callApi("QuerySubjectListByProjectId",
                new Param().setParameter("projectId", projectId));

        JSONArray subjects = result.get("result");
        JSONUtils.<JSONObject>forEach(subjects, subjectDoc -> {
            ExamSubject subject = new ExamSubject(subjectDoc);
            subjectService.saveSubject(projectId, subject);
        });
    }

    private void importProjectInfo(Context context) {
        String projectId = context.get(PROJECT_ID_KEY);
        Result result = appAuthClient.callApi("QueryProjectById",
                new Param().setParameter(PROJECT_ID_KEY, projectId));

        ExamProject project = projectResultParser.parse(context, result);
        context.put("project", project);
        projectService.saveProject(project);
    }

    private void importReportConfig(Context context) {
        Result result = appAuthClient.callApi("QueryProjectReportConfig",
                new Param().setParameter("projectId", context.getString(PROJECT_ID_KEY)));

        ReportConfig reportConfig = reportConfigParser.parse(context, result);

        if (reportConfig != null) {
            reportConfigService.saveReportConfig(reportConfig);
        }
    }

    private void importStudents(Context context) {

        ImportStudentHelper helper = new ImportStudentHelper(
                appAuthClient, schoolService, classService, studentService);

        helper.importStudentList(context);
    }

    private void importQuests(Context context) {
        String projectId = context.get(PROJECT_ID_KEY);
        Result result = appAuthClient.callApi("QueryQuestionByProject",
                new Param().setParameter(PROJECT_ID_KEY, projectId));

        questService.clearQuests(projectId);

        JSONArray quests = result.get("quests");
        List<ExamQuest>  questList = new ArrayList<>();

        JSONUtils.<JSONObject>forEach(quests, quest -> {
            ExamQuest examQuest = new ExamQuest(quest);
            questList.add(examQuest);
            scoreService.createQuestScoreTable(projectId, examQuest);
        });

        context.put("questList", questList);
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
        project.setId(context.getString(PROJECT_ID_KEY));
        project.setName(obj.getString("name"));
        project.setGrade(obj.getInteger("grade"));
        project.setCreateTime(new Date());
        return project;
    };

    // 解析报表配置的逻辑比较复杂，提取到单独的类当中，而不是内嵌类
    private ReportConfigParser reportConfigParser = new ReportConfigParser();
}
