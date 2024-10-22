package com.tangzc.autotable.test.core;

import com.tangzc.autotable.core.recordsql.AutoTableExecuteSqlLog;
import com.tangzc.autotable.core.recordsql.RecordSqlFileHandler;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 自定义sql记录，文件名以Flyway方式生成
 */
public class RecordSqlFlywayHandler extends RecordSqlFileHandler {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final String basePath;

    public RecordSqlFlywayHandler(String basePath) {
        this.basePath = basePath;
    }

    @Override
    protected Path getFilePath(AutoTableExecuteSqlLog autoTableExecuteSqlLog) {

        String sqlFilename = "V{version}_{time}__{table}.sql"
                .replace("{version}", autoTableExecuteSqlLog.getVersion())
                .replace("{time}", LocalDateTime.ofInstant(Instant.ofEpochMilli(autoTableExecuteSqlLog.getExecutionTime()), ZoneId.systemDefault()).format(dateTimeFormatter))
                .replace("{table}", autoTableExecuteSqlLog.getTableName());

        return Paths.get(basePath, sqlFilename);
    }
}
