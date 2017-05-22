package com.xz.scorep.executor.api.annotation;

/**
 * 接口返回值
 *
 * @author by fengye on 2017/5/22.
 */
public @interface ResultInfo {

    String success() default "true";

    // 单行返回值属性
    Property[] properties() default {};

    // 多行返回值属性
    ListProperty[] listProperties() default {};

}
