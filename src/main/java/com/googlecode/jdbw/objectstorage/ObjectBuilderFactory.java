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
import java.util.Map;

public interface ObjectBuilderFactory {
    public <K, O extends Storable<K>, B extends ObjectBuilder<O>> B newClone(Class<B> builderType, O object);
    public <K, O extends Storable<K>, B extends ObjectBuilder<O>> B newObject(Class<B> builderType, K key);
    public <K, O extends Storable<K>, B extends ObjectBuilder<O>> B newObject(Class<B> builderType, K key, O template);
    public <K, O extends Storable<K>, B extends ObjectBuilder<O>> B newObject(Class<B> builderType, K key, Map<String, Object> initialValues);
    @SuppressWarnings("unchecked")
    public <K, O extends Storable<K>, B extends ObjectBuilder<O>> List<B> newObjects(Class<B> builderType, K... keys);
    public <K, O extends Storable<K>, B extends ObjectBuilder<O>> List<B> newObjects(Class<B> builderType, Collection<K> keys);
}
