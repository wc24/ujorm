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

import org.ujorm.core.KeyRing;
import org.ujorm.core.annot.Immutable;
import org.ujorm.validator.ValidationException;

/**
 * This interface is a descriptor of the {@link Ujo} attribute. The property contains only property meta-data
 * and therefore the UjoPropertry implementation never contains business data. 
 * Each instance of the Key must be located in the {@code public static final} field of some Ujo implementation.
 * The Key can't have a serializable feature never, because its instance is the unique for a related java field.
 * An appropriate solution solution for serialization is to use a decorator class KeyRing.
 * <br>See a <a href="package-summary.html#UJO">general information</a> about current framework or see some implementations.
 *
 * @author Pavel Ponec
 * @see Ujo
 * @opt attributes
 * @opt operations
 * @see KeyRing
 */
@Immutable
@SuppressWarnings("deprecation")
public interface Key <UJO extends Ujo,VALUE> extends CharSequence, Comparable<Key>, CriterionProvider<UJO,VALUE> {

    /** Returns a name of the Key. */
    public String getName();

    /** Returns a class of the current property. */
    public Class<VALUE> getType();

    /** Returns a class of the domain Ujo object. */
    public Class<UJO> getDomainType();

    /** Returns a container of the Key field. */
    // public Class<?> getContainerType(); // TODO (?)

    /**
     * It is a basic method for setting an appropriate type safe value to an Ujo object.
     * <br>The method calls a method
     * {@link Ujo#writeValue(org.ujorm.Key, java.lang.Object)}
     * always.
     * @param ujo Related Ujo object
     * @param value A value to assign.
     * @throws ValidationException can be throwed from an assigned input validator{@Link Validator};
     * @see Ujo#writeValue(org.ujorm.Key, java.lang.Object)
     */
    public void setValue(UJO ujo, VALUE value) throws ValidationException;

    /**
     * TODO: Is it really the good idea to extend the interface with this method ?
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
//    public void setValue(UJO ujo, VALUE value, boolean createRelations) throws ValidationException;

    /**
     * A shortcut for the method {@link #of(org.ujorm.Ujo)}.
     * @see #of(Ujo)
     */
    public VALUE getValue(UJO ujo);

    /**
     * It is a basic method for getting an appropriate type safe value from an Ujo object.
     * <br>The method calls a method
     * {@link Ujo#writeValue(org.ujorm.Key, java.lang.Object)}
     * always.
     * <br>Note: this method replaces the value of <strong>null</strong> by default
     * @param ujo If a NULL parameter is used then an exception NullPointerException is throwed.
     * @return Returns a type safe value from the ujo object.
     * @see Ujo#readValue(Key)
     * @see #getValue(org.ujorm.Ujo)
     */
    public VALUE of(UJO ujo);


//    /**
//     * Similar function like getValue(UJO), however in case a null parameter is used so the result value is null and no NullPointerExeption is throwed.
//     * @param ujo If a null parameter is used then the null value is returned.
//     * @return Returns a type safe value from the ujo object.
//     * @see #getValue(Ujo)
//     */
//    public VALUE takeFrom(UJO ujo);

    /** Returns a property index or value -1 if the property index is not defined.
     * <br>The index is reasonable for an implementation an <code>ArrayUjo</code> class and the value is used is used
     * <br>for a sorting of Keys in a method <code>UjoManager.readProperties(Class type)</code> .
     * @see org.ujorm.implementation.array.ArrayUjo
     * @see org.ujorm.core.UjoManager#readProperties(Class)
     */
    public int getIndex();

    /** Method returns a default value for substitution of the <code>null</code> value for the current property.
     * The feature is purposeful only if the default value is not <code>null</code> and a propert value is <code>null</code> .
     * @see Ujo#readValue(Key)
     */
    public VALUE getDefault();


    /** Indicates whether a parameter value of the ujo "equal to" this property default value. */
    public boolean isDefault(UJO ujo);

    /**
     * Returns true, if the property value equals to a parameter value. The property value can be null.
     *
     * @param ujo A basic Ujo.
     * @param value Null value is supported.
     * @return Accordance
     */
    public boolean equals(UJO ujo, VALUE value);

    /**
     * Returns true, if the property name equals to the parameter value.
     * @param name The name of a property
     */
    public boolean equalsName(CharSequence name);

    /**
     * If the property is the direct property of the related UJO class then method returns the TRUE value.
     * The return value false means, that property is type of {@link CompositeKey}.
     * <br />
     * Note: The composite keys are excluded from from function Ujo.readProperties() by default
     * and these keys should not be sent to methods Ujo.writeValue() and Ujo.readValue().
     * @see CompositeKey
     * @since 0.81
     * @deprecated use rather a negation of the method {@link #isComposite() }
     */
    @Deprecated
    public boolean isDirect();

    /**
     * The composite key is an instance of CompositeKey.
     * It this key is the a direct key of a related UJO class then this method returns the TRUE value.
     * All composite keys are excluded from from list {@link Ujo#readKeys()} by default
     * <br />
     * Note:
     * and these keys should not be sent to methods Ujo.writeValue() and Ujo.readValue().
     * @see CompositeKey
     * @since 1.36
     */
    public boolean isComposite();

    /** Returns true if the property type is a type or subtype of the parameter class. */
    public boolean isTypeOf(Class type);

    /** A flag for an ascending direction of sorting. It is recommended that the default result was true.
     * @since 0.85
     * @see org.ujorm.core.UjoComparator
     */
    public boolean isAscending();

    /** Create new instance of an <strong>indirect</strong> property with the descending direction of sorting.
     * @return returns a new instance of the indirect Key
     * @since 0.85
     * @see #isAscending()
     * @see org.ujorm.core.UjoComparator
     */
    public Key<UJO,VALUE> descending();

    /** Create new instance of an <strong>indirect</strong> property with the descending direction of sorting.
     * @return returns a new instance of the indirect Key
     * @since 1.21
     * @see #isAscending()
     * @see org.ujorm.core.UjoComparator
     */
    public Key<UJO,VALUE> descending(boolean descending);

    /** Get the ujorm key validator or return the {@code null} value if no validator was assigned */
    public Validator<VALUE> getValidator();

    /** Create new composite (indirect) instance of the {@link  Key}.
     * @since 0.92
     */
    public <T> CompositeKey<UJO, T> add(Key<? super VALUE, T> key);

    /** Create new composite (indirect) instance of the {@link  Key}.
     * @since 1.36
     */
    public <T> ListKey<UJO, T> add(ListKey<? super VALUE, T> key);

    /** Copy a value from the first UJO object to second one. A null value is not replaced by the default. */
    public void copy(UJO from, UJO to);

    /** Compare to another Key object by the index and name of the property.
     * @since 1.20
     */
    @Override
    public int compareTo(Key p);

    /** Returns the name of the Key without domain class.
     * <br />Example: ID */
    @Override
    public String toString();

    /** Returns the full name of the Key including a simple domain class.
     * <br />Example: Person.ID */
    public String toStringFull();

    /**
     * Returns the full name of the Key including all atributes. 
     * <br />Example: Person.id {index=0, ascending=false, ...}
     * @param extended argumenta false calls the method {@link #toStringFull()} only.
     * @return the full name of the Key including all atributes.
     */
    public String toStringFull(boolean extended);

}
