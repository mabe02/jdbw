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
package com.googlecode.jdbw.objectstorage;

import java.util.Collection;
import java.util.List;

public interface ObjectStorage {

    <O extends Storable<?>> void register(Class<O> objectType);
    
    ObjectBuilderFactory getBuilderFactory();
    
    <K, O extends Storable<K>> boolean contains(O object);
    
    <K, O extends Storable<K>> boolean contains(Class<O> type, K id);

    <K, O extends Storable<K>> O get(Class<O> type, K id);

    @SuppressWarnings("unchecked")
    <K, O extends Storable<K>> List<O> getSome(Class<O> type, K... ids);
    
    <K, O extends Storable<K>> List<O> getSome(Class<O> type, Collection<K> ids);

    <K, O extends Storable<K>> List<O> getAll(Class<O> type);
    
    <O extends Storable<?>> int getSize(Class<O> type);

    <K, O extends Storable<K>> O put(O object);

    @SuppressWarnings("unchecked")
    <K, O extends Storable<K>> List<O> putAll(O... objects);

    <K, O extends Storable<K>> List<O> putAll(Collection<O> objects);

    @SuppressWarnings("unchecked")
    <K, O extends Storable<K>> void remove(O... objects);

    <K, O extends Storable<K>> void remove(Collection<O> objects);

    @SuppressWarnings("unchecked")
    <K, O extends Storable<K>> void remove(Class<O> objectType, K... ids);
    
    <K, O extends Storable<K>> void remove(Class<O> objectType, Collection<K> ids);
    
    <K, O extends Storable<K>> void removeAll(Class<O> objectType);
}
