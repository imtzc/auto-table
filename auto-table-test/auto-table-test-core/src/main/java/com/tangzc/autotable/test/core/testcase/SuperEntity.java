package com.tangzc.autotable.test.core.testcase;

import com.tangzc.autotable.annotation.Ignore;
import com.tangzc.autotable.test.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity基类
 *
 * @author Lion Li
 */

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SuperEntity extends BaseEntity<Long, Date> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 搜索值
     */
    @Ignore
    private String searchValue;

    /**
     * 创建部门
     */
    protected Long createDept;

    /**
     * 请求参数
     */
    @Ignore
    private Map<String, Object> params = new HashMap<>();

}
