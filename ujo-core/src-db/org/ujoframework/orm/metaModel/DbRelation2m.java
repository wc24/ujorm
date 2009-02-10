/*
 *  Copyright 2009 Paul Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ujoframework.orm.metaModel;

import java.lang.reflect.Field;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.core.annot.Transient;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.orm.annot.Column;

/**
 * The database/object relation to many.
 * @author pavel
 */
public class DbRelation2m extends AbstractMetaModel {

    /** DB column name */
    //@XmlAttribute
    public static final UjoProperty<DbRelation2m,String> NAME = newProperty("name", "");
    /** UJO relation property */
    @Transient
    public static final UjoProperty<DbRelation2m,UjoProperty> PROPERTY = newProperty("property", UjoProperty.class);
    /** DB table */
    @Transient
    public static final UjoProperty<DbRelation2m,DbTable> TABLE = newProperty("table", DbTable.class);


    public DbRelation2m(DbTable table, UjoProperty propertyColumn) {
        
        Field field = UjoManager.getInstance().getPropertyField(DbTable.DB_RELATIVE.of(table).getItemType(), propertyColumn);
        Column column = field.getAnnotation(Column.class);

        if (column!=null) {
            NAME      .setValue(this, column.name());
        }
        if (true) {
            TABLE.setValue(this, table);
            PROPERTY.setValue(this, propertyColumn);
        }
        changeDefault(this, NAME, propertyColumn.getName());
    }

    /** Get property value */
    @SuppressWarnings("unchecked")
    final public Object getValue(Ujo ujo) {
        final Object result = PROPERTY.of(this).of(ujo);
        return result;
    }

    @Override
    public String toString() {
        return NAME.of(this);
    }

}
