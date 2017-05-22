package com.xz.scorep.executor.api.annotation;

import java.lang.annotation.*;

/**
 * 用来描述一个接口文档
 *
 * @author by fengye on 2017/5/22.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Function {

    //接口描述
    String description();

    // 接口参数
    Parameter[] parameters() default {};

    // 接口返回值
    ResultInfo result() default @ResultInfo();
}
