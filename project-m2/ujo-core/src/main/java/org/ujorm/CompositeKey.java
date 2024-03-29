/*
 *  Copyright 2007-2013 Pavel Ponec
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

package org.ujorm;

import java.util.List;
import org.ujorm.validator.ValidationException;

/**
 * A <strong>CompositeKey</strong> interface is a composite of more Key objects.
 * The CompositeKey class can be used wherever is used Key - with a one important <strong>exception</strong>:
 * do not send the CompositeKey object to methods Ujo.readValue(...) and Ujo.writeValue(...) directly!!!
 * <p/>There is prefered two methods UjoManager.setValue(...) / UjoManager.getValue(...)
 * to write and read a value instead of this - or use some type safe solution by UjoExt or a method of Key.
 * <p/>Note that method isDirect() returns a false in this class. For this reason, the property is not included
 * in the list returned by Ujo.readProperties().
 *
 * @author Pavel Ponec
 * @since 0.81
 */
@SuppressWarnings("deprecation")
public interface CompositeKey<UJO extends Ujo, VALUE> extends Key<UJO, VALUE> {

    /** Get the first property of the current object. The result is direct property always. */
    public <UJO_IMPL extends Ujo> Key<UJO_IMPL, VALUE> getLastKey();

    /** Get the first property of the current object. The result is direct property always. */
    public <UJO_IMPL extends Ujo> Key<UJO_IMPL, VALUE> getFirstKey();

    /** Export all <string>direct</strong> keys to the list from parameter. */
    public List<Key> exportKeys(List<Key> result);

    /**
     * It is a basic method for setting an appropriate type safe value to an Ujo object.
     * <br>The method calls a method
     * {@link Ujo#writeValue(org.ujorm.Key, java.lang.Object)}
     * always.
     * @param ujo Related Ujo object
     * @param value A value to assign.
     * @param createRelations create related UJO objects in case of the composite key
     * @throws ValidationException can be throwed from an assigned input validator{@Link Validator};
     * @see Ujo#writeValue(org.ujorm.Key, java.lang.Object)
     */
    public void setValue(final UJO ujo, final VALUE value, boolean createRelations) throws ValidationException;

    /** Get a penultimate value of this composite key.
     * If any value is {@code null}, then the result is {@code null}.
     * If the Composite key is a direct key than the ujo argument is send to the method result.
     * @param ujo base Ujo object
     * @param create create new instance of a related UJO object for an undefined ({@code null} case.
     * During the assigning the new relations are <strong>disabled</strong> all validators.
     */
    public Ujo getSemiValue(UJO ujo, boolean create);

    /** Returns a count of inner key items of this composite key
     * @see #exportKeys(java.util.List)
     */
    public int getCompositeCount();


}
