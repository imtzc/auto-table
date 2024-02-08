package com.tangzc.autotable.core.strategy.sqlite.data;

import com.tangzc.autotable.core.strategy.CompareTableInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author don
 */
@Getter
@Setter
public class SqliteCompareTableInfo extends CompareTableInfo {

    /**
     * 构建表的sql，如果不为空，则重新构建表
     */
    private String rebuildTableSql;

    /**
     * 新构建索引的sql
     */
    private List<String> buildIndexSqlList = new ArrayList<>();

    /**
     * 待删除的索引
     */
    private List<String> deleteIndexList = new ArrayList<>();

    public SqliteCompareTableInfo(@NonNull String name) {
        super(name);
    }

    @Override
    public boolean needModify() {
        return rebuildTableSql != null ||
                !buildIndexSqlList.isEmpty() ||
                !deleteIndexList.isEmpty();
    }

    @Data
    @AllArgsConstructor
    public static class RebuildIndex {

        private String name;
        private String sql;
    }
}
