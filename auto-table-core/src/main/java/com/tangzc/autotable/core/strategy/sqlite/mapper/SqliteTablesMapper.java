package com.tangzc.autotable.core.strategy.sqlite.mapper;

import com.tangzc.autotable.core.strategy.sqlite.data.dbdata.SqliteMaster;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 创建更新表结构的Mapper
 * @author don
 */
public interface SqliteTablesMapper {

    /**
     * 根据表名判断是否存在
     *
     * @param tableName 表名
     * @return 影响行数
     */
    @Select("select count(1) from `sqlite_master` where type='table' and name=#{tableName};")
    int checkTableExist(String tableName);

    /**
     * 查询建表语句
     *
     * @param tableName 表名
     * @return 建表语句
     */
    @Select("select `sql` from sqlite_master where type='table' and name=#{tableName};")
    String queryBuildTableSql(String tableName);

    /**
     * 查询建表语句
     *
     * @param tableName 表名
     * @return 建表语句
     */
    @Results({
            @Result(column = "type", property = "type"),
            @Result(column = "name", property = "name"),
            @Result(column = "tbl_name", property = "tblName"),
            @Result(column = "rootpage", property = "rootpage"),
            @Result(column = "sql", property = "sql"),
    })
    @Select("select * from sqlite_master where type='index' and tbl_name=#{tableName};")
    List<SqliteMaster> queryBuildIndexSql(String tableName);
}
