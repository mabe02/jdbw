package com.googlecode.jdbw.impl;

import com.googlecode.jdbw.ResultSetInformation;
import com.googlecode.jdbw.metadata.Column;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of ResultSetInformation
 * @author Martin Berglund
 */
public class ResultSetInformationImpl implements ResultSetInformation {

    private final ResultSetMetaData resultSetMetaData;
    private final List<String> columnLabels;
    private final List<Integer> columnTypes;
    private final List<Column> columns;
    private final int resultSetIndex;

    /**
     * Creates a new result set information object by providing a {@code ResultSetMetaData} and an index count
     * @param resultSetMetaData The result set meta data from JDBC that will be used to derive all the information about
     *                          the result set
     * @param index             Which index the result set being inspect had, where 0 is the first result set being
     *                          returned by a query
     * @throws SQLException     If there was a database error while trying to derive the information about the result
     *                          set
     */
    public ResultSetInformationImpl(ResultSetMetaData resultSetMetaData, int index) throws SQLException {
        this.resultSetMetaData = resultSetMetaData;
        this.resultSetIndex = index;
        this.columnLabels = new ArrayList<String>(resultSetMetaData.getColumnCount());
        this.columnTypes = new ArrayList<Integer>(resultSetMetaData.getColumnCount());
        this.columns = new ArrayList<Column>(resultSetMetaData.getColumnCount());

        for(int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
            String columnLabel = extractColumnLabel(resultSetMetaData, i);
            int columnType = extractColumnType(resultSetMetaData, i);
            
            columnLabels.add(columnLabel);
            columnTypes.add(columnType);
            columns.add(new Column(
                            i, 
                            columnLabel, 
                            columnType,
                            extractColumnTypeName(resultSetMetaData, i),
                            extractColumnDisplaySize(resultSetMetaData, i),
                            extractScale(resultSetMetaData, i),
                            extractNullable(resultSetMetaData, i), 
                            extractAutoIncrement(resultSetMetaData, i)) {});
        }
    }

    protected String extractColumnLabel(ResultSetMetaData resultSetMetaData1, int i) throws SQLException {
        return resultSetMetaData1.getColumnLabel(i);
    }

    protected int extractColumnType(ResultSetMetaData resultSetMetaData1, int i) throws SQLException {
        return resultSetMetaData1.getColumnType(i);
    }

    protected String extractColumnTypeName(ResultSetMetaData resultSetMetaData1, int i) throws SQLException {
        return resultSetMetaData1.getColumnTypeName(i);
    }

    protected int extractColumnDisplaySize(ResultSetMetaData resultSetMetaData1, int i) throws SQLException {
        return resultSetMetaData1.getColumnDisplaySize(i);
    }

    protected int extractScale(ResultSetMetaData resultSetMetaData1, int i) throws SQLException {
        return resultSetMetaData1.getScale(i);
    }

    protected int extractNullable(ResultSetMetaData resultSetMetaData1, int i) throws SQLException {
        return resultSetMetaData1.isNullable(i);
    }

    protected String extractAutoIncrement(ResultSetMetaData resultSetMetaData1, int i) throws SQLException {
        return resultSetMetaData1.isAutoIncrement(i) ? "YES" : "NO";
    }

    @Override
    public ResultSetMetaData getResultSetMetaData() {
        return resultSetMetaData;
    }

    @Override
    public int getResultSetIndex() {
        return resultSetIndex;
    }

    @Override
    public int getNumberOfColumns() {
        return columnLabels.size();
    }

    @Override
    public String getColumnLabel(int columnIndex) throws ArrayIndexOutOfBoundsException {
        return columnLabels.get(columnIndex);
    }

    @Override
    public List<String> getColumnLabels() {
        return Collections.unmodifiableList(columnLabels);
    }

    @Override
    public int getColumnSQLType(int columnIndex) throws ArrayIndexOutOfBoundsException {
        return columnTypes.get(columnIndex);
    }

    /**
     * Returns the underlying column list for this result set
     * @return List of all columns in the result set this information object is describing
     */
    public List<Column> getColumns() {
        return Collections.unmodifiableList(columns);
    }
}
