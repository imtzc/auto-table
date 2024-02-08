package com.tangzc.autotable.core.strategy;

import lombok.Getter;
import lombok.NonNull;

/**
 * 比对表与实体的数据模型接口
 *
 * @author don
 */
@Getter
public abstract class CompareTableInfo {
    /**
     * 表名: 不可变，变了意味着新表
     */
    @NonNull
    protected final String name;

    public CompareTableInfo(@NonNull String name) {
        this.name = name;
    }

    /**
     * 是否需要修改表,即表与模型是否存在差异
     */
    public abstract boolean needModify();
}
