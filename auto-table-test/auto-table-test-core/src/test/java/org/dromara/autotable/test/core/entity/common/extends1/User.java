package org.dromara.autotable.test.core.entity.common.extends1;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.PrimaryKey;

/**
 * @author don
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AutoTable("sys_user")
public class User extends FatherEntity<String> {

    @PrimaryKey(autoIncrement = true)
    @ColumnComment("用户id")
    private Long id;

    @ColumnComment("姓名")
    private String name;
}
