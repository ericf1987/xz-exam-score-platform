package com.xz.scorep.executor.api.controllers;

import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.api.annotation.Function;
import com.xz.scorep.executor.api.server.Server;
import com.xz.scorep.executor.api.server.ServerConsole;
import com.xz.scorep.executor.api.utils.ParamUtils;
import com.xz.scorep.executor.api.utils.ThrowableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author by fengye on 2017/5/22.
 */
@RestController
public class ApiController {

    public static final Logger LOG = LoggerFactory.getLogger(ApiController.class);

    @RequestMapping(value = "/api/{server}", method = RequestMethod.GET)
    public Result api(String p, @PathVariable("server") String server) {
        Server serverObj = ServerConsole.getServer(server);
        if (null == serverObj) {
            return Result.fail("(未知的接口 " + server + " )");
        }

        Function function = ServerConsole.getFunctionByName(server);

        Param param;

        // 解析参数
        try {
            param = ParamUtils.decipherParam(function, p);
            //LOG.info("Request: " + server + "/" + param);
        } catch (Exception e) {
            LOG.error("", e);
            return Result.fail(e.getMessage());
        }

        // 处理业务接口
        Result result;
        try {
            result = serverObj.execute(param);
        } catch (Exception e) {
            LOG.error("", e);
            return Result.fail(ThrowableUtils.toString(e));
        }

        //LOG.debug(server + " result: " + JSON.toJSONString(result));
        return result;
    }
}
