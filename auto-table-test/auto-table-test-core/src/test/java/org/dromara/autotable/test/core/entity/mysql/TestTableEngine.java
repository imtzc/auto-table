package org.dromara.autotable.test.core.entity.mysql;

import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.mysql.MysqlEngine;

@Data
@AutoTable
@MysqlEngine("MyISAM")
public class TestTableEngine {
    private String id;
}
