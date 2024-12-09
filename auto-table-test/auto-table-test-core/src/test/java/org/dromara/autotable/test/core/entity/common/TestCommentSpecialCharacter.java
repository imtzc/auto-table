package org.dromara.autotable.test.core.entity.common;

import lombok.Data;
import org.dromara.autotable.annotation.AutoColumn;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.pgsql.PgsqlTypeConstant;

@Data
@AutoTable(comment = "测试备注''")
public class TestCommentSpecialCharacter {
    // 测试备注中的单引号'bug
    @AutoColumn(type = PgsqlTypeConstant.TEXT, comment = "平台域名白名单(多个用','分隔)")
    private String testColumn;
}
