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
 * Copyright (C) 2009-2012 mabe02
 */
package com.googlecode.jdbw.metadata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mabe02
 */
public class Catalog implements Comparable<Catalog> {

    private final MetaDataResolver metaDataResolver;
    private final String name;

    public Catalog(MetaDataResolver metaDataResolver, String name) {
        this.metaDataResolver = metaDataResolver;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Schema> getSchemas() throws SQLException {
        List<String> schemaNames = metaDataResolver.getSchemaNames(name);
        List<Schema> schemas = new ArrayList<Schema>();
        for(String schemaName : schemaNames) {
            schemas.add(metaDataResolver.getMetaDataFactory().createSchema(this, schemaName));
        }
        return schemas;
    }

    public Schema getSchema(String schemaName) throws SQLException {
        List<Schema> schemas = getSchemas();
        for(Schema schema : schemas) {
            if(schema.getName().equals(schemaName)) {
                return schema;
            }
        }
        return null;
    }

    @Override
    public int compareTo(Catalog o) {
        return getName().toLowerCase().compareTo(o.getName().toLowerCase());
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || obj instanceof Catalog == false) {
            return false;
        }

        Catalog other = (Catalog) obj;
        return metaDataResolver == other.metaDataResolver
                && getName().equals(other.getName());
    }
}
