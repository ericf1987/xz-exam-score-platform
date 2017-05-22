package com.xz.scorep.executor.api.annotation;

/**
 * 用来描述多行返回值
 *
 * @author by fengye on 2017/5/22.
 */
public @interface ListProperty {
    //返回值名称
    String name();

    //描述
    String description() default "";

    // 列表元素的类别
    Class type() default Object.class;

    // 列表元素的属性
    Property[] properties() default {};
}
