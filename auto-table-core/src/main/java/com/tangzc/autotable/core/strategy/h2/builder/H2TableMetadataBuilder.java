package com.tangzc.autotable.core.strategy.h2.builder;

import com.tangzc.autotable.core.builder.ColumnMetadataBuilder;
import com.tangzc.autotable.core.builder.DefaultTableMetadataBuilder;
import com.tangzc.autotable.core.builder.IndexMetadataBuilder;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.strategy.sqlite.builder.SqliteColumnMetadataBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author don
 */
@Slf4j
public class H2TableMetadataBuilder extends DefaultTableMetadataBuilder {

    public H2TableMetadataBuilder() {
        super(new ColumnMetadataBuilder(DatabaseDialect.H2), new IndexMetadataBuilder());
    }
}
