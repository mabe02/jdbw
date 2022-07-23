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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Special MariaDB SQL executor that makes sure results are coming back streaming
 * (instead of pre-caching the whole result) and also disables generated keys
 * retrieval for batch queries (this causes errors otherwise).
 * 
 * @author Martin Berglund
 */
class MariaDBExecutor extends MySQLExecutor {

    MariaDBExecutor(Connection connection) {
        super(connection);
    }

        @Override
    protected PreparedStatement prepareBatchUpdateStatement(String SQL) throws SQLException {
        return connection.prepareStatement(SQL, Statement.NO_GENERATED_KEYS);
    }

    @Override
    protected boolean isBatchWriteGeneratedKeyRetrievalAvailable() {
        return false;
    }
}
