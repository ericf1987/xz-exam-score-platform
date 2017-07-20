package com.xz.scorep.executor.api.server.mongoaggr;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.api.annotation.Function;
import com.xz.scorep.executor.api.annotation.Parameter;
import com.xz.scorep.executor.api.annotation.Type;
import com.xz.scorep.executor.api.server.Server;
import com.xz.scorep.executor.api.utils.HttpUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 唤醒mongodb统计项目导入数据
 *
 * @author by fengye on 2017/7/19.
 */
@Function(description = "查询学生考试情况", parameters = {
        @Parameter(name = "projectId", type = Type.String, description = "考试项目ID", required = true),
        @Parameter(name = "filePath", type = Type.String, description = "文件路径", required = true)
})
@Service
public class NotifyImportMysqlDump implements Server {

    public static final String MONGO_IMPORT_DUMP_URL = "http://10.10.22.212:8083/import-aggr-data/importMysqlDump";

    @Override
    public Result execute(Param param) {

        Map<String, String> p = new HashMap<>();

        p.put("projectId", param.getString("projectId"));

        p.put("filePath", param.getString("filePath"));

        HttpUtils.sendRequest(MONGO_IMPORT_DUMP_URL, p);

        return Result.success();
    }
}
