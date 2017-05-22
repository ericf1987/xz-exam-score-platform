package com.xz.scorep.executor.api.utils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 包工具，根据package路径，加载class
 *
 * @author zhaorenwu
 */
public class PackageUtil {

    private final static Logger LOG = LoggerFactory.getLogger(PackageUtil.class);

    /**
     *  扫描  scanPackages 下的文件的匹配符
     */
    protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";


    /**
     * 结合spring的类扫描方式
     * 根据需要扫描的包路径及相应的注解，获取最终测method集合
     * 仅返回public方法，如果方法是非public类型的，不会被返回
     * 可以扫描工程下的class文件及jar中的class文件
     *
     * @param scanPackages  包名称
     * @param annotation    注解类型
     *
     * @return Method列表
     */
    @SuppressWarnings({"unchecked", "unused"})
    public static <T> List<Method> findClassAnnotationMethods(
            String scanPackages, Class<? extends Annotation> annotation) {
        List<Class<T>> clazzSet = findPackageClass(scanPackages);
        List<Method> methods = new ArrayList<>();

        for (Class clazz: clazzSet) {
            try {
                Set<Method> annotationMethods = findAnnotationMethods(clazz, annotation);
                if (annotationMethods != null) {
                    methods.addAll(annotationMethods);
                }
            } catch (ClassNotFoundException ignore) {
                LOG.error("", ignore);
            }
        }

        return methods;
    }

    /**
     * 根据扫描包的,查询下面的所有类
     *
     * @param scanPackages 扫描的package路径
     *
     * @return Class列表
     */
    public static <T> List<Class<T>> findPackageClass(String... scanPackages) {
        List<Class<T>> classes = new ArrayList<>();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

        Set<String> packages = checkPackage(scanPackages);
        for (String basePackage : packages) {
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage)) +
                    "/" + DEFAULT_RESOURCE_PATTERN;

            try {
                Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
                for (Resource resource : resources) {
                    classes.add(loadClasses(metadataReaderFactory, resource));
                }
            } catch (Exception e) {
                LOG.error("获取包下面的类信息失败,package:" + basePackage, e);
            }

        }

        return classes;
    }

    /**
     * 加载资源，根据resource获取Class
     *
     * @param metadataReaderFactory spring中用来读取resource为class的工具
     * @param resource              这里的资源就是一个Class
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> loadClasses(
            MetadataReaderFactory metadataReaderFactory, Resource resource) throws IOException {
        try {
            if (resource.isReadable()) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                if (metadataReader != null) {
                    return (Class<T>) Class.forName(metadataReader.getClassMetadata().getClassName());
                }
            }
        } catch (Exception e) {
            LOG.error("根据resource获取类名称失败", e);
        }

        return null;
    }

    /**
     * 把action下面的所有method遍历一次，标记他们是否需要进行敏感词验证
     * 如果需要，放入cache中
     *
     * @param clazz 类
     */
    public static <T> Set<Method> findAnnotationMethods(
            Class<T> clazz, Class<? extends Annotation> anno) throws ClassNotFoundException {
        Set<Method> methodSet = new HashSet<>();
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            if (method.getModifiers() != Modifier.PUBLIC) {
                continue;
            }

            Annotation annotation = method.getAnnotation(anno);
            if (annotation != null) {
                methodSet.add(method);
            }
        }

        return methodSet;
    }

    /**
     * 排重、检测package父子关系，避免多次扫描
     *
     * @param scanPackages  包名称列表
     *
     * @return 返回检查后有效的路径集合
     */
    private static Set<String> checkPackage(String... scanPackages) {
        Set<String> packages = new HashSet<>();
        Collections.addAll(packages, scanPackages);
        Set<String> packagesBak = new HashSet<>(packages);

        for (String packageName : packagesBak) {
            if (StringUtils.isBlank(packageName) || packageName.startsWith(".")) {
                continue;
            }

            if (packageName.endsWith(".")) {
                packageName = packageName.substring(0, packageName.length() - 1);
            }

            boolean needAdd = true;
            Iterator<String> packageIte = packages.iterator();
            while (packageIte.hasNext()) {
                String pack = packageIte.next();
                if (packageName.startsWith(pack + ".")) {
                    needAdd = false;
                } else if (pack.startsWith(packageName + ".")) {
                    packageIte.remove();
                }
            }

            if (needAdd) {
                packages.add(packageName);
            }
        }

        return packages;
    }
}
