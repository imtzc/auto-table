package org.dromara.autotable.test.core.entity.common;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.TableIndex;
import org.dromara.autotable.annotation.TableIndexes;

@Data
@FieldNameConstants
@AutoTable(comment = "测试备注''")
@TableIndexes({
        @TableIndex(fields = {TestTableIndex.Fields.tableIndex1, TestTableIndex.Fields.tableIndex2}, comment = "这是一个通过@TableIndex创建的索引")
})
public class TestTableIndex {
    private String tableIndex1;
    private String tableIndex2;
}
