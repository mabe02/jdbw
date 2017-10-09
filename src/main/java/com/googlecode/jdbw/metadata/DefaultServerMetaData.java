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
package com.googlecode.jdbw.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;

/**
 * A default implementation of ServerMetaData that uses JDBC DatabaseMetaData to read information on the database and
 * convert it to the data model in this package. If you are creating support for a new database server, some of the
 * methods in here may not work as the JDBC driver may not fully implement all functionality of DatabaseMetaData (this
 * is more common than one would expect...). In this case, you may want to extern from this class and override the
 * methods that are not working and implement this using custom code to get the information.
 */
public class DefaultServerMetaData implements ServerMetaData {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServerMetaData.class);
    protected final DataSource dataSource;

    /**
     * Creates a new meta data resolver based on a DataSource passed in that will be used to drawing connections on
     * every operation that requires a call to the database.
     * @param dataSource What data source to use when talking to the database
     */
    public DefaultServerMetaData(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Catalog> getCatalogs() throws SQLException {
        try (Connection pooledConnection = dataSource.getConnection()) {
            List<Catalog> result = new ArrayList<>();
            for (String catalogName : readResultSetColumn(getCatalogNames(pooledConnection), 1)) {
                result.add(createCatalog(catalogName));
            }
            return result;
        }
    }

    @Override
    public Catalog getCatalog(String catalogName) throws SQLException {
        for(Catalog catalog: getCatalogs()) {
            if(Objects.equals(catalog.getName(), catalogName))
                return catalog;
        }
        return null;
    }

    @Override
    public List<Schema> getSchemas(Catalog catalog) throws SQLException {
        try (Connection pooledConnection = dataSource.getConnection()) {
            List<Schema> result = new ArrayList<>();
            ResultSet resultSet = getSchemaMetadata(pooledConnection, catalog, null);
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            try {
                while (resultSet.next()) {
                    //Some database venders doesn't send the second column!
                    if (resultSetMetaData.getColumnCount() < 2) {
                        result.add(createSchema(catalog, resultSet.getString(1)));
                    }
                    else if (resultSet.getString(2) == null || resultSet.getString(2).equals(catalog.getName())) {
                        result.add(createSchema(catalog, resultSet.getString(1)));
                    }
                }
                return result;
            }
            finally {
                try {
                    resultSet.close();
                }
                catch (SQLException e2) {
                    LOGGER.error("Failed to close the result set", e2);
                }
            }
        }
    }

    @Override
    public Schema getSchema(Catalog catalog, String schemaName) throws SQLException {
        try (Connection pooledConnection = dataSource.getConnection()) {
            ResultSet resultSet = getSchemaMetadata(pooledConnection, catalog, schemaName);
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            try {
                while (resultSet.next()) {
                    //Some database venders doesn't send the second column!
                    if (resultSetMetaData.getColumnCount() < 2) {
                        if (resultSet.getString(1) != null && Objects.equals(resultSet.getString(1), schemaName)) {
                            return createSchema(catalog, resultSet.getString(1));
                        }
                    }
                    else if (resultSet.getString(2) == null || resultSet.getString(2).equals(catalog.getName())) {
                        if (resultSet.getString(1) != null && Objects.equals(resultSet.getString(1), schemaName)) {
                            return createSchema(catalog, resultSet.getString(1));
                        }
                    }
                }
                return null;
            }
            finally {
                try {
                    resultSet.close();
                }
                catch (SQLException e2) {
                    LOGGER.error("Failed to close the result set", e2);
                }
            }
        }
    }

    @Override
    public List<Table> getTables(Schema schema) throws SQLException {
        try (Connection pooledConnection = dataSource.getConnection()) {
            List<Table> result = new ArrayList<>();
            for (String tableName : readResultSetColumn(getTableMetadata(pooledConnection, schema, null), 3)) {
                result.add(createTable(schema, tableName));
            }
            return result;
        }
    }

    @Override
    public Table getTable(Schema schema, String tableName) throws SQLException {
        try (Connection pooledConnection = dataSource.getConnection()) {
            for (String foundTableName : readResultSetColumn(getTableMetadata(pooledConnection, schema, tableName), 3)) {
                if (Objects.equals(foundTableName, tableName)) {
                    return createTable(schema, foundTableName);
                }
            }
            return null;
        }
    }

    @Override
    public List<SystemTable> getSystemTables(Schema schema) throws SQLException {
        try (Connection pooledConnection = dataSource.getConnection()) {
            List<SystemTable> result = new ArrayList<>();
            for (String tableName : readResultSetColumn(getSystemTableMetadata(pooledConnection, schema, null), 3)) {
                result.add(createSystemTable(schema, tableName));
            }
            return result;
        }
    }

    @Override
    public SystemTable getSystemTable(Schema schema, String systemTableName) throws SQLException {
        try (Connection pooledConnection = dataSource.getConnection()) {
            for (String foundTableName : readResultSetColumn(getSystemTableMetadata(pooledConnection, schema, systemTableName), 3)) {
                if (Objects.equals(foundTableName, systemTableName)) {
                    return createSystemTable(schema, foundTableName);
                }
            }
            return null;
        }
    }

    @Override
    public List<View> getViews(Schema schema) throws SQLException {
        try (Connection pooledConnection = dataSource.getConnection()) {
            List<View> result = new ArrayList<>();
            for (String viewName : readResultSetColumn(getViewMetadata(pooledConnection, schema, null), 3)) {
                result.add(createView(schema, viewName));
            }
            return result;
        }
    }

    @Override
    public View getView(Schema schema, String viewName) throws SQLException {
        try (Connection pooledConnection = dataSource.getConnection()) {
            for (String foundViewName : readResultSetColumn(getViewMetadata(pooledConnection, schema, viewName), 3)) {
                if (Objects.equals(foundViewName, viewName)) {
                    return createView(schema, foundViewName);
                }
            }
            return null;
        }
    }

    @Override
    public List<TableColumn> getColumns(Table table) throws SQLException {
        try (Connection pooledConnection = dataSource.getConnection()) {
            List<TableColumn> result = new ArrayList<>();
            ResultSet resultSet = getTableColumnMetadata(pooledConnection, table);
            while (resultSet.next()) {
                result.add(
                        createTableColumn(
                                table,
                                resultSet.getInt("ORDINAL_POSITION"),
                                resultSet.getString("COLUMN_NAME"),
                                resultSet.getInt("DATA_TYPE"),
                                resultSet.getString("TYPE_NAME"),
                                resultSet.getInt("COLUMN_SIZE"),
                                resultSet.getInt("DECIMAL_DIGITS"),
                                resultSet.getInt("NULLABLE"),
                                resultSet.getString("IS_AUTOINCREMENT")));
            }
            return result;
        }
    }

    @Override
    public List<Index> getIndexes(Table table) throws SQLException {
        
        //Preload all the table columns so we don't need to look them up later, while the connection
        //below is in use (won't work for single-connection pools)
        Map<String, TableColumn> tableColumns = table.getColumnMap();

        try (Connection pooledConnection = dataSource.getConnection()) {
            Map<String, Index> result = new HashMap<>();
            ResultSet resultSet = getIndexMetadata(pooledConnection, table);
            while (resultSet.next()) {
                String indexName = resultSet.getString("INDEX_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");
                if (result.containsKey(indexName)) {
                    result.get(indexName).addColumn(tableColumns.get(columnName));
                }
                else {
                    result.put(indexName,
                            createIndex(
                                    table,
                                    indexName,
                                    resultSet.getShort("TYPE"),
                                    !resultSet.getBoolean("NON_UNIQUE"),
                                    tableColumns.get(columnName)));
                }
            }
            return sortIndexList(new ArrayList<>(result.values()));
        }
    }

    @Override
    public List<ViewColumn> getColumns(View view) throws SQLException {
        try (Connection pooledConnection = dataSource.getConnection()) {
            List<ViewColumn> result = new ArrayList<>();
            ResultSet resultSet = getViewColumnMetadata(pooledConnection, view);
            while (resultSet.next()) {
                result.add(
                        createViewColumn(
                                view,
                                resultSet.getInt("ORDINAL_POSITION"),
                                resultSet.getString("COLUMN_NAME"),
                                resultSet.getInt("DATA_TYPE"),
                                resultSet.getString("TYPE_NAME"),
                                resultSet.getInt("COLUMN_SIZE"),
                                resultSet.getInt("DECIMAL_DIGITS"),
                                resultSet.getInt("NULLABLE"),
                                resultSet.getString("IS_AUTOINCREMENT")));
            }
            return result;
        }
    }

    @Override
    public List<StoredProcedure> getStoredProcedures(Schema schema) throws SQLException {
        try (Connection pooledConnection = dataSource.getConnection()) {
            List<StoredProcedure> result = new ArrayList<>();
            for (String procedureName : readResultSetColumn(getStoredProcedureMetadata(pooledConnection, schema, null), 3)) {
                result.add(createStoredProcedure(schema, procedureName));
            }
            return result;
        }
    }

    @Override
    public StoredProcedure getStoredProcedure(Schema schema, String procedureName) throws SQLException {
        try (Connection pooledConnection = dataSource.getConnection()) {
            for (String foundProcName : readResultSetColumn(getStoredProcedureMetadata(pooledConnection, schema, procedureName), 3)) {
                if (Objects.equals(foundProcName, procedureName)) {
                    return createStoredProcedure(schema, foundProcName);
                }
            }
            return null;
        }
    }

    @Override
    public List<Function> getFunctions(Schema schema) throws SQLException {
        try (Connection pooledConnection = dataSource.getConnection()) {
            List<Function> result = new ArrayList<>();
            for (String functionName : readResultSetColumn(getFunctionMetadata(pooledConnection, schema, null), 3)) {
                result.add(createFunction(schema, functionName));
            }
            return result;
        }
    }

    @Override
    public Function getFunction(Schema schema, String functionName) throws SQLException {
        try (Connection pooledConnection = dataSource.getConnection()) {
            for (String foundFunctionName : readResultSetColumn(getFunctionMetadata(pooledConnection, schema, functionName), 3)) {
                if (Objects.equals(foundFunctionName, functionName)) {
                    return createFunction(schema, foundFunctionName);
                }
            }
            return null;
        }
    }
    
    protected List<String> readResultSetColumn(ResultSet resultSet, int index) throws SQLException {
        List<String> list = new ArrayList<>();
        try {
            while(resultSet.next()) {
                list.add(resultSet.getString(index));
            }
            return list;
        }
        finally {
            try {
                resultSet.close();
            }
            catch(SQLException e) {
                LOGGER.error("Failed to close the result set", e);
            }
        }
    }
    
    protected Catalog createCatalog(String catalogName) {
        return new Catalog(this, catalogName);
    }
    
    protected Schema createSchema(Catalog catalog, String schemaName) {
        return new Schema(this, catalog, schemaName);
    }
    
    protected Table createTable(Schema schema, String tableName) {
        return new Table(this, schema, tableName);
    }

    private SystemTable createSystemTable(Schema schema, String tableName) {
        return new SystemTable(this, schema, tableName);
    }
    
    protected View createView(Schema schema, String viewName) {
        return new View(this, schema, viewName);
    }
    
    protected StoredProcedure createStoredProcedure(Schema schema, String procedureName) {
        return new StoredProcedure(this, schema, procedureName);
    }

    protected Function createFunction(Schema schema, String functionName) {
        return new Function(schema, functionName);
    }

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
        
        return new TableColumn(table, ordinalPosition, columnName, sqlType, typeName, columnSize, decimalDigits, nullable, autoIncrement);
    }

    protected ViewColumn createViewColumn(
            View view, 
            int ordinalPosition, 
            String columnName, 
            int sqlType, 
            String typeName, 
            int columnSize, 
            int decimalDigits, 
            int nullable, 
            String autoIncrement) {
        
        return new ViewColumn(view, ordinalPosition, columnName, sqlType, typeName, columnSize, decimalDigits, nullable, autoIncrement);
    }

    protected Index createIndex(
            Table table, 
            String indexName, 
            short type, 
            boolean unique, 
            TableColumn firstColumn) {
        
        boolean clustered = (type == DatabaseMetaData.tableIndexClustered);
        boolean primaryKey = unique && clustered;
        return new Index(table, indexName, unique, clustered, primaryKey, firstColumn);
    }

    protected ResultSet getCatalogNames(Connection pooledConnection) throws SQLException {
        return pooledConnection.getMetaData().getCatalogs();
    }

    protected ResultSet getSchemaMetadata(Connection pooledConnection, Catalog catalog, String schemaName) throws SQLException {
        return pooledConnection.getMetaData().getSchemas(catalog != null ? catalog.getName() : null, schemaName);
    }

    protected ResultSet getTableMetadata(Connection pooledConnection, Schema schema, String tableName) throws SQLException {
        return pooledConnection.getMetaData().getTables(
                schema.getCatalog().getName(), 
                schema.getName(), 
                tableName, 
                new String[]{"TABLE"});
    }

    protected ResultSet getSystemTableMetadata(Connection pooledConnection, Schema schema, String systemTableName) throws SQLException {
        return pooledConnection.getMetaData().getTables(
                schema.getCatalog().getName(), 
                schema.getName(), 
                systemTableName, 
                new String[]{"SYSTEM TABLE"});
    }

    protected ResultSet getViewMetadata(Connection pooledConnection, Schema schema, String viewName) throws SQLException {
        return pooledConnection.getMetaData().getTables(
                schema.getCatalog().getName(), 
                schema.getName(), 
                viewName, 
                new String[]{"VIEW"});
    }

    protected ResultSet getIndexMetadata(Connection pooledConnection, Table table) throws SQLException {
        return pooledConnection.getMetaData().getIndexInfo(
                table.getSchema().getCatalog().getName(), 
                table.getSchema().getName(), 
                table.getName(), 
                false /* unique */,
                false /* approximate */);
    }

    protected ResultSet getTableColumnMetadata(Connection pooledConnection, Table table) throws SQLException {
        return pooledConnection.getMetaData().getColumns(
                table.getSchema().getCatalog().getName(), 
                table.getSchema().getName(), 
                table.getName(), 
                null /* columnNamePattern */);
    }

    protected ResultSet getViewColumnMetadata(Connection pooledConnection, View view) throws SQLException {
        return pooledConnection.getMetaData().getColumns(
                view.getSchema().getCatalog().getName(), 
                view.getSchema().getName(), 
                view.getName(), 
                null /* columnNamePattern */);
    }

    protected ResultSet getStoredProcedureMetadata(Connection pooledConnection, Schema schema, String procedureName) throws SQLException {
        return pooledConnection.getMetaData().getProcedures(
                schema.getCatalog().getName(), 
                schema.getName(), 
                procedureName);
    }

    protected ResultSet getFunctionMetadata(Connection pooledConnection, Schema schema, String functionName) throws SQLException {
        return pooledConnection.getMetaData().getFunctions(
                schema.getCatalog().getName(), 
                schema.getName(), 
                functionName);
    }

    protected List<Index> sortIndexList(List<Index> indexes) {
        Collections.sort(indexes);
        return indexes;
    }
}
