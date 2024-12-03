package org.dromara.autotable.test.core;

import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;

@Data
@AutoTable("test_sqlite_entity")
public class TestSqliteEntity_ {

    private String name;

    private String age;

    private String phone;
}
