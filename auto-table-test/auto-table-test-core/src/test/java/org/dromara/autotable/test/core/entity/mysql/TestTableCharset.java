package org.dromara.autotable.test.core.entity.mysql;

import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.mysql.MysqlCharset;

@Data
@AutoTable
@MysqlCharset(charset = "utf8mb4", collate = "utf8mb4_general_ci")
public class TestTableCharset {
    private String id;
}
