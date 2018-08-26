package com.googlecode.jdbw.server.mysql;

import com.googlecode.jdbw.DatabaseServerTypes;
import com.googlecode.jdbw.impl.AuthenticatingDatabaseConnectionFactory;

/**
 * Connection factory for MariaDB with additional connection options
 * @author Martin Berglund
 */
public class MariaDBDatabaseConnectionFactory extends AuthenticatingDatabaseConnectionFactory {
    MariaDBDatabaseConnectionFactory(String jdbcUrl) {
        super(DatabaseServerTypes.MARIA_DB, jdbcUrl);
        setUseUnicode(true);
        setCharacterEncoding("utf8");
        setRewriteBatchedStatements(true);
        setContinueBatchOnError(false);
        setAllowMultiQueriesboolean(false);
        setUseCompression(true);
        setZeroDateTimeBehavior("convertToNull");
    }

    @Override
    public MariaDBDatabaseConnectionFactory setUsername(String username) {
        super.setUsername(username);
        return this;
    }

    @Override
    public MariaDBDatabaseConnectionFactory setPassword(String password) {
        super.setPassword(password);
        return this;
    }

    public final MariaDBDatabaseConnectionFactory setUseUnicode(boolean useUnicode) {
        setConnectionProperty("useUnicode", useUnicode + "");
        return this;
    }

    public final MariaDBDatabaseConnectionFactory setCharacterEncoding(String characterEncoding) {
        setConnectionProperty("characterEncoding", characterEncoding);
        return this;
    }

    public final MariaDBDatabaseConnectionFactory setRewriteBatchedStatements(boolean rewriteBatchedStatements) {
        setConnectionProperty("rewriteBatchedStatements", rewriteBatchedStatements + "");
        return this;
    }

    public final MariaDBDatabaseConnectionFactory setContinueBatchOnError(boolean continueBatchOnError) {
        setConnectionProperty("continueBatchOnError", continueBatchOnError + "");
        return this;
    }

    public final MariaDBDatabaseConnectionFactory setAllowMultiQueriesboolean(boolean allowMultiQueries) {
        setConnectionProperty("allowMultiQueries", allowMultiQueries + "");
        return this;
    }

    public final MariaDBDatabaseConnectionFactory setUseCompression(boolean useCompression) {
        setConnectionProperty("useCompression", useCompression + "");
        return this;
    }

    public final MariaDBDatabaseConnectionFactory setZeroDateTimeBehavior(String zeroDateTimeBehavior) {
        setConnectionProperty("zeroDateTimeBehavior", zeroDateTimeBehavior);
        return this;
    }
}
