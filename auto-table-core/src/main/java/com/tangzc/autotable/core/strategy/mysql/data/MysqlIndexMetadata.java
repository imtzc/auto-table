package com.tangzc.autotable.core.strategy.mysql.data;

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
public class MysqlIndexMetadata {

    /**
     * 索引名称
     */
    private String name;

    /**
     * 索引字段
     */
    private List<IndexColumnParam> columns = new ArrayList<>();

    /**
     * 索引类型
     */
    private IndexTypeEnum type;

    /**
     * 索引注释
     */
    private String comment;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor(staticName = "newInstance")
    public static class IndexColumnParam {
        /**
         * 字段名称
         */
        private String column;
        /**
         * 索引排序
         */
        private IndexSortTypeEnum sort;
    }
}
