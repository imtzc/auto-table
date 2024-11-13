package com.tangzc.autotable.core.utils;

import lombok.AllArgsConstructor;

import java.util.function.Supplier;

/**
 * 字符串拼接工具类
 * @author don
 */
@AllArgsConstructor(staticName = "newInstance")
public class StringConnectHelper {

    private String string;

    public StringConnectHelper replace(String key, String value) {
        string = string.replace(key, value);
        return this;
    }

    public StringConnectHelper replace(String key, Supplier<String> valueFunc) {
        string = string.replace(key, valueFunc.get());
        return this;
    }

    @Override
    public String toString() {
        return string;
    }
}
