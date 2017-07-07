package com.xz.scorep.executor.api.server.express;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.api.annotation.Function;
import com.xz.scorep.executor.api.annotation.Parameter;
import com.xz.scorep.executor.api.annotation.Type;
import com.xz.scorep.executor.api.server.Server;
import com.xz.scorep.executor.api.service.ExamBaseInfoQuery;
import com.xz.scorep.executor.bean.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 快速报表接口
 *
 * @author by fengye on 2017/7/3.
 */
@Function(description = "查询学生考试情况", parameters = {
        @Parameter(name = "projectId", type = Type.String, description = "考试项目ID", required = true),
        @Parameter(name = "subjectId", type = Type.String, description = "科目ID", required = true),
        @Parameter(name = "schoolId", type = Type.String, description = "学校ID", required = true),
        @Parameter(name = "classId", type = Type.String, description = "班级ID", required = true)
})
@Service
public class ExpressReportServer implements Server {

    @Autowired
    ExamBaseInfoQuery examBaseInfoQuery;

    @Override
    public Result execute(Param param) {

        return null;
    }
}
