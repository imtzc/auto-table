package com.tangzc.autotable.springboot;

import com.tangzc.autotable.springboot.properties.AutoTableProperties;
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
@Import({AutoTableAutoConfig.class})
public @interface EnableAutoTable {
}
