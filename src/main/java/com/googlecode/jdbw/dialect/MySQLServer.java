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
 * Copyright (C) 2009-2012 mabe02
 */
package com.googlecode.jdbw.dialect;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.googlecode.jdbw.DatabaseServer;
import com.googlecode.jdbw.DatabaseServerType;
import com.googlecode.jdbw.SQLExecutor;

/**
 *
 * @author mabe02
 */
public class MySQLServer extends DefaultDatabaseServer {

    private MySQLServer(String hostname, int port, String catalog, String username, String password) {
        super(hostname, port, catalog, username, password);
    }

    public static MySQLServer newInstance(String hostname, String catalog, String username, String password) {
        return newInstance(hostname, "3306", catalog, username, password);
    }

    public static MySQLServer newInstance(String hostname, String port, String catalog, String username, String password) {
        return newInstance(hostname, new Integer(port), catalog, username, password);
    }

    public static MySQLServer newInstance(String hostname, int port, String catalog, String username, String password) {
        return new MySQLServer(hostname, port, catalog, username, password);
    }

    @Override
    public DatabaseServerType getServerType() {
        return DatabaseServerType.MYSQL;
    }

    @Override
    protected Properties getConnectionProperties() {
        Properties properties = new Properties();
        properties.setProperty("user", getUsername());
        properties.setProperty("password", getPassword());
        return properties;
    }

    @Override
    protected String getJDBCUrl() {
        return "jdbc:mysql://" + getHostname() + ":" + getPort() + "/" + getCatalog()
                + "?useUnicode=yes"
                + "&characterEncoding=UTF-8"
                + "&rewriteBatchedStatements=true"
                + "&continueBatchOnError=false"
                + "&allowMultiQueries=true"
                + "&useCompression=true"
                + "&zeroDateTimeBehavior=convertToNull";
    }

    @Override
    protected void loadDriver() {
        loadDriver("com.mysql.jdbc.Driver");
    }

    @Override
    public DatabaseServerTraits getServerTraits() {
        return new MySQLTraits();
    }

    @Override
    public String toString() {
        return "mysql://" + getHostname() + ":" + getPort() + "/" + getCatalog();
    }

    @Override
    protected boolean isConnectionError(SQLException e) {
        if("MySQLSyntaxErrorException".equals(e.getClass().getSimpleName())) {
            return false;
        }

        return super.isConnectionError(e);
    }

    @Override
    protected DefaultDatabaseConnection createDatabaseConnection() {
        return new MySQLDatabaseConnectionPool(this);
    }

    public static class Factory extends DatabaseServerFactory {

        @Override
        public DatabaseServer createDatabaseServer(String hostname, int port, String catalog, String username, String password) {
            return new MySQLServer(hostname, port, catalog, username, password);
        }
    }

    private static class MySQLDatabaseConnectionPool extends DefaultDatabaseConnection {

        public MySQLDatabaseConnectionPool(DefaultDatabaseServer databaseServer) {
            super(databaseServer);
        }

        @Override
        protected MetaDataResolver createMetaDataResolver() {
            return new MySQLMetaDataResolver(this);
        }

        @Override
        protected PooledDatabaseConnection newPooledDatabaseConnection(Connection connection) {
            return new MySQLPooledDatabaseConnection(connection);
        }
    }

    private static class MySQLMetaDataResolver extends DefaultMetaDataResolver {

        public MySQLMetaDataResolver(MySQLDatabaseConnectionPool connectionPool) {
            super(connectionPool);
        }

        @Override
        protected MetaDataFactory getMetaDataFactory() {
            return new MySQLMetaDataFactory(this);
        }

        @Override
        protected List<String> getSchemaNames(String catalogName) throws SQLException {
            return Arrays.asList("schema");
        }

        @Override
        protected List<String> getProcedureInputParameterNames(String catalogName, String schemaName, StoredProcedure procedure) throws SQLException {
            return super.getProcedureInputParameterNames(catalogName, null, procedure);
        }

        @Override
        protected void extractIndexDataFromMetaResult(ResultSet resultSet, Map<String, Index> indexMap, Table table) throws SQLException {
            String indexName = resultSet.getString("INDEX_NAME");
            boolean unique = !resultSet.getBoolean("NON_UNIQUE");
            boolean clustered = resultSet.getShort("TYPE") == DatabaseMetaData.tableIndexClustered;
            String columnName = resultSet.getString("COLUMN_NAME");
            boolean primaryKey = "PRIMARY".equals(indexName);

            Column column = table.getColumn(columnName);

            if(indexName == null) //Only named indexes!
            {
                return;
            }

            if(indexMap.containsKey(indexName)) {
                indexMap.get(indexName).addColumn(column);
            } else {
                indexMap.put(indexName, new Index(indexName, unique, clustered, primaryKey, table, column));
            }
        }
    }

    private static class MySQLMetaDataFactory extends DefaultMetaDataFactory {

        public MySQLMetaDataFactory(MetaDataResolver metaDataResolver) {
            super(metaDataResolver);
        }

        @Override
        public Catalog createCatalog(String catalogName) {
            return new MySQLCatalog(metaDataResolver, catalogName);
        }
    }

    private static class MySQLPooledDatabaseConnection extends PooledDatabaseConnection {

        public MySQLPooledDatabaseConnection(Connection connection) {
            super(connection);
        }

        @Override
        protected SQLExecutor createExecutor() {
            return new MySQLExecutor(this);
        }
    }

    private static class MySQLExecutor extends DefaultSQLExecutor {

        public MySQLExecutor(PooledDatabaseConnection pooledConnection) {
            super(pooledConnection);
        }

        @Override
        protected PreparedStatement prepareExecuteStatement(String SQL) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(SQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
                    java.sql.ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(Integer.MIN_VALUE);
            return ps;
        }

        @Override
        protected PreparedStatement prepareQueryStatement(String SQL) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(SQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
                    java.sql.ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(Integer.MIN_VALUE);
            return ps;
        }

        @Override
        protected PreparedStatement prepareUpdateStatement(String SQL) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
            return ps;
        }

        @Override
        protected PreparedStatement prepareBatchUpdateStatement(String SQL) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(SQL, Statement.NO_GENERATED_KEYS);
            return ps;
        }

        @Override
        protected void executeUpdate(Statement statement, String SQL) throws SQLException {
            super.executeUpdate(statement, SQL);
        }
    }

    public static class MySQLCatalog extends Catalog {

        public MySQLCatalog(MetaDataResolver metaDataResolver, String name) {
            super(metaDataResolver, name);
        }

        @Override
        public Schema getSchema(String schemaName) throws SQLException {
            return getSchemas().get(0);
        }
    }
}
