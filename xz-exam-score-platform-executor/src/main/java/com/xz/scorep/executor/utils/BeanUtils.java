package com.xz.scorep.executor.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * (description)
 * created at 2017/2/9
 *
 * @author yidin
 */
public class BeanUtils {

    private static final Logger LOG = LoggerFactory.getLogger(BeanUtils.class);

    /**
     * 将属性值从 source 拷贝到 dst，仅当 source 的属性值不为 null，且 dst 的对应属性值为 null 时才拷贝该属性
     *
     * @param dst    目标对象
     * @param source 源对象
     *
     * @throws IntrospectionException 如果解析 source 的属性失败
     */
    public static <T> void fillProperties(T dst, T source) throws IntrospectionException {
        if (dst == null || source == null) {
            return;
        }

        Class<?> dstClass = dst.getClass();
        BeanInfo beanInfo = Introspector.getBeanInfo(source.getClass());
        List<String> failProperties = new ArrayList<>();

        for (PropertyDescriptor pDescriptor : beanInfo.getPropertyDescriptors()) {

            if (pDescriptor.getName().equals("class")) {
                continue;
            }

            try {
                tryCopyProperty(dst, source, dstClass, pDescriptor);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                failProperties.add(pDescriptor.getName());
            }
        }

        if (!failProperties.isEmpty()) {
            LOG.error("Failed to copy properties " + failProperties + " from " + source + " to " + dst);
        }
    }

    private static <T> void tryCopyProperty(
            T dst, T source, Class<?> dstClass, PropertyDescriptor pDescriptor
    ) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Method srcReadMethod = pDescriptor.getReadMethod();
        Object srcPropertyValue = srcReadMethod.invoke(source);

        if (srcPropertyValue != null) {
            Method dstReadMethod = dstClass.getDeclaredMethod(srcReadMethod.getName());
            Object dstPropertyValue = dstReadMethod.invoke(dst);

            if (dstPropertyValue == null) {
                String writeMethodName = pDescriptor.getWriteMethod().getName();
                Class<?>[] writeMethodParamTypes = pDescriptor.getWriteMethod().getParameterTypes();
                Method dstWriteMethod = dstClass.getDeclaredMethod(writeMethodName, writeMethodParamTypes);
                dstWriteMethod.invoke(dst, srcPropertyValue);
            }
        }
    }
}
