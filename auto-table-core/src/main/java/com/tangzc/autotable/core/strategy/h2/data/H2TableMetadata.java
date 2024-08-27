package com.tangzc.autotable.core.strategy.h2.data;

import com.tangzc.autotable.core.strategy.ColumnMetadata;
import com.tangzc.autotable.core.strategy.IndexMetadata;
import com.tangzc.autotable.core.strategy.TableMetadata;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlColumnMetadata;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author don
 */
@Getter
@Setter
@Accessors(chain = true)
public class H2TableMetadata extends TableMetadata {

    /**
     * 引擎
     */
    private String engine;
    /**
     * 默认字符集
     */
    private String characterSet;
    /**
     * 默认排序规则
     */
    private String collate;
    /**
     * 所有列
     */
    private List<ColumnMetadata> columnMetadataList = new ArrayList<>();
    /**
     * 索引
     */
    private List<IndexMetadata> indexMetadataList = new ArrayList<>();

    public H2TableMetadata(Class<?> entityClass, String tableName, String comment) {
        super(entityClass, tableName, "", comment);
    }
}
