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
package com.googlecode.jdbw.objectstorage.impl;

import com.googlecode.jdbw.DatabaseConnection;
import com.googlecode.jdbw.DatabaseTransaction;
import com.googlecode.jdbw.TransactionIsolation;
import com.googlecode.jdbw.objectstorage.AbstractObjectStorage;
import com.googlecode.jdbw.objectstorage.FieldMapping;
import com.googlecode.jdbw.objectstorage.ObjectBuilderFactory;
import com.googlecode.jdbw.objectstorage.ObjectFactory;
import com.googlecode.jdbw.objectstorage.ObjectStorageException;
import com.googlecode.jdbw.objectstorage.Storable;
import com.googlecode.jdbw.objectstorage.TableMapping;
import com.googlecode.jdbw.objectstorage.TableMappingFactory;
import com.googlecode.jdbw.util.BatchUpdateHandlerAdapter;
import com.googlecode.jdbw.util.SQLWorker;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JDBCObjectStorage extends AbstractObjectStorage {

    private final DatabaseConnection databaseConnection;
    private final TableMappingFactory tableMappingFactory;
    private final ObjectFactory objectFactory;
    private final ConcurrentHashMap<Class, TableMapping> tableMappings;

    public JDBCObjectStorage(DatabaseConnection databaseConnection) {
        this(databaseConnection, new DefaultTableMappingFactory());
    }

    public JDBCObjectStorage(DatabaseConnection databaseConnection, TableMappingFactory tableMappingFactory) {
        this(databaseConnection, tableMappingFactory, new ImmutableObjectFactory());
    }

    public JDBCObjectStorage(
            DatabaseConnection databaseConnection, 
            TableMappingFactory tableMappingFactory, 
            ObjectFactory objectFactory) {
        
        this.databaseConnection = databaseConnection;
        this.tableMappingFactory = tableMappingFactory;
        this.objectFactory = objectFactory;
        this.tableMappings = new ConcurrentHashMap<Class, TableMapping>();
    }

    public <O extends Storable> void register(Class<O> objectType) {
        tableMappings.putIfAbsent(objectType, tableMappingFactory.createTableMapping(objectType));
    }

    public ObjectBuilderFactory getBuilderFactory() {
        return new DefaultObjectBuilderFactory() {
            @Override
            protected FieldMapping getFieldMapping(Class<? extends Storable> objectType) {
                if(tableMappings.contains(objectType)) {
                    return tableMappings.get(objectType);
                }
                else {
                    return super.getFieldMapping(objectType);
                }
            }
        };
    }

    public <K, O extends Storable<K>> List<O> getSome(Class<O> type, Collection<K> ids) {
        if(!tableMappings.containsKey(type)) {
            throw new IllegalArgumentException("Cannot call JDBCObjectStorage.getSome(...) non-registered type " + type.getSimpleName());
        }
        
        TableMapping tableMapping = tableMappings.get(type);        
        String sql = tableMapping.getSelectSome(
                databaseConnection.getServerType().getSQLDialect(),
                ids.size());
        List<Object[]> rows;
        try {
            Object[] keysAsArray = ids.toArray();
            rows = new SQLWorker(databaseConnection.createAutoExecutor()).query(sql, keysAsArray);
        }
        catch(SQLException e) {
            throw new ObjectStorageException("Database error when calling JDBCObjectStorage.getSome(...) with {type=" +
                    type + "} and {ids=" + ids + "}", e);
        }
        return transform(type, tableMapping, rows);
    }

    public <O extends Storable> List<O> getAll(Class<O> type) {
        if(!tableMappings.containsKey(type)) {
            throw new IllegalArgumentException("Cannot call JDBCObjectStorage.getAll(...) non-registered type " + type.getSimpleName());
        }
        TableMapping tableMapping = tableMappings.get(type);        
        String sql = tableMapping.getSelectAll(databaseConnection.getServerType().getSQLDialect());
        List<Object[]> rows;
        try {
            rows = new SQLWorker(databaseConnection.createAutoExecutor()).query(sql);
        }
        catch(SQLException e) {
            throw new ObjectStorageException("Database error when calling JDBCObjectStorage.getAll(...) with {type=" + type + "}", e);
        }
        return transform(type, tableMapping, rows);
    }

    public <O extends Storable> int getSize(Class<O> type) {
        if(!tableMappings.containsKey(type)) {
            throw new IllegalArgumentException("Cannot call JDBCObjectStorage.getSize(...) non-registered type " + type.getSimpleName());
        }        
        String sql = tableMappings.get(type).getSelectCount(databaseConnection.getServerType().getSQLDialect());
        int count;
        try {
            count = new SQLWorker(databaseConnection.createAutoExecutor()).topLeftValueAsInt(sql);
        }
        catch(SQLException e) {
            throw new ObjectStorageException("Database error when calling JDBCObjectStorage.getSize(...) with {type=" + type + "}", e);
        }
        return count;
    }

    public <O extends Storable> O put(O object) {
        return putAll(object).get(0);
    }

    @Override
    public <O extends Storable> List<O> putAll(Collection<O> objects) {
        Class<O> objectType = null;
        objects = Utils.removeNullElements(new ArrayList(objects));
        if(objects.isEmpty()) {
            return Collections.emptyList();
        }
        for(O object: objects) {
            objectType = getStorableTypeFromObject(object);
            break;
        }
        if(!tableMappings.containsKey(objectType)) {
            throw new IllegalArgumentException("Cannot call JDBCObjectStorage.putAll(...) non-registered type " + objectType.getSimpleName());
        }
        
        DatabaseTransaction transaction = null;
        try {
            TableMapping tableMapping = tableMappings.get(objectType);        
            String sql = tableMapping.getSelectKeys(databaseConnection.getServerType().getSQLDialect(), objects.size());
            Object[] allKeys = new Object[objects.size()];
            int count = 0;
            for(O object: objects) {
                allKeys[count++] = object.getId();
            }
            
            transaction = databaseConnection.beginTransaction(TransactionIsolation.SERIALIZABLE);
            SQLWorker worker = new SQLWorker(transaction);
            Set<Object> existingRows = new HashSet<Object>(worker.leftColumn(sql, allKeys));
            
            List<O> toBeUpdated = new ArrayList<O>();
            List<O> toBeInserted = new ArrayList<O>();
            for(O object: objects) {
                if(existingRows.contains(object.getId())) {
                    toBeUpdated.add(object);
                }
                else {
                    toBeInserted.add(object);
                }
            }
            if(!toBeInserted.isEmpty()) {
                sql = tableMapping.getInsert(databaseConnection.getServerType().getSQLDialect());
                List<Object[]> batch = new ArrayList<Object[]>();
                for(O o: toBeInserted) {
                    batch.add(transform(tableMapping, o));
                }
                transaction.batchWrite(new BatchUpdateHandlerAdapter(), sql, batch);
            }
            if(!toBeUpdated.isEmpty()) {
                sql = tableMapping.getUpdate(databaseConnection.getServerType().getSQLDialect());
                List<Object[]> batch = new ArrayList<Object[]>();
                for(O o: toBeInserted) {
                    batch.add(transform(tableMapping, o, false));
                }
                transaction.batchWrite(new BatchUpdateHandlerAdapter(), sql, batch);
            }
            transaction.commit();
        }
        catch(SQLException e) {
            throw new ObjectStorageException("Database error when calling JDBCObjectStorage.putAll(...) with {type=" +
                    objectType + "} and {objects=" + objects + "}", e);
        }
        return null;
    }

    public <K, O extends Storable<K>> void remove(Class<O> objectType, Collection<K> ids) {
        if(!tableMappings.containsKey(objectType)) {
            throw new IllegalArgumentException("Cannot call JDBCObjectStorage.remove(...) non-registered type " + objectType.getSimpleName());
        }
        
        String sql = tableMappings.get(objectType).getDelete(
                databaseConnection.getServerType().getSQLDialect(),
                ids.size());
        try {
            Object[] keysAsArray = ids.toArray();
            new SQLWorker(databaseConnection.createAutoExecutor()).write(sql, keysAsArray);
        }
        catch(SQLException e) {
            throw new ObjectStorageException("Database error when calling JDBCObjectStorage.remove(...) with {type=" +
                    objectType + "} and {ids=" + ids + "}", e);
        }
    }

    public <O extends Storable> void removeAll(Class<O> objectType) {
        if(!tableMappings.containsKey(objectType)) {
            throw new IllegalArgumentException("Cannot call JDBCObjectStorage.getAll(...) non-registered type " + objectType.getSimpleName());
        }
        
        String sql = tableMappings.get(objectType).getDeleteAll(databaseConnection.getServerType().getSQLDialect());
        try {
            new SQLWorker(databaseConnection.createAutoExecutor()).write(sql);
        }
        catch(SQLException e) {
            throw new ObjectStorageException("Database error when calling JDBCObjectStorage.removeAll(...) with {type=" + objectType + "}", e);
        }
    }

    @Override
    protected <O extends Storable> Class<O> getStorableTypeFromObject(O object) throws ObjectStorageException {
        Class<O> type = super.getStorableTypeFromObject(object);
        if(type != null) {
            return type;
        }
        
        //Custom detection
        Class candidate = (Class)object.getClass();
        if(tableMappings.contains(candidate)) {
           type = candidate; 
        }
        else if(object instanceof Proxy) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(object);
            if(invocationHandler instanceof ObjectProxyHandler) {
                type = (Class)((ObjectProxyHandler)invocationHandler).getFieldMapping().getObjectType();
            }
        }        
        return type;
    }
    
    private <O extends Storable> List<O> transform(Class<O> type, FieldMapping fieldMapping, List<Object[]> rows) {
        List<O> result = new ArrayList<O>();
        for(Object[] row: rows) {
            result.add(objectFactory.newObject(type, fieldMapping, row));
        }
        return result;
    }
    
    private <O extends Storable> Object[] transform(FieldMapping fieldMapping, O object) {
        return transform(fieldMapping, object, true);
    }
    
    private <O extends Storable> Object[] transform(FieldMapping fieldMapping, O object, boolean idAtFirst) {
        Object[] result = new Object[fieldMapping.getFieldNames().size() + 1];
        if(idAtFirst) {
            result[0] = object.getId();
        }
        for(Method method: fieldMapping.getObjectType().getMethods()) {
            if(fieldMapping.getFieldName(method) == null || "getId".equals(method.getName())) {
                continue;
            }
            try {
                method.setAccessible(true);
                result[fieldMapping.getFieldIndex(method) + (idAtFirst ? 1 : 0)] = method.invoke(object);
            }
            catch(Exception e) {
                throw new ObjectStorageException("Failed transform, couldn't copy value from object due to " + e.getClass().getSimpleName(), e);
            }
        }
        if(!idAtFirst) {
            result[result.length - 1] = object.getId();
        }
        return result;
    }
}