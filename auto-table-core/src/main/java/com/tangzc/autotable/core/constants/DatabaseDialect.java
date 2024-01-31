package com.tangzc.autotable.core.constants;

import lombok.Getter;

/**
 * @author don
 */
@Getter
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

    public static DatabaseDialect parseFromDialectName(String driverName) {

        for (DatabaseDialect dialect : DatabaseDialect.values()) {
            if (driverName.toLowerCase().startsWith(dialect.name().toLowerCase())) {
                return dialect;
            }
        }

        return null;
    }
}
