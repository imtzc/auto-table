package org.dromara.autotable.core.recordsql;

import java.util.List;

public interface RecordSqlHandler {

    /**
     * 记录sql
     * @param autoTableExecuteSqlLog sql对象
     */
    void record(List<AutoTableExecuteSqlLog> autoTableExecuteSqlLogs);
}
