package com.xz.scorep.executor.importproject;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hyd.dao.DAO;
import com.mongodb.MongoClient;
import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.appauth.AppAuthClient;
import com.xz.ajiaedu.common.json.JSONUtils;
import com.xz.ajiaedu.common.lang.Context;
import com.xz.ajiaedu.common.lang.DoubleValue;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.bean.ExamProject;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.ProjectStatus;
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

import java.util.*;

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
    private AbsentService absentService;

    @Autowired
    private CheatService cheatService;

    @Autowired
    private LostService lostService;

    @Autowired
    private DAOFactory daoFactory;

    public void importProject(ImportProjectParameters parameters) {

        String projectId = parameters.getProjectId();
        Context context = new Context();
        context.put(PROJECT_ID_KEY, projectId);

        projectService.updateProjectStatus(projectId, ProjectStatus.Importing);

        // 预先初始化项目记录
        if (parameters.isImportProjectInfo()) {
            LOG.info("导入项目 {} 基本信息...", projectId);
            importProjectInfo(context);
        }

        //导入报表配置
        if (parameters.isImportReportConfig()) {
            LOG.info("导入项目 {} 报表配置...", projectId);
            importReportConfig(context);
        }

        // 初始化项目数据库
        if (parameters.isRecreateDatabase()) {
            LOG.info("重新创建项目 {} 的数据库...", projectId);
            projectService.initProjectDatabase(projectId);
        }

        //先导入题目信息(根据题目分数创建虚拟机科目)
        if (parameters.isImportQuests()) {
            LOG.info("导入项目 {} 题目信息...", projectId);
            importQuests(context);
        } else {
            context.put("questList", questService.queryQuests(projectId));
        }

        // 导入项目科目数据(并根据配置是否拆分综合科目)
        if (parameters.isImportProjectInfo()) {
            LOG.info("导入项目 {} 科目信息...", projectId);
            importSubjects(context);
        }

        //获取mongo连接
        MongoClient mongoClient = mongoClientFactory.getProjectMongoClient(projectId);
        if (mongoClient == null) {
            throw new IllegalArgumentException("项目 " + projectId + " 在网阅数据库中不存在");
        }
        context.put("client", mongoClient);

        //导入考生基础信息,通过监控平台导入,出现问题时表明项目为之前项目,再通过CMS导入
        if (parameters.isImportStudents()) {
            importStudent(context);
        }

        //导入项目阅卷分数
        if (parameters.isImportScore()) {
            LOG.info("导入项目 {} 阅卷分数...", projectId);
            importScore(context);
        }

        LOG.info("导入项目 {} 完成。", projectId);
        projectService.updateProjectStatus(projectId, ProjectStatus.Ready);
    }

    private void importStudent(Context context) {
        String projectId = context.get(PROJECT_ID_KEY);
        ImportStudentHelper helper = new ImportStudentHelper(
                appAuthClient, schoolService, classService, studentService);
        try {
            LOG.info("通过监控平台导入项目 {} 考生信息...", projectId);
            helper.importStudentListFromMonitor(context);
        } catch (Exception e) {
            LOG.info("通过监控平台导入项目 {} 考生信息失败...", projectId);

            LOG.info("通过CMS接口导入项目 {} 考生信息...", projectId);
            helper.importStudentListFromCMS(context);
            LOG.info("通过CMS接口导入项目 {} 考生信息成功...", projectId);
        }
    }


    private void importScore(Context context) {

        String projectId = context.get(PROJECT_ID_KEY);
        scoreService.clearScores(projectId);

        DAO projectDao = daoFactory.getProjectDao(projectId);
        MongoClient mongoClient = context.get("client");
        ImportScoreHelper helper = new ImportScoreHelper(context, mongoClient, projectDao);

        //////////////////////////////////////////////////////////////////////////
        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
        helper.setAbsentService(absentService);
        helper.setCheatService(cheatService);
        helper.setLostService(lostService);
        helper.setStudentService(studentService);
        helper.setReportConfig(reportConfig);
        helper.importScore();
    }

    // 导入考试科目，并计算考试项目总分
    private void importSubjects(Context context) {
        String projectId = context.getString(PROJECT_ID_KEY);
        DoubleValue projectFullScore = DoubleValue.of(0);
        Result result = appAuthClient.callApi("QuerySubjectListByProjectId",
                new Param().setParameter("projectId", projectId));

        JSONArray subjects = result.get("result");
        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);

        JSONUtils.<JSONObject>forEach(subjects, subjectDoc -> {
            ExamSubject subject = new ExamSubject(subjectDoc);
            subject.setVirtualSubject("false");
            projectFullScore.add(subject.getFullScore());
            subjectService.saveSubject(projectId, subject);
            subjectService.createSubjectScoreTable(projectId, subject.getId());
        });
        projectService.updateProjectFullScore(projectId, projectFullScore.get());

        //是否要拆分科目
        if (Boolean.valueOf(reportConfig.getSeparateCategorySubjects())) {

            Map<String, JSONArray> subjectOptionalGroups = getSubjectsOptionalGroup(projectId);

            List<ExamSubject> examSubjects = subjectService.listSubjects(projectId);
            for (ExamSubject examSubject : examSubjects) {
                if (examSubject.getId().length() > 3) {
                    String examSubjectId = examSubject.getId();
                    String cardId = examSubject.getCardId();
                    LOG.info("正在拆分项目ID{},科目ID{}，科目{}", projectId, examSubjectId, examSubject.getName());

                    //////////////////////////////////////////////////////////////////////////
                    JSONArray jsonArray = subjectOptionalGroups.get(examSubjectId);
                    String[] exclude = getExcludeQuestNos(jsonArray);
                    createVirtualSubject(projectId, examSubjectId, cardId, exclude);

                }
            }
        }
    }

    /**
     * 根据综合科目创建综合科目下的子科目
     *
     * @param projectId     项目id
     * @param examSubjectId 参加考试的科目(综合科目)
     * @param cardId        cardId
     * @param exclude       综合科目下所有的选做题
     */
    private void createVirtualSubject(String projectId, String examSubjectId, String cardId, String[] exclude) {
        int size = examSubjectId.length() / 3;
        for (int i = 1; i <= size; i++) {
            String subSubjectId = examSubjectId.substring(i * size - 3, i * size);
            String subjectName = subjectService.getSubjectName(subSubjectId);

            double subSubjectScore = subjectService.getSubSubjectScore(projectId, subSubjectId, exclude);
            ExamSubject subject = new ExamSubject(subSubjectId, subjectName, subSubjectScore);
            subject.setVirtualSubject(String.valueOf(true));
            subject.setCardId(cardId);

            subjectService.saveSubject(projectId, subject);
            subjectService.createSubjectScoreTable(projectId, subject.getId());
        }
    }


    /**
     * 获取该综合科目下所有的选做题
     *
     * @param jsonArray 选做题组
     * @return 要排除的选做题列表
     */
    private String[] getExcludeQuestNos(JSONArray jsonArray) {
        List<String> excludeQuest = new ArrayList<>();

        JSONUtils.<JSONObject>forEach(jsonArray, json -> {
            String[] questNos = json.getString("quest_nos")
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .split(",");
            Integer chooseCount = json.getInteger("choose_count");
            for (int i = 0; i < questNos.length - chooseCount; i++) {
                excludeQuest.add(questNos[i]);
            }
        });

        String[] exclude = new String[excludeQuest.size()];
        return excludeQuest.toArray(exclude);
    }

    /**
     * 获取所有含有选做题的综合科目
     *
     * @param projectId 项目ID
     * @return
     */
    private Map<String, JSONArray> getSubjectsOptionalGroup(String projectId) {
        Map<String, JSONArray> subjectMap = new HashMap<>();

        JSONObject optionalGroups = appAuthClient.callApi("QueryQuestionByProject",
                new Param().setParameter(PROJECT_ID_KEY, projectId)).get("optionalGroups");

        for (Map.Entry<String, Object> entry : optionalGroups.entrySet()) {
            String entryKey = entry.getKey();
            JSONArray jsonArray = JSONArray.parseArray(entry.getValue().toString());
            if (entryKey.length() > 3 && jsonArray.size() != 0) {
                subjectMap.put(entryKey, jsonArray);
            }
        }
        return subjectMap;
    }

    private void importProjectInfo(Context context) {
        String projectId = context.get(PROJECT_ID_KEY);
        Result result = appAuthClient.callApi("QueryProjectById",
                new Param().setParameter(PROJECT_ID_KEY, projectId));

        ExamProject project = projectResultParser.parse(context, result);
        project.setStatus(ProjectStatus.Importing.name());

        context.put("project", project);
        projectService.saveProject(project);
    }

    protected void importReportConfig(Context context) {
        Result result = appAuthClient.callApi("QueryProjectReportConfig",
                new Param().setParameter("projectId", context.getString(PROJECT_ID_KEY)));

        ReportConfig reportConfig = reportConfigParser.parse(context, result);

        if (reportConfig != null) {
            reportConfigService.saveReportConfig(reportConfig);
        }
    }

    private void importQuests(Context context) {
        String projectId = context.get(PROJECT_ID_KEY);
        Result result = appAuthClient.callApi("QueryQuestionByProject",
                new Param().setParameter(PROJECT_ID_KEY, projectId));

        questService.clearQuests(projectId);

        JSONArray quests = result.get("quests");
        List<ExamQuest> questList = new ArrayList<>();

        JSONUtils.<JSONObject>forEach(quests, quest -> {
            ExamQuest examQuest = new ExamQuest(quest);

            if (examQuest.isObjective()) {
                String answer = examQuest.getAnswer();
                //如果客观题没有答案,则跳过.避免单科统计时因为暂未开考的科目没答案而不能统计
                if (answer == null) {
                    return;
                }
                examQuest.setAnswer(answer.toUpperCase());
                examQuest.setScoreRule(examQuest.getScoreRule().toUpperCase());
                examQuest.setOptions(examQuest.getOptions().toUpperCase());
            }

            questList.add(examQuest);
            scoreService.createQuestScoreTable(projectId, examQuest);
        });

        context.put("questList", questList);
        questService.saveQuest(projectId, questList);
        LOG.info("已导入 " + questList.size() + " 个题目。");
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
