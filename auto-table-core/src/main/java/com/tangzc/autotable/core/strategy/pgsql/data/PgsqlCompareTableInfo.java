package com.tangzc.autotable.core.strategy.pgsql.data;

import com.tangzc.autotable.core.strategy.CompareTableInfo;
import com.tangzc.autotable.core.utils.StringUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author don
 */
@Getter
@Setter
public class PgsqlCompareTableInfo extends CompareTableInfo {

    /**
     * 注释: 有值说明需要改
     */
    private String comment;

    /**
     * 新的主键
     */
    private List<PgsqlColumnMetadata> newPrimaries = new ArrayList<>();
    /**
     * 不为空删除主键
     */
    private String dropPrimaryKeyName;

    /**
     * 注释: 需要添加/修改的字段注释《列名，注释内容》
     */
    private Map<String, String> columnComment = new HashMap<>();

    /**
     * 注释: 需要添加/修改的索引注释《索引名，注释内容》
     */
    private Map<String, String> indexComment = new HashMap<>();

    /**
     * 需要删除的列
     */
    private List<String> dropColumnList = new ArrayList<>();

    /**
     * 需要修改的列
     */
    private List<PgsqlColumnMetadata> modifyColumnMetadataList = new ArrayList<>();

    /**
     * 需要新增的列
     */
    private List<PgsqlColumnMetadata> newColumnMetadataList = new ArrayList<>();

    /**
     * 需要删除的索引
     */
    private List<String> dropIndexList = new ArrayList<>();

    /**
     * 新添加的索引
     */
    private List<PgsqlIndexMetadata> indexMetadataList = new ArrayList<>();

    public PgsqlCompareTableInfo(@NonNull String name) {
        super(name);
    }

    @Override
    public boolean needModify() {
        return StringUtils.hasText(comment) ||
                StringUtils.hasText(dropPrimaryKeyName) ||
                !newPrimaries.isEmpty() ||
                !columnComment.isEmpty() ||
                !indexComment.isEmpty() ||
                !dropColumnList.isEmpty() ||
                !modifyColumnMetadataList.isEmpty() ||
                !newColumnMetadataList.isEmpty() ||
                !dropIndexList.isEmpty() ||
                !indexMetadataList.isEmpty();
    }

    @Override
    public String validateFailedMessage() {
        StringBuilder errorMsg = new StringBuilder();
        if (StringUtils.hasText(comment)) {
            errorMsg.append("表注释变更: ").append(comment).append("\n");
        }
        if (StringUtils.hasText(dropPrimaryKeyName)) {
            errorMsg.append("删除主键: ").append(dropPrimaryKeyName).append("\n");
        }
        if (!newPrimaries.isEmpty()) {
            errorMsg.append("新增主键: ").append(newPrimaries).append("\n");
        }
        if (!columnComment.isEmpty()) {
            errorMsg.append("列注释变更: ").append(columnComment.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(", "))).append("\n");
        }
        if (!indexComment.isEmpty()) {
            errorMsg.append("索引注释变更: ").append(indexComment.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(", "))).append("\n");
        }
        if (!dropColumnList.isEmpty()) {
            errorMsg.append("删除列: ").append(String.join(",", dropColumnList)).append("\n");
        }
        if (!modifyColumnMetadataList.isEmpty()) {
            errorMsg.append("修改列: ").append(modifyColumnMetadataList.stream().map(PgsqlColumnMetadata::getName).collect(Collectors.joining(","))).append("\n");
        }
        if (!newColumnMetadataList.isEmpty()) {
            errorMsg.append("新增列: ").append(newColumnMetadataList.stream().map(PgsqlColumnMetadata::getName).collect(Collectors.joining(","))).append("\n");
        }
        if (!dropIndexList.isEmpty()) {
            errorMsg.append("删除索引: ").append(String.join(",", dropIndexList)).append("\n");
        }
        if (!indexMetadataList.isEmpty()) {
            errorMsg.append("新增索引: ").append(indexMetadataList.stream().map(PgsqlIndexMetadata::getName).collect(Collectors.joining(","))).append("\n");
        }
        return errorMsg.toString();
    }

    public void addColumnComment(String columnName, String newComment) {
        this.columnComment.put(columnName, newComment);
    }

    public void addNewColumn(PgsqlColumnMetadata columnMetadata) {
        this.newColumnMetadataList.add(columnMetadata);
    }

    public void addModifyColumn(PgsqlColumnMetadata columnMetadata) {
        this.modifyColumnMetadataList.add(columnMetadata);
    }

    public void addDropColumns(Set<String> dropColumnList) {
        this.dropColumnList.addAll(dropColumnList);
    }

    public void addNewIndex(PgsqlIndexMetadata pgsqlIndexMetadata) {
        this.indexMetadataList.add(pgsqlIndexMetadata);
    }

    public void addModifyIndex(PgsqlIndexMetadata pgsqlIndexMetadata) {
        this.dropIndexList.add(pgsqlIndexMetadata.getName());
        this.indexMetadataList.add(pgsqlIndexMetadata);
    }

    public void addIndexComment(@NonNull String indexName, String newComment) {
        this.indexComment.put(indexName, newComment);
    }

    public void addDropIndexes(Set<String> indexNameList) {
        this.dropIndexList.addAll(indexNameList);
    }

    public void addNewPrimary(List<PgsqlColumnMetadata> pgsqlColumnMetadata) {
        this.newPrimaries.addAll(pgsqlColumnMetadata);
    }
}
