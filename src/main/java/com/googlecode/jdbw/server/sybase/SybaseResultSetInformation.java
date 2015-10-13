package com.googlecode.jdbw.server.sybase;

import com.googlecode.jdbw.impl.ResultSetInformationImpl;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 *
 * @author mberglun
 */
class SybaseResultSetInformation extends ResultSetInformationImpl {
    public SybaseResultSetInformation(ResultSetMetaData resultSetMetaData, int index) throws SQLException {
        super(resultSetMetaData, index);
    }

    @Override
    protected String extractColumnTypeName(ResultSetMetaData resultSetMetaData1, int i) throws SQLException {
        return "<UNSUPPORTED>";
    }
}
