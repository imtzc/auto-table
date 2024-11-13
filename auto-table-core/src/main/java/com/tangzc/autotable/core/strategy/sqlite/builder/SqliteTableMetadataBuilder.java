package com.tangzc.autotable.core.strategy.sqlite.builder;

import com.tangzc.autotable.core.builder.IndexMetadataBuilder;
import com.tangzc.autotable.core.builder.DefaultTableMetadataBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author don
 */
@Slf4j
public class SqliteTableMetadataBuilder extends DefaultTableMetadataBuilder {

    public SqliteTableMetadataBuilder() {
        super(new SqliteColumnMetadataBuilder(), new IndexMetadataBuilder());
    }
}
