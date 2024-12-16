package org.dromara.autotable.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Utils {

    public static boolean tableIsExists(Connection connection, String schema, String tableName, String[] types, boolean ignoreCase) throws SQLException {

        boolean exists = false;
        String catalog = connection.getCatalog();
        if(ignoreCase) {
            ResultSet tables = connection.getMetaData().getTables(catalog, schema, null, types);
            while (tables.next()) {
                String existingTableName = tables.getString("TABLE_NAME");
                if (existingTableName.equalsIgnoreCase(tableName)) { // 忽略大小写比较
                    exists = true;
                    break;
                }
            }
        } else {
            exists = connection.getMetaData().getTables(catalog, schema, tableName, types).next();
        }
        return exists;
    }
}
