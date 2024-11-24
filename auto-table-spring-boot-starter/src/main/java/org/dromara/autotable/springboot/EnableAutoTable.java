package org.dromara.autotable.springboot;

import org.dromara.autotable.springboot.properties.AutoTableProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author don
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableConfigurationProperties(AutoTableProperties.class)
@Import({AutoTableAutoConfig.class, AutoTableImportRegister.class, AutoTableRunner.class})
public @interface EnableAutoTable {

    String[] basePackages() default {};
}
