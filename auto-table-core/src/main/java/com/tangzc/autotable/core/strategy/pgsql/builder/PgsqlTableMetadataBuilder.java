package com.tangzc.autotable.core.strategy.pgsql.builder;

import com.tangzc.autotable.core.builder.DefaultTableMetadataBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author don
 */
@Slf4j
public class PgsqlTableMetadataBuilder extends DefaultTableMetadataBuilder {

    public PgsqlTableMetadataBuilder() {
        super(new PgsqlColumnMetadataBuilder(), new PgsqlIndexMetadataBuilder());
    }
}
