package com.tangzc.autotable.springboot;

import com.tangzc.autotable.core.AutoTableBootstrap;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;

/**
 * @author don
 */
@AutoConfigureAfter({AutoTableAutoConfig.class})
public class AutoTableRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        // 启动AutoTable
        if (!isTestEnvironment()) {
            AutoTableBootstrap.start();
        }
    }

    public boolean isTestEnvironment() {
        try {
            // 尝试加载JUnit测试类
            Class.forName("org.junit.jupiter.api.Test");
            return true; // 如果找到则表示在测试环境中
        } catch (ClassNotFoundException e) {
            return false; // 否则是正常启动环境
        }
    }
}
