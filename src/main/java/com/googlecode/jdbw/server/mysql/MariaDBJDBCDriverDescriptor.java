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

import com.googlecode.jdbw.DatabaseServer;
import com.googlecode.jdbw.JDBCDriverDescriptor;
import com.googlecode.jdbw.server.StandardDatabaseServer;

/**
 * This driver descriptor is designed to be used with the official MariaDB JDBC driver.
 * @author Martin Berglund
 */
public class MariaDBJDBCDriverDescriptor implements JDBCDriverDescriptor<MariaDBDatabaseConnectionFactory> {

    @Override
    public String getDriverClassName() {
        return "org.mariadb.jdbc.Driver";
    }

    @Override
    public String formatJDBCUrl(DatabaseServer databaseServer) {
        return formatJDBCUrl(
                ((StandardDatabaseServer)databaseServer).getHostname(),
                ((StandardDatabaseServer)databaseServer).getPort(),
                ((StandardDatabaseServer)databaseServer).getDefaultCatalog());
    }

    /**
     * Creates a JDBC url based on supplied values
     * @param host Host where the database server is running
     * @param port Port the database server is listening on
     * @param defaultCatalog What catalog to use as the default for this connection
     * @return A JDBC url which can be used to connect to
     */
    public String formatJDBCUrl(String host, int port, String defaultCatalog) {
        return "jdbc:mariadb://" + host + ":" + port + "/" + defaultCatalog;
    }

    @Override
    public MariaDBDatabaseConnectionFactory createDatabaseConnectionFactory(DatabaseServer databaseServer) {
        return new MariaDBDatabaseConnectionFactory(formatJDBCUrl(databaseServer));
    }
}
