package com.tangzc.autotable.core.intercepter;

import com.tangzc.autotable.core.strategy.TableMetadata;

@FunctionalInterface
public interface BuildTableMetadataIntercepter {

    void intercept(String databaseDialect, TableMetadata tableMetadata);
}
