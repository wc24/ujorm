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

package org.ujorm.extensions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.Validator;
import org.ujorm.core.annot.Immutable;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.Operator;
import org.ujorm.criterion.ValueCriterion;
import org.ujorm.validator.ValidationException;

/**
 * A <strong>PathProperty</strong> class is an composite of a Key objects.
 * The PathProperty class can be used wherever is used Key - with a one important <strong>exception</strong>:
 * do not send the PathProperty object to methods Ujo.readValue(...) and Ujo.writeValue(...) !!!
 * <p/>Note that method isDirect() returns a false in this class. For this reason, the property is not included
 * in the list returned by Ujo.readProperties().
 *
 * @author Pavel Ponec
 * @since 0.81
 */
@Immutable
@SuppressWarnings("deprecation")
public class PathProperty<UJO extends Ujo, VALUE> implements CompositeKey<UJO, VALUE> {

    /** Array of <strong>direct</strong> keys */
    private final Key[] keys;
    /** Is property ascending / descending */
    private final boolean ascending;
    private String name;

    public PathProperty(List<Key> keys) {
        this(keys.toArray(new Key[keys.size()]));
    }

    /** The main constructor. It is recommended to use the factory method
     * {@link #newInstance(org.ujorm.Key, org.ujorm.Key) newInstance(..)}
     * for better performance in some cases.
     * @see #newInstance(org.ujorm.Key, org.ujorm.Key) newInstance(..)
     */
    public PathProperty(Key... keys) {
        this(null, keys);
    }

