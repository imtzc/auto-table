package org.dromara.autotable.test.core.entity.sqlite;

import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;

@Data
@AutoTable
public class TestSqlite {

    private String name;

    private String age;

    private String address;
}
