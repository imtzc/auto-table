package org.dromara.autotable.test.core.entity.common.extends1;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.autotable.test.core.BaseEntity;

import java.io.Serializable;

/**
 * @author don
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FatherEntity<USER extends Serializable> extends BaseEntity<USER, Long> {
    protected String name;
}
