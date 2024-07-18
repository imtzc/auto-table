package com.tangzc.autotable.test.core;

import lombok.Data;

/**
 * @author don
 */
@Data
public class BaseEntity {
    protected String createBy;
    protected Long createTime;
    protected String modifyBy;
    protected Long modifyTime;
}
