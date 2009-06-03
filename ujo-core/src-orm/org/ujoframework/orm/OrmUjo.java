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

package org.ujoframework.orm;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;

/**
 * The OrmUjo is a basic interface of the persistent object in this ORM-UJO module.
 * A class that implements the interface must have got a special feature: reference to a foreign BO
 * must be able to store an object of any type by the method Ujo.writeProperty(...).
 * This feature is necessary for the proper functioning of the lazy initialization.
 * @author Ponec
 */
public interface OrmUjo extends Ujo {

    /** Read an ORM session */
    public Session readSession();

    /** Write an ORM session */
    public void writeSession(Session session);

    /**
     * Returns changed properties.
     * @param clear True value clears all the property changes.
     */
    public UjoProperty[] readChangedProperties(boolean clear);

    /** A special implementation, see a code in the TableUjo class for more information. */
    @Override
    public Object readValue(final UjoProperty property);

}