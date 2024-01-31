package com.tangzc.autotable.core.utils;

import lombok.AllArgsConstructor;

import java.util.function.Function;

/**
 * @author don
 */
@AllArgsConstructor(staticName = "newInstance")
public class StringHelper {

    private String string;

    public StringHelper replace(String key, String value) {
        string = string.replace(key, value);
        return this;
    }

    public StringHelper replace(String key, Function<String, String> valueFunc) {
        string = string.replace(key, valueFunc.apply(key));
        return this;
    }

    @Override
    public String toString() {
        return string;
    }

    /**
     * 字符串驼峰转下划线格式
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public static String camelToUnderline(String param) {

        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append('_');
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }
}
