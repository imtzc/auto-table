package org.dromara.autotable.test.core.entity.pgsql;

import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.Index;
import org.dromara.autotable.annotation.enums.IndexTypeEnum;

@Data
@AutoTable
public class TestIndex {
    // 测试指定索引方法
    @Index(type = IndexTypeEnum.UNIQUE, comment = "这是一个通过@Index创建的唯一索引")
    private String testColumn;
}
