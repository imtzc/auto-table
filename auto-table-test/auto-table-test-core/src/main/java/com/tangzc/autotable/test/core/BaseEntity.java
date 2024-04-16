package com.tangzc.autotable.test.core;

import lombok.Data;

/**
 * @author don
 */
@Data
public class BaseEntity {
    private String createBy;
    private Long createTime;
    private String modifyBy;
    private Long modifyTime;
}
