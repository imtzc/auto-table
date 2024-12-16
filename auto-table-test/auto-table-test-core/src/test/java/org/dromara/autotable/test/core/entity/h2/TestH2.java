package org.dromara.autotable.test.core.entity.h2;

import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnDefault;

@Data
@AutoTable
public class TestH2 {

    @ColumnDefault("这个人很懒～，没有什么可说的。123～abc~")
    private String testColumn;
}
