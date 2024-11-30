package org.dromara.autotable.springboot;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;

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
@TestExecutionListeners(
        listeners = {AutoTableTestExecutionListener.class},
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
@Import(AutoTableImportRegister.class)
public @interface EnableAutoTableTest {

    String[] basePackages() default {};

    Class[] classes() default {};
}
