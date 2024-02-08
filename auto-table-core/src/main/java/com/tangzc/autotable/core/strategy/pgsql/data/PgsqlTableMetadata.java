package com.tangzc.autotable.core.strategy.pgsql.data;

import com.tangzc.autotable.core.strategy.TableMetadata;
import lombok.Data;
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
     * 注释
     */
    private String comment;

    /**
     * 所有列信息
     */
    private List<PgsqlColumnMetadata> columnMetadataList;

    /**
     * 所有索引信息
     */
    private List<PgsqlIndexMetadata> indexMetadataList;

    public PgsqlTableMetadata(String tableName) {
        super(tableName);
    }
}
