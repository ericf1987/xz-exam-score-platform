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

import java.util.List;
import java.util.Map;

/**
 * @author luckylo
 */
@Function(description = "查询学生单题与班级差距较大的TOP5", parameters = {
        @Parameter(name = "projectId", type = Type.String, description = "考试项目ID", required = true),
        @Parameter(name = "studentId", type = Type.String, description = "学生ID", required = true),
        @Parameter(name = "subjectId", type = Type.String, description = "科目ID", required = true),
        @Parameter(name = "schoolId", type = Type.String, description = "学校ID", required = false),
        @Parameter(name = "classId", type = Type.String, description = "班级ID", required = true)
})
@Service
public class ObjectiveSubjectiveTop5Server implements Server {

    @Autowired
    private SubjectiveObjectiveService service;

    @Override
    public Result execute(Param param) {
        String projectId = param.getString("projectId");
        String subjectId = param.getString("subjectId");
        String classId = param.getString("classId");
        String studentId = param.getString("studentId");

        List<Map<String, Object>> objectiveTop5 = service.queryObjectiveTop5(projectId, subjectId, classId, studentId);
        List<Map<String, Object>> subjectiveTop5 = service.querySubjectiveTop5(projectId, subjectId, studentId, classId);
        return Result.success()
                .set("objective", objectiveTop5)
                .set("subjective", subjectiveTop5);
    }
}
