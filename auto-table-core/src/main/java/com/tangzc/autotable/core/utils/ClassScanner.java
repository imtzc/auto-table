package com.tangzc.autotable.core.utils;

import com.tangzc.autotable.core.AutoTableAnnotationFinder;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

/**
 * 基于注解扫描java类
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

        String path = packageName.replace('.', '/');
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Pattern pattern = Pattern.compile(path.replace("/", "\\/").replace("**", "[a-z0-9\\/]+").replace("*", "[a-z0-9]+") + "(\\/)?$");

        Resource[] resources = resolver.getResources(CLASSPATH_ALL_URL_PREFIX + path);
        Set<Class<?>> classes = new HashSet<>();

        Arrays.stream(resources).forEach(resource -> {
            try {
                String packageUrl = resource.getURL().toString();
                Matcher matcher = pattern.matcher(packageUrl);
                if(matcher.find()) {
                    String packageUrlPerfix = packageUrl.substring(0, packageUrl.length() - matcher.group().length());

                    if (resource.getURL().getProtocol().startsWith(ResourceUtils.URL_PROTOCOL_FILE)) {
                        classes.addAll(findClassesLocal(packageUrlPerfix, resource.getFile(), checker));
                    } else if (resource.getURL().getProtocol().startsWith(ResourceUtils.URL_PROTOCOL_JAR)) {
                        JarURLConnection jarURLConnection = (JarURLConnection) resource.getURL().openConnection();
                        classes.addAll(findClassesJar(packageName, jarURLConnection.getJarFile(), checker));
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        return classes;
    }

    private static Set<Class<?>> findClassesLocal(String packageUrlPerfix, File directory, Function<Class<?>, Boolean> checker) throws ClassNotFoundException, IOException {
        Set<Class<?>> classes = new HashSet<>();
        if (!directory.exists()) {
            return classes;
        }

        Files.walk(directory.toPath()).filter(
                (path)-> Files.isRegularFile(path) && path.toString().endsWith(".class")
        ).collect(Collectors.toList()).forEach(path -> {
            try {
                String pathUrl = path.toUri().toURL().toString();
                String className = pathUrl.substring(packageUrlPerfix.length(), pathUrl.length() - 6).replace("/", ".");
                Class<?> clazz = Class.forName(className);
                if (checker.apply(clazz)) { // check annotation
                    classes.add(clazz);
                }
            }catch(ClassNotFoundException | MalformedURLException e){
                // ignore
            }
        });
        return classes;
    }

    private static Set<Class<?>> findClassesJar(String packageName, JarFile jarFile, Function<Class<?>, Boolean> checker) throws ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        AntPathMatcher matcher = new AntPathMatcher();
        String antMacher = packageName.replace('.', '/');
        boolean isAntPattern = packageName.contains("**");
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class") && (isAntPattern ? matcher.matchStart(antMacher, entry.getName()) : entry.getName().startsWith(antMacher))){
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
