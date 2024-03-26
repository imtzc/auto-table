package com.tangzc.autotable.core.recordsql;

import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.annotation.Ignore;
import lombok.Getter;
import lombok.Setter;

@Getter
public class AutoTableExecuteSqlLog {

    @Ignore
    private Class<?> entityClass;

    private String tableName;

    @ColumnType(length = 5000)
    private String sqlStatement;

    @Setter
    private String version;

    private Long executionTime;

    private Long executionEndTime;

    private AutoTableExecuteSqlLog() {
    }

    public static AutoTableExecuteSqlLog of(Class<?> entityClass, String tableName, String sql, long executionTime, long executionEndTime) {
        AutoTableExecuteSqlLog autoTableExecuteSqlLog = new AutoTableExecuteSqlLog();
        autoTableExecuteSqlLog.entityClass = entityClass;
        autoTableExecuteSqlLog.tableName = tableName;
        autoTableExecuteSqlLog.sqlStatement = sql;
        autoTableExecuteSqlLog.executionTime = executionTime;
        autoTableExecuteSqlLog.executionEndTime = executionEndTime;
        return autoTableExecuteSqlLog;
    }
}
