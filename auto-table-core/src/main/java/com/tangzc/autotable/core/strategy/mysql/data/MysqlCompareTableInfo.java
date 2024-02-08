package com.tangzc.autotable.core.strategy.mysql.data;

import com.tangzc.autotable.core.strategy.CompareTableInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author don
 */
@Getter
@Setter
public class MysqlCompareTableInfo extends CompareTableInfo {

    /**
     * 引擎: 有值，则说明需要修改
     */
    private String engine;
    /**
     * 默认字符集: 有值，则说明需要修改
     */
    private String characterSet;
    /**
     * 默认排序规则: 有值，则说明需要修改
     */
    private String collate;
    /**
     * 注释: 有值，则说明需要修改
     */
    private String comment;
    /**
     * 新的主键
     */
    private List<MysqlColumnMetadata> newPrimaries = new ArrayList<>();
    /**
     * 是否删除主键
     */
    private boolean dropPrimary;
    /**
     * 删除的列：谨慎，会导致数据丢失
     */
    private final List<String> dropColumnList = new ArrayList<>();
    /**
     * 修改的列，包含新增、修改
     */
    private final List<MysqlModifyColumnMetadata> modifyMysqlColumnMetadataList = new ArrayList<>();
    /**
     * 删除的索引
     */
    private final List<String> dropIndexList = new ArrayList<>();
    /**
     * 索引
     */
    private final List<MysqlIndexMetadata> mysqlIndexMetadataList = new ArrayList<>();

    public MysqlCompareTableInfo(@NonNull String name) {
        super(name);
    }

    /**
     * 判断该修改参数，是不是可用，如果除了name，其他值均没有设置过，则无效，反之有效
     */
    @Override
    public boolean needModify() {
        return engine != null ||
                characterSet != null ||
                collate != null ||
                comment != null ||
                dropPrimary ||
                !newPrimaries.isEmpty() ||
                !dropColumnList.isEmpty() ||
                !modifyMysqlColumnMetadataList.isEmpty() ||
                !dropIndexList.isEmpty() ||
                !mysqlIndexMetadataList.isEmpty();
    }

    public void addNewColumnMetadata(MysqlColumnMetadata mysqlColumnMetadata) {
        this.modifyMysqlColumnMetadataList.add(new MysqlModifyColumnMetadata(ModifyType.ADD, mysqlColumnMetadata));
    }

    public void addEditColumnMetadata(MysqlColumnMetadata mysqlColumnMetadata) {
        this.modifyMysqlColumnMetadataList.add(new MysqlModifyColumnMetadata(ModifyType.MODIFY, mysqlColumnMetadata));
    }

    /**
     * 重设主键
     */
    public void resetPrimary(List<MysqlColumnMetadata> primaries) {
        this.newPrimaries = primaries;
        this.dropPrimary = true;
    }

    @Data
    @AllArgsConstructor
    public static class MysqlModifyColumnMetadata {
        private ModifyType type;
        private MysqlColumnMetadata mysqlColumnMetadata;
    }

    public static enum ModifyType {
        ADD, MODIFY
    }
}
