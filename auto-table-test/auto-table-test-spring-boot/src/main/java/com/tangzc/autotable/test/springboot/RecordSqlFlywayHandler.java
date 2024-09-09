package com.tangzc.autotable.test.springboot;

import com.tangzc.autotable.core.recordsql.AutoTableExecuteSqlLog;
import com.tangzc.autotable.core.recordsql.RecordSqlFileHandler;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 自定义sql记录，文件名以Flyway方式生成
 */
@Component
public class RecordSqlFlywayHandler extends RecordSqlFileHandler {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    protected Path getFilePath(AutoTableExecuteSqlLog autoTableExecuteSqlLog) {

        String sqlFilename = "V{version}_{time}__{table}.sql"
                .replace("{version}", autoTableExecuteSqlLog.getVersion())
                .replace("{time}", LocalDateTime.ofInstant(Instant.ofEpochMilli(autoTableExecuteSqlLog.getExecutionTime()), ZoneId.systemDefault()).format(dateTimeFormatter))
                .replace("{table}", autoTableExecuteSqlLog.getTableName());

        return Paths.get("/Users/don/Downloads/sqlLogs", sqlFilename);
    }
}
