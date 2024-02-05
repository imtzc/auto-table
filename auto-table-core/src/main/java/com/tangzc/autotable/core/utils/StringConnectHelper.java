package com.tangzc.autotable.core.utils;

import lombok.AllArgsConstructor;

import java.util.function.Function;

/**
 * @author don
 */
@AllArgsConstructor(staticName = "newInstance")
public class StringConnectHelper {

    private String string;

    public StringConnectHelper replace(String key, String value) {
        string = string.replace(key, value);
        return this;
    }

    public StringConnectHelper replace(String key, Function<String, String> valueFunc) {
        string = string.replace(key, valueFunc.apply(key));
        return this;
    }

    @Override
    public String toString() {
        return string;
    }
}
