package com.tangzc.autotable.core.recordsql;

import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.config.PropertyConfig;
import com.tangzc.autotable.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RecordSqlService {

    public static void record(List<AutoTableExecuteSqlLog> autoTableExecuteSqlLogs) {

        PropertyConfig.RecordSqlProperties recordSql = AutoTableGlobalConfig.getAutoTableProperties().getRecordSql();

        if (!recordSql.isEnable()) {
            return;
        }

        RecordSqlHandler recordSqlHandler;
        PropertyConfig.RecordSqlProperties.TypeEnum recordType = recordSql.getRecordType();
        switch (recordType) {
            case db:
                log.info("开启数据库记录执行SQL");
                recordSqlHandler = new RecordSqlDbHandler();
                break;
            case file:
                log.info("开启文件记录执行SQL");
                recordSqlHandler = new RecordSqlFileHandler();
                break;
            case custom:
                log.info("开启自定义记录执行SQL");
            default:
                recordSqlHandler = AutoTableGlobalConfig.getCustomRecordSqlHandler();
                break;
        }

        String version = recordSql.getVersion();

        if (StringUtils.noText(version)) {
            log.warn("AutoTable的SQL记录功能没有配置版本号，默认为空，强烈建议关联即将上线的版本号，根据版本管理SQL日志，避免混乱");
        }

        for (AutoTableExecuteSqlLog autoTableExecuteSqlLog : autoTableExecuteSqlLogs) {
            // 设置手动指定的版本
            autoTableExecuteSqlLog.setVersion(version);
            // 调用不同的记录器
            recordSqlHandler.record(autoTableExecuteSqlLog);
        }
    }
}
