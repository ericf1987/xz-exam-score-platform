package com.xz.scorep.executor.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 描述信息
 *
 * @author by fengye on 2017/5/22.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {
    public String value();
}
