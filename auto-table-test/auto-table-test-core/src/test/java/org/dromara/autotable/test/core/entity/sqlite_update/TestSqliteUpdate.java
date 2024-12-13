package org.dromara.autotable.test.core.entity.sqlite_update;

import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;

@Data
@AutoTable("test_sqlite")
public class TestSqliteUpdate {

    private String name;

    private String age;

    // 删除
    // private String address;

    // 新增列
    private String phone;
}
