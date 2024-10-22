package com.tangzc.autotable.test.core;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author don
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FatherEntity<USER> extends BaseEntity<USER, Long> {
    protected String name;
}
