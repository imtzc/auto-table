package com.tangzc.autotable.core.strategy;

import com.tangzc.autotable.annotation.enums.IndexSortTypeEnum;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author don
 */
@Data
@Accessors(chain = true)
public class IndexMetadata {

    /**
     * 索引名称
     */
    protected String name;

    /**
     * 索引字段
     */
    protected List<IndexColumnParam> columns = new ArrayList<>();

    /**
     * 索引类型
     */
    protected IndexTypeEnum type;

    /**
     * 索引注释
     */
    protected String comment;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor(staticName = "newInstance")
    public static class IndexColumnParam {
        /**
         * 字段名称
         */
        protected String column;
        /**
         * 索引排序
         */
        protected IndexSortTypeEnum sort;
    }
}
