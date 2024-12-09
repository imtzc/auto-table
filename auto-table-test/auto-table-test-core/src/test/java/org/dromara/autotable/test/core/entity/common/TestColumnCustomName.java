package org.dromara.autotable.test.core.entity.common;

import lombok.Data;
import org.dromara.autotable.annotation.AutoColumn;
import org.dromara.autotable.annotation.AutoTable;

@Data
@AutoTable
public class TestColumnCustomName {
    @AutoColumn("custom_name")
    private String testColumn;
}
