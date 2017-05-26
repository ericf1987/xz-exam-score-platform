package com.xz.scorep.executor.api.server.paperScreenShot;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.api.server.Server;

/**
 * 查询学生主客观题情况(总分,我的得分,得分排名,班级平均分,班级最高分)
 *
 * @author luckylo
 */
public class ObjectiveSubjectiveServer implements Server {

    @Override
    public Result execute(Param param) {
        String studentId = param.getString("student_id");
        String subjectId = param.getString("subject_id");
        String schoolId = param.getString("school_id");
        String classId = param.getString("class_id");

        return Result.success()
                .set("objective", null)
                .set("subjective", null);
    }
}
