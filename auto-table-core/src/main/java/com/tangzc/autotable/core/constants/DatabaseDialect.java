package com.tangzc.autotable.core.constants;

import com.sun.istack.internal.Nullable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author don
 */
@Getter
@Slf4j
public enum DatabaseDialect {

    /**
     * MySQL数据库
     */
    MySQL,

    /**
     * SQLite数据库
     */
    SQLite,

    /**
     * PostgreSQL数据库
     */
    PostgreSQL;

    public static @Nullable DatabaseDialect parseFromDialectName(String databaseDialect) {

        for (DatabaseDialect dialect : DatabaseDialect.values()) {
            if (databaseDialect.toLowerCase().startsWith(dialect.name().toLowerCase())) {
                return dialect;
            }
        }

        log.warn("无法识别的数据库类型: {}", databaseDialect);
        return null;
    }
}
