package org.dromara.autotable.test.core.entity.pgsql_update;

import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.Index;

@Data
@AutoTable("test_index")
public class TestIndexUpdate {
    // 测试指定索引方法
    @Index(method = "HASH", comment = "这是一个通过@Index创建的唯一索引")
    private String testColumn;
}
