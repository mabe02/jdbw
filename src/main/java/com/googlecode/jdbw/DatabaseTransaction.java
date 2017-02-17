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
package com.googlecode.jdbw;

import java.sql.SQLException;

/**
 * Representing a ongoing database transaction. Remember that it is important that you either commit() or rollback() the
 * transaction when you are done with it. Remember this applies even when handling exceptions, you must declare yourself
 * done with the transaction so that the connection can be returned to there original data source. Failure to do so may
 * starve the connection pool.
 * <p>
 * Once you have committed or rolled back the transaction, this transaction object cannot be used anymore, you must ask
 * for a new one.
 *
 * @author Martin Berglund
 */
public interface DatabaseTransaction extends SQLExecutor {

    /**
     * This will commit the transaction on the remote server and the connection will be returned to the pool. This
     * object cannot be used again after calling this method.
     * @throws SQLException If there was an error when trying to commit the transaction
     */
    void commit() throws SQLException;

    /**
     * This will rollback the transaction on the remote server and the connection will be returned to the pool. This
     * object cannot be used again after calling this method.
     * @throws SQLException If there was an error when trying to rollback the transaction
     */
    void rollback() throws SQLException;
}
