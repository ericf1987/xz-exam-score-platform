package com.xz.scorep.executor.importproject;

import com.xz.ajiaedu.common.lang.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImportProjectController {

    @Autowired
    private ImportProjectService importProjectService;

    @PostMapping("/import/project")
    public Result importProject(
            @RequestParam(name = "projectId") String projectId,
            @RequestParam(required = false, defaultValue = "false", name = "recreateDatabase") boolean recreateDatabase,
            @RequestParam(required = false, defaultValue = "false", name = "projectInfo") boolean projectInfo,
            @RequestParam(required = false, defaultValue = "false", name = "reportConfig") boolean reportConfig,
            @RequestParam(required = false, defaultValue = "false", name = "students") boolean students,
            @RequestParam(required = false, defaultValue = "false", name = "quests") boolean quests
    ) {
        importProjectService.importProject(ImportProjectParameters.importSelected(
                projectId, recreateDatabase, projectInfo, reportConfig, students, quests
        ));
        return Result.success("项目 " + projectId + " 考生数据导入成功。");
    }


}
