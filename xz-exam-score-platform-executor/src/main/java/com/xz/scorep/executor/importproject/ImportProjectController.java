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
    public Result importProject(@RequestParam("projectId") String projectId) {
        importProjectService.importProject(projectId);
        return Result.success("项目 " + projectId + " 考生数据导入成功。");
    }


}
