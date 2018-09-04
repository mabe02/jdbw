/*
 * This file is part of jdbw (http://code.google.com/p/jdbw/).
 *
 * jdbw is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2007-2012 Martin Berglund
 */

package com.googlecode.jdbw.server.mysql;

import com.googlecode.jdbw.metadata.*;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;

/**
 * A meta data resolver tuned for MySQL
 * @author Martin Berglund
 */
class MySQLMetaDataResolver extends DefaultServerMetaData {

    MySQLMetaDataResolver(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Schema> getSchemas(Catalog catalog) throws SQLException {
        return Arrays.asList(createSchema(catalog, MySQLServer.DEFAULT_MYSQL_SCHEMA_NAME));
    }

    @Override
    public Schema getSchema(Catalog catalog, String schemaName) throws SQLException {
        if(Objects.equals(MySQLServer.DEFAULT_MYSQL_SCHEMA_NAME, schemaName)) {
            return getSchemas(catalog).get(0);
        }
        else {
            return null;
        }
    }

    @Override
    protected TableColumn createTableColumn(
            Table table,
            int ordinalPosition,
            String columnName,
            int sqlType,
            String typeName,
            int columnSize,
            int decimalDigits,
            int nullable,
            String autoIncrement) {

        if (isSingleBit(typeName, sqlType, columnSize)) {
            sqlType = Types.BOOLEAN;
            typeName = "BIT";
            columnSize = 1;
        }
        else if (isUnsignedTinyInt(typeName, sqlType, columnSize)) {
            // Unsigned tinyint, we need to change this to a smallint to capture the whole 0-255 range
            sqlType = Types.SMALLINT;
            typeName = "SMALLINT";
            columnSize = 1;
        }

        return super.createTableColumn(table, ordinalPosition, columnName, sqlType, typeName, columnSize, decimalDigits, nullable, autoIncrement);
    }

    private boolean isSingleBit(String typeName, int sqlType, int columnSize) {
        return sqlType == Types.BIT && columnSize == 1;
    }

    private boolean isUnsignedTinyInt(String typeName, int sqlType, int columnSize) {
        if ("TINYINT UNSIGNED".equals(typeName)) {
            return true;
        }
        else if (sqlType == Types.BIT && columnSize == 3) {
            return true;
        }
        return false;
    }

    //TODO: Fix this properly by loading primary keys from DatabaseMetaData using the appropriate method
    @Override
    protected Index createIndex(Table table, String indexName, short type, boolean unique, TableColumn firstColumn) {
        return super.createIndex(table, indexName, Objects.equals("PRIMARY", indexName) ? DatabaseMetaData.tableIndexClustered : type, unique, firstColumn);
    }
}
