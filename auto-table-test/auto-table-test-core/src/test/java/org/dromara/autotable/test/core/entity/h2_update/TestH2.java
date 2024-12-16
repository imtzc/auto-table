package org.dromara.autotable.test.core.entity.h2_update;

import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;

@Data
@AutoTable
public class TestH2 {

    // @ColumnDefault("这个人很懒～，没有什么可说的。123～abc~")
    private String testColumn;
}
