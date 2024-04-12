package com.tangzc.autotable.test.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MyTest {

    @Test
    public void test() {

        String dateTimeRegex = "(\\d+(.)?)+";
        System.out.println("2021/01/01 12:11:10:123".matches(dateTimeRegex));
    }
}
