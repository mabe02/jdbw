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
package com.googlecode.jdbw.orm.jdbc;

import com.googlecode.jdbw.orm.Identifiable;
import com.googlecode.jdbw.orm.Modifiable;
import java.util.Map;

class UpdatableObjectProxyHandler<U, T extends Identifiable<U> & Modifiable> extends ModifiableObjectProxyHandler<U, T> {

    public UpdatableObjectProxyHandler(FieldMapping fieldMapping, Class<T> objectType, U key, Map<String, Object> initialValues) {
        super(fieldMapping, objectType, key, initialValues);
    }

    @Override
    protected void setValue(String fieldName, Object value) {
        if("id".equals(fieldName)) {
            throw new IllegalArgumentException("Cannot re-assign the id");
        }
        super.setValue(fieldName, value);
    }
    
    static class Finalized<U, T extends Identifiable<U> & Modifiable> extends ModifiableObjectProxyHandler.Finalized<U, T> {
        public Finalized(Class<T> objectType, U id, Object[] values) {
            super(objectType, id, values);
        }
    }    
}