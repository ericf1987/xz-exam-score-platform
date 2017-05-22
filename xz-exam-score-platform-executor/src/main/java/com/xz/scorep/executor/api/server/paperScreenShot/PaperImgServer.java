package com.xz.scorep.executor.api.server.paperScreenShot;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.api.annotation.Function;
import com.xz.scorep.executor.api.annotation.Parameter;
import com.xz.scorep.executor.api.annotation.Type;
import com.xz.scorep.executor.api.server.Server;
import org.springframework.stereotype.Service;

/**
 * 查询试卷截图留痕数据接口
 * @author by fengye on 2017/5/22.
 */
@Function(description = "查询试卷截图留痕数据接口", parameters = {
        @Parameter(name = "projectId", type = Type.String, description = "考试项目ID", required = true)
})
@Service
public class PaperImgServer implements Server{
    @Override
    public Result execute(Param param) {
        return null;
    }
}
