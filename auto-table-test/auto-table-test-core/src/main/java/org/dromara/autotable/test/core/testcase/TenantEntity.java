package org.dromara.autotable.test.core.testcase;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户基类
 *
 * @author Michelle.Chung
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantEntity extends SuperEntity {

    /**
     * 租户编号
     */
    protected String tenantId;

}
