package com.tangzc.autotable.test.core;

import lombok.Data;

/**
 * @author don
 */
@Data
public class BaseEntity<USER, TIME> {
    protected USER createBy;
    protected TIME createTime;
    protected USER modifyBy;
    protected TIME modifyTime;
}
