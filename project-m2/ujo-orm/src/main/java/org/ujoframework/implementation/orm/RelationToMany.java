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

package org.ujoframework.implementation.orm;

import org.ujoframework.core.UjoIterator;
import org.ujoframework.extensions.Property;

/**
 * The relation 1:N to another UJO type items
 * @author Pavel Ponec
 * @see org.ujoframework.core.UjoIterator
 */
public class RelationToMany<UJO extends OrmTable, ITEM extends OrmTable>
    extends Property<UJO, UjoIterator<ITEM>>
{

    private final Class<ITEM> itemType;

    /** Constructor */
    @SuppressWarnings("unchecked")
    public RelationToMany(String name, Class<ITEM> itemType) {
        init(name, (Class) UjoIterator.class, null, -1, false);
        this.itemType = itemType;
    }

    /** Constructor
     * @param name Property name.
     * @param itemType The type of item.
     * @param index An property order
     */
    @SuppressWarnings("unchecked")
    public RelationToMany(String name, Class<ITEM> itemType, int index, boolean lock) {
        init(name, (Class) UjoIterator.class, null, index, lock);
        this.itemType = itemType;
    }


    /** Returns ItemType */
    public Class<ITEM> getItemType() {
        return itemType;
    }


}