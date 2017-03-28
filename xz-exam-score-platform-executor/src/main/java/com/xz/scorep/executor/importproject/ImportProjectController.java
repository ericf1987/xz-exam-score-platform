package com.xz.scorep.executor.importproject;

import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.bean.ExamProject;
import com.xz.scorep.executor.project.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImportProjectController {

    private static final Logger LOG = LoggerFactory.getLogger(ImportProjectController.class);

    @Autowired
    private ImportProjectService importProjectService;

    @Autowired
    private ProjectService projectService;

    /**
     * 导入考试信息
     *
     * @param projectId        项目ID
     * @param recreateDatabase 是否要重新创建数据库（如果是第一次导入，则会强制创建）
     * @param projectInfo      是否导入项目基本信息
     * @param reportConfig     是否导入报表配置信息
     * @param students         是否导入学生列表
     * @param quests           是否导入科目和题目列表
     * @param score            是否导入网阅分数（应该是至少在某科阅卷完成后）
     *
     * @return 操作结果
     */
    @PostMapping("/import/project")
    public Result importProject(
            @RequestParam(name = "projectId") String projectId,
            @RequestParam(required = false, defaultValue = "false", name = "recreateDatabase") boolean recreateDatabase,
            @RequestParam(required = false, defaultValue = "false", name = "projectInfo") boolean projectInfo,
            @RequestParam(required = false, defaultValue = "false", name = "reportConfig") boolean reportConfig,
            @RequestParam(required = false, defaultValue = "false", name = "students") boolean students,
            @RequestParam(required = false, defaultValue = "false", name = "quests") boolean quests,
            @RequestParam(required = false, defaultValue = "false", name = "score") boolean score
    ) {

        ExamProject project = projectService.findProject(projectId);
        if (project == null) {
            LOG.info("项目 " + projectId + " 没找到，需要重新创建。");
            recreateDatabase = true;
            projectInfo = true;
            reportConfig = true;
            students = true;
            quests = true;
        }

        importProjectService.importProject(ImportProjectParameters.importSelected(
                projectId, recreateDatabase, projectInfo, reportConfig, students, quests, score
        ));
        return Result.success("项目 " + projectId + " 考生数据导入成功。");
    }


}
