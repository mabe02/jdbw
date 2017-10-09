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
package com.googlecode.jdbw.server.postgresql;

import com.googlecode.jdbw.DatabaseServer;
import com.googlecode.jdbw.JDBCDriverDescriptor;
import com.googlecode.jdbw.impl.AuthenticatingDatabaseConnectionFactory;
import com.googlecode.jdbw.server.StandardDatabaseServer;

/**
 * JDBC driver descriptor designed to work with the official PostgreSQL JDBC client driver,
 * {@code org.postgresql.Driver}
 * @author Martin Berglund
 */
public class PostgreSQLJDBCDriverDescriptor implements JDBCDriverDescriptor<AuthenticatingDatabaseConnectionFactory> {
    @Override
    public String formatJDBCUrl(DatabaseServer<AuthenticatingDatabaseConnectionFactory> databaseServer) {
        return formatJDBCUrl(
                ((StandardDatabaseServer<AuthenticatingDatabaseConnectionFactory>)databaseServer).getHostname(),
                ((StandardDatabaseServer<AuthenticatingDatabaseConnectionFactory>)databaseServer).getPort(),
                ((StandardDatabaseServer<AuthenticatingDatabaseConnectionFactory>)databaseServer).getDefaultCatalog());
    }

    /**
     * Creates a JDBC url based on supplied values
     * @param host Host where the database server is running
     * @param port Port the database server is listening on
     * @param defaultCatalog What catalog to use as the default for this connection
     * @return A JDBC url which can be used to connect to
     */
    public String formatJDBCUrl(String host, int port, String defaultCatalog) {
        return "jdbc:postgresql://" + host + ":" + port + "/" + defaultCatalog;
    }

    @Override
    public String getDriverClassName() {
        return "org.postgresql.Driver";
    }

    @Override
    public AuthenticatingDatabaseConnectionFactory createDatabaseConnectionFactory(DatabaseServer<AuthenticatingDatabaseConnectionFactory> databaseServer) {
        return new AuthenticatingDatabaseConnectionFactory(PostgreSQLServerType.INSTANCE, formatJDBCUrl(databaseServer));
    }
}
