package org.dromara.autotable.test.core.entity.common.extends2;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnDefault;

/**
 * 案件对象 zf_case
 *
 * @author chengliang4810
 * @date 2024-08-22
 */
@Data
@Accessors(chain = true)
@AutoTable(comment = "案件")
@EqualsAndHashCode(callSuper = true)
public class Case extends TenantEntity {

    private static final long serialVersionUID = 1L;
    /**
     * 是否办理结束
     */
    @ColumnDefault("false")
    private Boolean isEnd;

    /**
     * 案件逻辑删除
     */
    @ColumnDefault("0")
    private String deleteFlag;

    /**
     * 是否是案子
     */
    @ColumnDefault("false")
    private Boolean isCases;
}
