package com.xz.scorep.executor.api.server.paperScreenShot;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.aggritems.StudentExamQuery;
import com.xz.scorep.executor.api.annotation.Function;
import com.xz.scorep.executor.api.annotation.Parameter;
import com.xz.scorep.executor.api.annotation.Type;
import com.xz.scorep.executor.api.server.Server;
import com.xz.scorep.executor.api.service.SubjectiveObjectiveService;
import com.xz.scorep.executor.pss.service.PssService;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 查询试卷截图留痕数据接口
 *
 * @author by fengye on 2017/5/22.
 */
@Function(description = "查询试卷截图留痕数据接口", parameters = {
        @Parameter(name = "projectId", type = Type.String, description = "考试项目ID", required = true),
        @Parameter(name = "schoolId", type = Type.String, description = "学校ID", required = true),
        @Parameter(name = "classId", type = Type.String, description = "班级ID", required = true),
        @Parameter(name = "subjectId", type = Type.String, description = "科目ID", required = true),
        @Parameter(name = "studentId", type = Type.String, description = "学生ID", required = true),
        @Parameter(name = "isPositive", type = Type.String, description = "正反面", required = true)
})
@Service
public class PaperImgServer implements Server {

    @Autowired
    PssService pssService;

    @Autowired
    private StudentExamQuery studentExamQuery;

    @Autowired
    private SubjectiveObjectiveService subjectiveObjectiveService;

    @Override
    public Result execute(Param param) {
        String projectId = param.getString("projectId");
        String schoolId = param.getString("schoolId");
        String classId = param.getString("classId");
        String subjectId = param.getString("subjectId");
        String studentId = param.getString("studentId");
        boolean isPositive = BooleanUtils.toBoolean(param.getString("isPositive"));

        //图片数据
        String imgString = pssService.getOneStudentOnePage(projectId, subjectId, studentId, isPositive, null);

        List<Map<String, Object>> objective =
                subjectiveObjectiveService.queryObjectiveScoreDetail(projectId, subjectId, classId, studentId);
        List<Map<String, Object>> subjective =
                subjectiveObjectiveService.querySubjectiveScoreDetail(projectId, subjectId, classId, studentId);

        Map<String, Object> studentScore = studentExamQuery.queryStudentScore(projectId, subjectId, studentId);
        Map<String, Object> overAverage = studentExamQuery.queryStudentOverAverage(projectId, subjectId, schoolId, classId, studentId);
        Map<String, Object> rankMap = studentExamQuery.queryStudentRank(projectId, subjectId, studentId);

        return Result.success().set("imgString", imgString)
                .set("objective", objective)
                .set("subjective", subjective)
                .set("studentScore", studentScore)
                .set("overAverage", overAverage)
                .set("rankMap", rankMap);
    }
}
