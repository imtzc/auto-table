package com.tangzc.autotable.core.utils;

import com.tangzc.autotable.core.AutoTableAnnotationFinder;
import com.tangzc.autotable.core.AutoTableGlobalConfig;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * @author don
 */
public class ClassScanner {

    public static Set<Class<?>> scan(String[] basePackages, Set<Class<? extends Annotation>> includeAnnotations, Set<Class<? extends Annotation>> excludeAnnotations) {

        if (basePackages == null || includeAnnotations == null) {
            return Collections.emptySet();
        }

        AutoTableAnnotationFinder autoTableAnnotationFinder = AutoTableGlobalConfig.getAutoTableAnnotationFinder();

        return Arrays.stream(basePackages)
                .map(basePackage -> {
                    try {
                        return getClasses(basePackage,
                                clazz -> includeAnnotations.stream().anyMatch(anno -> autoTableAnnotationFinder.exist(clazz, anno)) &&
                                        excludeAnnotations.stream().noneMatch(anno -> autoTableAnnotationFinder.exist(clazz, anno))
                        );
                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException(String.format("扫描包%s下实体出错", basePackage), e);
                    }
                }).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public static Set<Class<?>> getClasses(String packageName, Function<Class<?>, Boolean> checker) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        Set<Class<?>> classes = new HashSet<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if ("file".equals(resource.getProtocol())) {
                String decodedPath = URLDecoder.decode(resource.getFile(), "UTF-8");
                classes.addAll(findClassesLocal(packageName, new File(decodedPath), checker));
            } else if ("jar".equals(resource.getProtocol())) {
                JarURLConnection jarURLConnection = (JarURLConnection) resource.openConnection();
                classes.addAll(findClassesJar(packageName, jarURLConnection.getJarFile(), checker));
            }
        }

        return classes;
    }

    private static Set<Class<?>> findClassesLocal(String packageName, File directory, Function<Class<?>, Boolean> checker) throws ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    assert !file.getName().contains(".");
                    classes.addAll(findClassesLocal(packageName + "." + file.getName(), file, checker));
                } else if (file.getName().endsWith(".class")) {
                    Class<?> clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                    if (checker.apply(clazz)) {
                        classes.add(clazz);
                    }
                }
            }
        }

        return classes;
    }

    private static Set<Class<?>> findClassesJar(String packageName, JarFile jarFile, Function<Class<?>, Boolean> checker) throws ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<>();
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class") && entry.getName().startsWith(packageName.replace('.', '/'))) {
                String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                Class<?> clazz = Class.forName(className);
                if (checker.apply(clazz)) {
                    classes.add(clazz);
                }
            }
        }

        return classes;
    }
}
