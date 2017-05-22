package com.xz.scorep.executor.api.server;

import com.xz.scorep.executor.api.AppException;
import com.xz.scorep.executor.api.annotation.Function;
import com.xz.scorep.executor.api.context.App;
import com.xz.scorep.executor.api.utils.PackageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务控制台
 *
 * @author by fengye on 2017/5/22.
 */
public class ServerConsole {
    static final Logger LOG = LoggerFactory.getLogger(ServerConsole.class);

    public static Map<String, Function> SERVER_FUNCTION_MAP = new HashMap<>();

    public static Map<String, Server> SERVER_MAP = new HashMap<>();

    public static final String[] packageName = new String[]{
            "com.xz.scorep.executor.api.server"
    };

    public static void start() throws AppException {
        List<Class<Object>> loadClasses = new ArrayList<>();

        try {
            //获取指定包位置的所有类和接口
            List<Class<Object>> packageClasses = PackageUtil.findPackageClass(packageName);
            //不列出接口和抽象类
            packageClasses.removeIf(p -> p.isInterface() || Modifier.isAbstract(p.getModifiers()));
            //过滤非Server子类
            packageClasses.removeIf(p -> !Server.class.isAssignableFrom(p));

            packageClasses.forEach(p -> {
                Function function = p.getAnnotation(Function.class);

                String simpleName = p.getSimpleName();

                SERVER_FUNCTION_MAP.put(simpleName, function);

                SERVER_MAP.put(simpleName, (Server) App.getBean(p));

                loadClasses.add(p);
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("server api start is Exception", e.fillInStackTrace());
        }

        List<String> classNames = loadClasses.stream().map(Class::getSimpleName).collect(Collectors.toList());
        LOG.info("load Servers:{}", classNames);
    }

    /**
     * 通过类名获取Server的类
     *
     * @param className 类名
     *
     * @return  Server类
     */
    public static Server getServer(String className) {
        return SERVER_MAP.get(className);
    }

    /**
     * 获取指定function
     * @param className 类名
     * @return
     */
    public static Function getFunctionByName(String className) {
        return SERVER_FUNCTION_MAP.get(className);
    }

    /**
     * 获取所有接口列表
     */
    public static Map<String, Function> getAllFunctions() {
        return SERVER_FUNCTION_MAP;
    }
}
