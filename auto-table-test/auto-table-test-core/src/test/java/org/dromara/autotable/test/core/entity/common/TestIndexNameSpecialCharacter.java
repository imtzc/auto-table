package org.dromara.autotable.test.core.entity.common;

import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.Index;

@Data
@AutoTable
public class TestIndexNameSpecialCharacter {
    @Index(name = "包含特殊字符','¥#(|)\"", comment = "这是一个通过@Index创建的普通索引")
    private String testIndex;
}
