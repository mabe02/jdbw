package com.googlecode.jdbw.server.postgresql;

import com.googlecode.jdbw.server.DefaultSQLDialect;

/**
 * PostgreSQL dialect definition
 * @author martin
 */
public class PostgreSQLDialect extends DefaultSQLDialect {
    @Override
    public String getUseCatalogStatement(String catalogName) {
        return null;
    }
}