    /** Main constructor */
    @SuppressWarnings("unchecked")
    public PathProperty(Boolean ascending, Key... keys) {
        final ArrayList<Key> list = new ArrayList<Key>(keys.length + 3);
        for (Key property : keys) {
            if (property.isComposite()) {
                ((CompositeKey)property).exportKeys(list);
            } else {
                list.add(property);
            }
        }
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Argument must not be empty");
        }
        this.ascending = ascending!=null ? ascending : keys[keys.length-1].isAscending();
        this.keys = list.toArray(new Key[list.size()]);
    }

    /** Private constructor for a better performance. For internal use only. */
    private PathProperty(final Key[] keys, final boolean ascending) {
        this.keys = keys;
        this.ascending = ascending;
    }

    /** Get the last property of the current object. The result may not be the direct property. */
    @SuppressWarnings("unchecked")
    public <UJO_IMPL extends Ujo> Key<UJO_IMPL, VALUE> getLastPartialProperty() {
        return keys[keys.length - 1];
    }

    /** Get the first property of the current object. The result is direct property always. */
    @SuppressWarnings("unchecked")
    @Override
    final public <UJO_IMPL extends Ujo> Key<UJO_IMPL, VALUE> getLastKey() {
        Key result = keys[keys.length - 1];
        return result.isComposite()
            ? ((CompositeKey)result).getLastKey()
            : result
            ;
    }

    /** Get the first property of the current object. The result is direct property always. */
    @SuppressWarnings("unchecked")
    @Override
    final public <UJO_IMPL extends Ujo> Key<UJO_IMPL, VALUE> getFirstKey() {
        Key result = keys[0];
        return result.isComposite()
            ? ((CompositeKey)result).getFirstKey()
            : result
            ;
    }

    /** Full property name */
    final public String getName() {
        if (name==null) {
            StringBuilder result = new StringBuilder(32);
            for (Key p : keys) {
                if (result.length() > 0) {
                    result.append('.');
                }
                result.append(p.getName());
            }
            name = result.toString();
        }
        return name;
    }

    /** Property type */
    @Override
    public Class<VALUE> getType() {
        return getLastPartialProperty().getType();
    }

    /** Property domain type */
    @Override
    @SuppressWarnings("unchecked")
    public Class<UJO> getDomainType() {
        return keys[0].getDomainType();
    }

    /** Get a penultimate value of a composite key.
     * If any value (not getLastPartialProperty) is null, then the result is null.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Ujo getSemiValue(final UJO ujo, final boolean create) {
        if (ujo==null) {
            return ujo;
        }
        Ujo result = ujo;
        for (int i = 0, max = keys.length - 1; i < max; i++) {
            Ujo value = (Ujo) keys[i].of(result);
            if (value==null) {
                if (create) {
                    try {
                       value = (Ujo) keys[i].getType().newInstance();
                       result.writeValue(keys[i], value);
                   } catch (Throwable e) {
                       throw new IllegalStateException("Can't create new instance for the key: " + keys[i].toStringFull(), e);
                   }
                } else {
                    return value;
                }
            }
            result = value;
        }
        return result;
    }

    /**
     * An alias for the method of(Ujo) .
     * @see #of(Ujo)
     */
    @SuppressWarnings("unchecked")
    @Override
    final public VALUE getValue(final UJO ujo) throws ValidationException {
        return of(ujo);
    }

    /** {@inheritDoc} */
    @Override
    final public void setValue(final UJO ujo, final VALUE value) throws ValidationException {
        setValue(ujo, value, false);
    }

    @Override
    final public void setValue(final UJO ujo, final VALUE value, boolean createRelations) throws ValidationException {
        final Ujo u = getSemiValue(ujo, createRelations);
        getLastPartialProperty().setValue(u, value);
    }

    /** Get a value from an Ujo object by a chain of keys.
     * If a value  (not getLastPartialProperty) is null, then the result is null.
     */
    @Override
    final public VALUE of(final UJO ujo) {
        final Ujo u = getSemiValue(ujo, false);
        return  u!=null ? getLastPartialProperty().of(u) : null ;
    }

    @Override
    final public int getIndex() {
        return -1;
    }

    /** Returns a default value */
    @Override
    public VALUE getDefault() {
        return getLastPartialProperty().getDefault();
    }

    /** Indicates whether a parameter value of the ujo "equal to" this default value. */
    @Override
    public boolean isDefault(UJO ujo) {
        VALUE value = of(ujo);
        VALUE defaultValue = getDefault();
        final boolean result
        =  value==defaultValue
        || (defaultValue!=null && defaultValue.equals(value))
        ;
        return result;
    }

    /** Copy a value from the first UJO object to second one. A null value is not replaced by the default. */
    @Override
    public void copy(final UJO from, final UJO to) {
        final Ujo from2 = getSemiValue(from, false);
        final Ujo to2 = getSemiValue(to, false);
        getLastPartialProperty().copy(from2, to2);
    }

    /** Returns true if the property type is a type or subtype of the parameter class. */
    @SuppressWarnings("unchecked")
    @Override
    final public boolean isTypeOf(final Class type) {
        return getLastKey().isTypeOf(type);
    }

    /**
     * Returns true, if the property value equals to a parameter value. The property value can be null.
     *
     * @param ujo A basic Ujo.
     * @param value Null value is supported.
     * @return Accordance
     */
    @Override
    public boolean equals(final UJO ujo, final VALUE value) {
        Object myValue = of(ujo);
        if (myValue==value) { return true; }

        final boolean result
        =  myValue!=null
        && value  !=null
        && myValue.equals(value)
        ;
        return result;
    }

    /**
     * Returns true, if the property name equals to the parameter value.
     */
    @Override
    public boolean equalsName(final CharSequence name) {
        return name!=null && name.toString().equals(getName());
    }

    /**
     * Returns true, if the property value equals to a parameter value. The property value can be null.
     *
     * @param property A basic CujoProperty.
     * @param value Null value is supported.
     */
    @Override
    public boolean equals(final Object property) {
        return property instanceof Key
            && property.toString().equals(getName())
            && getType().equals(((Key)property).getType())
            ;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String toStringFull() {
        return getDomainType().getSimpleName() + '.' +  getName();
    }

    /**
     * Returns the full name of the Key including all atributes.
     * <br />Example: Person.id {index=0, ascending=false, ...}
     * @param extended argumenta false calls the method {@link #toStringFull()} only.
     * @return the full name of the Key including all atributes.
     */
    @Override
    public String toStringFull(boolean extended) {
        return  extended
                ? toStringFull() + Property.printAttributes(this)
                : toStringFull() ;
    }

    /** Length of the Name */
    @Override
    public int length() {
        return getName().length();
    }

    /** A char from Name */
    @Override
    public char charAt(int index) {
        return getName().charAt(index);
    }

    /** Sub sequence from the Name */
    @Override
    public CharSequence subSequence(int start, int end) {
        return getName().subSequence(start, end);
    }

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
    @Override
    public final boolean isDirect() {
        return ! isComposite();
    }

    /** @{@inheritDoc} */
    @Override
    public final boolean isComposite() {
        return true;
    }

    /** A flag for an ascending direction of order. For the result is significant only the last property.
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    public boolean isAscending() {
        return ascending;
    }

    /** Create a new instance of the property with a descending direction of order.
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    @SuppressWarnings({"unchecked","deprecation"})
    public Key<UJO,VALUE> descending() {
        return descending(true);
    }

    /** Create a new instance of the property with a descending direction of order.
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    @SuppressWarnings({"unchecked","deprecation"})
    public Key<UJO,VALUE> descending(boolean descending) {
        return isAscending()==descending
                ? new PathProperty(keys, !descending)
                : this
                ;
    }

    /** Export all <string>direct</strong> keys to the list from parameter. */
    @SuppressWarnings("unchecked")
    @Override
    public List<Key> exportKeys(final List<Key> result) {
        for (Key p : keys) {
            if (p.isComposite()) {
                ((CompositeKey)p).exportKeys(result);
            } else {
                result.add(p);
            }
        }
        return result;
    }

    /** Get the last key validator or return the {@code null} value if no validator was assigned */
    public Validator<VALUE> getValidator() {
        return getLastKey().getValidator();
    }

    /** Create new composite (indirect) instance.
     * @since 0.92
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> CompositeKey<UJO, T> add(final Key<? super VALUE, T> property) {

        Key[] props = new Key[keys.length+1];
        System.arraycopy(keys, 0, props, 0, keys.length);
        props[keys.length] = property;

        return new PathProperty(props);
    }

    /** ListKey */
    @SuppressWarnings("unchecked")
    public <T> ListKey<UJO, T> add(ListKey<? super VALUE, T> property) {
        Key[] props = new Key[keys.length+1];
        System.arraycopy(keys, 0, props, 0, keys.length);
        props[keys.length] = property;

        return new PathListProperty(props);
    }


    /** Compare to another Key object by the index and name of the property.
     * @since 1.20
     */
    public int compareTo(final Key p) {
        return getIndex()<p.getIndex() ? -1
             : getIndex()>p.getIndex() ?  1
             : getName().compareTo(p.getName())
             ;
    }

    /** Returns the full sequence of the direct keys */
    public Key[] getDirectKeySequence() {
        final Key[] result = new Key[this.keys.length];
        System.arraycopy(this.keys, 0, result, 0, result.length);
        return result;
    }

    /** Returns a count of inner key items of this composite key */
    public int getCompositeCount() {
        return this.keys.length;
    }

    // ================ STATIC ================

    /** Create a new instance of property with a new sort attribute value.
     * @hidden
     */
    @SuppressWarnings("deprecation")
    public static <UJO extends Ujo, VALUE> Key<UJO, VALUE> sort(final Key<UJO, VALUE> property, final boolean ascending) {
        if (property.isAscending()==ascending) {
            return (Key<UJO, VALUE>) property;
        }
        return property.isComposite()
            ? new PathProperty<UJO, VALUE>(ascending, property)
            : new PathProperty<UJO, VALUE>(new Key[]{property}, ascending)
            ;
    }

    /** Create a new instance of property with a new sort attribute value.
     * This is an alias for the static method {@link #sort(org.ujorm.Key, boolean) sort()}.
     * @hidden
     * @see #sort(org.ujorm.Key, boolean) sort(..)
     */
    public static <UJO extends Ujo, VALUE> Key<UJO, VALUE> newInstance(final Key<UJO, VALUE> property, final boolean ascending) {
        return sort(property, ascending);
    }

    /** Quick instance for the direct property.
     * @hidden
     */
    public static <UJO extends Ujo, VALUE> PathProperty<UJO, VALUE> newInstance(final Key<UJO, VALUE> property) {
        return property.isComposite()
            ? new PathProperty<UJO, VALUE>(property.isAscending(), property)
            : new PathProperty<UJO, VALUE>(new Key[]{property}, property.isAscending())
            ;
    }

    /** Quick instance for the direct properrites
     * @hidden
     */
    public static <UJO1 extends Ujo, UJO2 extends Ujo, VALUE> PathProperty<UJO1, VALUE> newInstance
        ( final Key<UJO1, UJO2> property1
        , final Key<UJO2, VALUE> property2
        ) {
        return property1.isComposite() || property2.isComposite()
            ? new PathProperty<UJO1, VALUE>(property2.isAscending(), property1, property2)
            : new PathProperty<UJO1, VALUE>(new Key[]{property1,property2}, property2.isAscending())
            ;
    }

    /** Create new instance
     * @hidden
     */
    public static <UJO1 extends Ujo, UJO2 extends Ujo, UJO3 extends Ujo, VALUE> PathProperty<UJO1, VALUE> newInstance
        ( final Key<UJO1, UJO2> property1
        , final Key<UJO2, UJO3> property2
        , final Key<UJO3, VALUE> property3
        ) {
        return new PathProperty<UJO1, VALUE>(property1, property2, property3);
    }

    /** Create new instance
     * @hidden
     */
    public static <UJO1 extends Ujo, UJO2 extends Ujo, UJO3 extends Ujo, UJO4 extends Ujo, VALUE> PathProperty<UJO1, VALUE> newInstance
        ( final Key<UJO1, UJO2> property1
        , final Key<UJO2, UJO3> property2
        , final Key<UJO3, UJO4> property3
        , final Key<UJO4, VALUE> property4
        ) {
        return new PathProperty<UJO1, VALUE>(property1, property2, property3, property4);
    }

    /** Create new instance
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo, VALUE> PathProperty<UJO, VALUE> create(Key<UJO, ? extends Object>... keys) {
        return new PathProperty(keys);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> where(Operator operator, VALUE value) {
        return Criterion.where(this, operator, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> where(Operator operator, Key<?, VALUE> value) {
        return Criterion.where(this, operator, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereEq(VALUE value) {
        return Criterion.where(this, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereEq(Key<UJO, VALUE> value) {
        return Criterion.where(this, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereIn(Collection<VALUE> list) {
        return Criterion.whereIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereNotIn(Collection<VALUE> list) {
        return Criterion.whereNotIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereIn(VALUE... list) {
        return Criterion.whereIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereNotIn(VALUE... list) {
        return Criterion.whereNotIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereNull() {
        return Criterion.whereNull(this);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereNotNull() {
        return Criterion.whereNotNull(this);
    }

    /** {@inheritDoc} */
    public Criterion<UJO> whereFilled() {
        return Criterion.whereNotNull(this).and(Criterion.where(this, Operator.NOT_EQ, getEmptyValue()));
    }

    /** {@inheritDoc} */
    public Criterion<UJO> whereNotFilled(){
        return Criterion.whereNull(this).or(Criterion.where(this, getEmptyValue()));
    }

    /** Returns an empty value */
    private VALUE getEmptyValue() {
        final Class type = getType();
        if (CharSequence.class.isAssignableFrom(type)) {
            return (VALUE) "";
        }
        if (type.isArray()) {
            return (VALUE) Array.newInstance(type, 0);
        }
        if (List.class.isAssignableFrom(type)) {
            return (VALUE) Collections.EMPTY_LIST;
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereNeq(VALUE value) {
        return Criterion.where(this, Operator.NOT_EQ, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereGt(VALUE value) {
        return Criterion.where(this, Operator.GT, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereGe(VALUE value) {
        return Criterion.where(this, Operator.GE, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereLt(VALUE value) {
        return Criterion.where(this, Operator.LT, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereLe(VALUE value) {
        return Criterion.where(this, Operator.LE, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> forSql(String sqlCondition) {
        return Criterion.forSql(this, sqlCondition);
    }

    /** The method creates a new Criterion for a native condition (called Native Criterion) in SQL statejemt format.
     * Special features:
     * <ul>
     *   <li>parameters of the SQL_condition are not supported by the Ujorm</li>
     *   <li>your own implementation of SQL the parameters can increase
     *       a risk of the <a href="http://en.wikipedia.org/wiki/SQL_injection">SQL injection</a> attacks</li>
     *   <li>method {@link #evaluate(org.ujorm.Ujo)} is not supported and throws UnsupportedOperationException in the run-time</li>
     *   <li>native Criterion dependents on a selected database so application developers should to create support for each supported database
     *       of target application to ensure database compatibility</li>
     * </ul>
     * @param property The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     * @param sqlTemplate a SQL condition in the String format, the NULL value or empty string is not accepted
     * A substring {@code {0}} will be replaced for the current column name;
     * @param value a codition value
     * A substring {@code {1}} will be replaced for the current column name;
     * @see Operator#XSQL
     */

    public Criterion<UJO> forSql(String sqlTemplate, VALUE value) {
        return Criterion.forSql(this, sqlTemplate, value);
    }

    public Criterion<UJO> forSqlUnchecked(String sqlTemplate, Object value) {
        return Criterion.forSqlUnchecked(this, sqlTemplate, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> forAll() {
        return Criterion.forAll(this);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> forNone() {
        return Criterion.forNone(this);
    }

}
