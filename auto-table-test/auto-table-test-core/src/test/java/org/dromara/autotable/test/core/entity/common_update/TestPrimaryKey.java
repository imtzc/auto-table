package org.dromara.autotable.test.core.entity.common_update;

import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.PrimaryKey;

@Data
@AutoTable
public class TestPrimaryKey {

    // 非自增主键 改为 自增主键
    // @PrimaryKey
    @PrimaryKey(autoIncrement = true)
    private Long id;
}
