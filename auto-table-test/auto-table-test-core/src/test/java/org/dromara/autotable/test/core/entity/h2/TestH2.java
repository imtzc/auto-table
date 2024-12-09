package org.dromara.autotable.test.core.entity.h2;

import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.Index;

@Data
@AutoTable
public class TestH2 {
    // 测试指定索引方法
    @Index(method = "HASH")
    private String testColumn;
}
