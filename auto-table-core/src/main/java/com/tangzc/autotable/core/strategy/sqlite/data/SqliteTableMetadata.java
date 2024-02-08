package com.tangzc.autotable.core.strategy.sqlite.data;

import com.tangzc.autotable.core.strategy.TableMetadata;
import lombok.Data;
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
public class SqliteTableMetadata extends TableMetadata {

    /**
     * 注释
     */
    private String comment;
    /**
     * 所有列
     */
    private List<SqliteColumnMetadata> columnMetadataList = new ArrayList<>();
    /**
     * 索引
     */
    private List<SqliteIndexMetadata> indexMetadataList = new ArrayList<>();

    public SqliteTableMetadata(String tableName) {
        super(tableName);
    }
}
