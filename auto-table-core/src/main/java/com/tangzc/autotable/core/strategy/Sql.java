package com.tangzc.autotable.core.strategy;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class Sql {

    private SqlTypeEnum type;

    private String sql;

    public static enum SqlTypeEnum {
        INSERT,
        UPDATE,
        DELETE
    }
}
