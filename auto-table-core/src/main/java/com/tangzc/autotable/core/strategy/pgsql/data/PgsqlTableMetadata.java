package com.tangzc.autotable.core.strategy.pgsql.data;

import com.tangzc.autotable.core.strategy.ColumnMetadata;
import com.tangzc.autotable.core.strategy.IndexMetadata;
import com.tangzc.autotable.core.strategy.TableMetadata;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author don
 */
@Getter
@Setter
@Accessors(chain = true)
public class PgsqlTableMetadata extends TableMetadata {

    /**
     * 所有列信息
     */
    private List<ColumnMetadata> columnMetadataList;

    /**
     * 所有索引信息
     */
    private List<IndexMetadata> indexMetadataList;

    public PgsqlTableMetadata(String tableName) {
        super(tableName);
    }
}
