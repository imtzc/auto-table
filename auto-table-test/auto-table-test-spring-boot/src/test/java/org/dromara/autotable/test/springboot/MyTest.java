package org.dromara.autotable.test.springboot;

import org.dromara.autotable.springboot.EnableAutoTableTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@EnableAutoTableTest
@SpringBootTest
public class MyTest {

    @Test
    public void test() {

        String dateTimeRegex = "(\\d+(.)?)+";
        System.out.println("2021/01/01 12:11:10:123".matches(dateTimeRegex));
    }
}
