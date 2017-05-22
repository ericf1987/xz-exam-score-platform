package com.xz.scorep.executor.api.annotation;

/**
 * 用来描述一个接口参数
 *
 * @author by fengye on 2017/5/22.
 */
public @interface Parameter {

    //参数名称
    String name();

    //参数类型
    Type type();

    //参数描述
    String description();

    //是否必须
    boolean required() default true;

    //正则表达式模板
    String pattern() default "";

    //缺省值
    String defaultValue() default "";
}
