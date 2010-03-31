/*
 *  Copyright 2009-2010 Pavel Ponec
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

import org.ujoframework.core.annot.Transient;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.extensions.Property;


/**
 * DB index metamodel.
 * @author Pavel Ponec
 */
final public class MetaIndex extends AbstractMetaModel {
    private static final Class CLASS = MetaIndex.class;

    /** Index database name */
    @Transient
    public static final Property<MetaIndex,String> NAME = newProperty("name", String.class);
    /** Table */
    @Transient
    public static final Property<MetaIndex,MetaTable> TABLE = newProperty("table", MetaTable.class);
    /** Is the index unique ? */
    @Transient
    public static final Property<MetaIndex,Boolean> UNIQUE = newProperty("unique", true);
    /** Table Columns */
    @Transient
    public static final ListProperty<MetaIndex,MetaColumn> COLUMNS = newListProperty("column", MetaColumn.class);

    /** The property initialization */
    static{init(CLASS);}

    public MetaIndex(String index, MetaTable table) {
        NAME.setValue(this, index);
        TABLE.setValue(this, table);
    }

    /** Show an index name + table */
    @Override
    public String toString() {
        final String result = NAME.of(this)
            + " ["
            + COLUMNS.getItemCount(this)
            + "] of the table: "
            + get(TABLE).get(MetaTable.NAME)
            ;
        return result;
    }




}