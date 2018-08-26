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

import com.googlecode.jdbw.DatabaseServerType;
import com.googlecode.jdbw.DatabaseServerTypes;
import com.googlecode.jdbw.JDBCDriverDescriptor;
import com.googlecode.jdbw.server.StandardDatabaseServer;

/**
 * This class represents a MySQL database server that is connected to over a TCP/IP network.
 * @author Martin Berglund
 */
public class MariaDBServer extends StandardDatabaseServer<MariaDBDatabaseConnectionFactory> {

    public static final String DEFAULT_MARIADB_SCHEMA_NAME = MySQLServer.DEFAULT_MYSQL_SCHEMA_NAME;
    public static final int DEFAULT_MARIADB_PORT = 3306;

    public MariaDBServer(String hostname, String catalog) {
        this(new MariaDBJDBCDriverDescriptor(), hostname, DEFAULT_MARIADB_PORT, catalog);
    }

    public MariaDBServer(String hostname, int port, String catalog) {
        this(new MariaDBJDBCDriverDescriptor(), hostname, port, catalog);
    }

    protected MariaDBServer(JDBCDriverDescriptor<MariaDBDatabaseConnectionFactory> driverDescriptor, String hostname, int port, String catalog) {
        super(driverDescriptor, hostname, port, catalog);
    }

    @Override
    public DatabaseServerType getServerType() {
        return DatabaseServerTypes.MARIA_DB;
    }
}
