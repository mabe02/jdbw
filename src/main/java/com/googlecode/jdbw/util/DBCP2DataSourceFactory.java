package com.googlecode.jdbw.util;

import com.googlecode.jdbw.DataSourceFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import javax.sql.DataSource;

/**
 * This utility class will allow you to use JDBW with Apache DBCP2. Please note that JDBW doesn't have a dependency on
 * dbcp2 so you need to add that yourself for this class (and the connection pool) to work.
 * @author martin
 */
public class DBCP2DataSourceFactory implements DataSourceFactory {
    @Override
    public DataSource newDataSource(String url, Properties properties) {
        try {
            Class<? extends DataSource> basicDataSourceClass = (Class<? extends DataSource>)Class.forName("org.apache.commons.dbcp2.BasicDataSource");
            DataSource ds = (DataSource)basicDataSourceClass.newInstance();
            Method setUrlMethod = basicDataSourceClass.getMethod("setUrl", String.class);
            Method addConnectionPropertyMethod = basicDataSourceClass.getMethod("addConnectionProperty", String.class, String.class);
            
            setUrlMethod.invoke(ds, url);
            for(String property : properties.stringPropertyNames()) {
                addConnectionPropertyMethod.invoke(ds, property, properties.getProperty(property));
            }
            return ds;
        }
        catch(Exception e) {
            throw new IllegalStateException("Problems loading DBCP2 classes, please make sure dbcp2 is on the class path", e);
        }
    }

    @Override
    public void close(DataSource previouslyConstructedDataSource) { 
        try {
            Method closeMethod = previouslyConstructedDataSource.getClass().getMethod("close");
            closeMethod.invoke(previouslyConstructedDataSource);
        }
        catch(InvocationTargetException ignore) {
        }
        catch(Exception e) {
            throw new IllegalStateException("Problems closing DBCP2 BasicDataSource, please make sure the correct dbcp2 jar is on the class path", e);
        }
    }
}
