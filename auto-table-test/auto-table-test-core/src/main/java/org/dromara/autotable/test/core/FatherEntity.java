package org.dromara.autotable.test.core;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author don
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FatherEntity<USER extends Serializable> extends BaseEntity<USER, Long> {
    protected String name;
}
