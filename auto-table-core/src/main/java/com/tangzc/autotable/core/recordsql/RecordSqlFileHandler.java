package com.tangzc.autotable.core.recordsql;

import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.config.PropertyConfig;
import com.tangzc.autotable.core.dynamicds.DatasourceNameManager;
import com.tangzc.autotable.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class RecordSqlFileHandler implements RecordSqlHandler {
    @Override
    public void record(AutoTableExecuteSqlLog autoTableExecuteSqlLog) {

        PropertyConfig.RecordSqlProperties recordSql = AutoTableGlobalConfig.getAutoTableProperties().getRecordSql();

        String folderPath = recordSql.getFolderPath();
        Path path = getFilePath(folderPath, autoTableExecuteSqlLog);
        if (path != null) {
            try {
                String sqlStatement = autoTableExecuteSqlLog.getSqlStatement();
                // 末尾添加;
                if (!sqlStatement.endsWith(";")) {
                    sqlStatement = sqlStatement + ";";
                }
                // 末尾添加换行符
                if (!sqlStatement.endsWith(System.lineSeparator())) {
                    sqlStatement = sqlStatement + System.lineSeparator();
                }
                Files.write(path, sqlStatement.getBytes(StandardCharsets.UTF_8), java.nio.file.StandardOpenOption.APPEND);
            } catch (IOException e) {
                log.error("向{}写入SQL日志出错", path, e);
            }
        }
    }

    protected Path getFilePath(String folderPath, AutoTableExecuteSqlLog autoTableExecuteSqlLog) {

        if (StringUtils.noText(folderPath)) {
            log.error("没有指定SQL日志文件目录，无法记录SQL执行记录");
            return null;
        }

        String fileName = getFileName(autoTableExecuteSqlLog);

        Path path = Paths.get(folderPath, fileName);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            } catch (IOException e) {
                log.error("创建日志文件{}出错", path, e);
                return null;
            }
        }
        return path;
    }

    protected String getFileName(AutoTableExecuteSqlLog autoTableExecuteSqlLog) {

        StringBuilder fileName = new StringBuilder();
        // 添加版本号
        if (StringUtils.hasText(autoTableExecuteSqlLog.getVersion())) {
            fileName.append(autoTableExecuteSqlLog.getVersion()).append("_");
        }
        // 添加数据源名称
        String dataSourceName = DatasourceNameManager.getDatasourceName();
        if (StringUtils.hasText(dataSourceName)) {
            fileName.append(dataSourceName).append("_");
        }
        // 添加表名
        fileName.append(autoTableExecuteSqlLog.getTableName());

        return fileName.append(".sql").toString();
    }

}
