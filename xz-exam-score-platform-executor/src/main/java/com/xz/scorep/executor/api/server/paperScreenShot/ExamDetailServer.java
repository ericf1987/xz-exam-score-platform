package com.xz.scorep.executor.api.server.paperScreenShot;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.aggritems.StudentExamQuery;
import com.xz.scorep.executor.api.annotation.Function;
import com.xz.scorep.executor.api.annotation.Parameter;
import com.xz.scorep.executor.api.annotation.Type;
import com.xz.scorep.executor.api.server.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author luckylo
 */
@Function(description = "查询学生考试情况", parameters = {
        @Parameter(name = "projectId", type = Type.String, description = "考试项目ID", required = true),
        @Parameter(name = "studentId", type = Type.String, description = "学生ID", required = true),
        @Parameter(name = "subjectId", type = Type.String, description = "科目ID", required = true),
        @Parameter(name = "schoolId", type = Type.String, description = "学校ID", required = true),
        @Parameter(name = "classId", type = Type.String, description = "班级ID", required = true)
})
@Service
public class ExamDetailServer implements Server {

    @Autowired
    private StudentExamQuery studentExamQuery;

    @Override
    public Result execute(Param param) {
        String projectId = param.getString("projectId");
        String studentId = param.getString("studentId");
        String schoolId = param.getString("schoolId");
        String classId = param.getString("classId");
        String subjectId = param.getString("subjectId");

        Map<String, Object> studentScore = studentExamQuery.queryStudentScore(projectId, subjectId, studentId);
        Map<String, Object> overAverage = studentExamQuery.queryStudentOverAverage(projectId, subjectId, schoolId, classId, studentId);
        Map<String, Object> rankMap = studentExamQuery.queryStudentRank(projectId, subjectId, studentId);

        return Result.success()
                .set("score", studentScore)
                .set("rank", rankMap)
                .set("overAverage", overAverage);
    }
}
