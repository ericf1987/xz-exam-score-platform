package com.xz.scorep.executor.api.annotation;

/**
 * 接口返回值的属性描述
 * @author by fengye on 2017/5/22.
 */
public @interface Property {
    // 属性名
    String name();

    // 属性类型
    Type type();

    // 描述
    String description() default "";

    Class pojoType() default Object.class;
}
