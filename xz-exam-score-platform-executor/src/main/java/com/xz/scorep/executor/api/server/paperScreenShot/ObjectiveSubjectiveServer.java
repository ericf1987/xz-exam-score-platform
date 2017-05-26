package com.xz.scorep.executor.api.server.paperScreenShot;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.api.annotation.Function;
import com.xz.scorep.executor.api.annotation.Parameter;
import com.xz.scorep.executor.api.annotation.Type;
import com.xz.scorep.executor.api.server.Server;
import com.xz.scorep.executor.api.service.SubjectiveObjectiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 查询学生主客观题情况(总分,我的得分,得分排名,班级平均分,班级最高分)
 *
 * @author luckylo
 */
@Function(description = "查询学生考试情况", parameters = {
        @Parameter(name = "projectId", type = Type.String, description = "考试项目ID", required = true),
        @Parameter(name = "studentId", type = Type.String, description = "学生ID", required = true),
        @Parameter(name = "subjectId", type = Type.String, description = "科目ID", required = true),
        @Parameter(name = "schoolId", type = Type.String, description = "学校ID", required = false),
        @Parameter(name = "classId", type = Type.String, description = "班级ID", required = true)
})
@Service
public class ObjectiveSubjectiveServer implements Server {

    @Autowired
    private SubjectiveObjectiveService service;

    @Override
    public Result execute(Param param) {
        String projectId = param.getString("projectId");
        String studentId = param.getString("studentId");
        String subjectId = param.getString("subjectId");
        String classId = param.getString("classId");

        Map<String, Object> objective = service.objectiveDetail(projectId, subjectId, classId, studentId);
        Map<String, Object> subjective = service.subjectiveDetail(projectId, subjectId, classId, studentId);

        return Result.success()
                .set("objective", objective)
                .set("subjective", subjective);
    }
}
