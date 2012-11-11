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
package com.googlecode.jdbw.orm;

import java.util.List;

public abstract class AbstractExternalObjectStorage extends AbstractObjectStorage implements ExternalObjectStorage {
         
    private final CachePolicy defaultCachePolicy;

    public AbstractExternalObjectStorage() {
        this(CachePolicy.EXTERNAL_GET);
    }

    public AbstractExternalObjectStorage(CachePolicy defaultCachePolicy) {
        this.defaultCachePolicy = defaultCachePolicy;
    }
    
    @Override
    public <U, T extends Identifiable<U>> T get(Class<T> type, U key) {
        return get(type, key, defaultCachePolicy);
    }

    @Override
    public <U, T extends Identifiable<U>> List<T> getAll(Class<T> type) {
        return getAll(type, defaultCachePolicy);
    }
}
