/*
 * Copyright 2012-2012 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.core;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.core.annot.PackagePrivate;
import org.ujorm.extensions.AbstractCollectionProperty;
import org.ujorm.extensions.ListProperty;
import org.ujorm.extensions.Property;
import org.ujorm.extensions.PropertyModifier;

/**
 * Serializable property factory is the best tool of Ujorm to create Property implementations.
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 * <span class="java-keywords">import</span> org.ujorm.*;
 * <span class="keyword-directive">public class</span> Person <span class="keyword-directive">implements</span> Ujo {
 *
 *     <span class="keyword-directive">private static final</span> KeyFactory&lt;Person&gt; factory
 *             = KeyFactory.CamelBuilder.get(Person.<span class="keyword-directive">class</span>);
 *
 *     <span class="keyword-directive">public static final</span> Key&lt;Person,Long&gt; PID = factory.newKey();
 *     <span class="keyword-directive">public static final</span> Key&lt;Person,Integer&gt; AGE = factory.newKey();
 *     <span class="keyword-directive">public static final</span> ListUjoProperty&lt;Person,String&gt; NAMES = factory.newListProperty();
 *
 *     <span class="keyword-directive">static</span> {
 *         pf.lock();
 *     }
 *
 *     <span class="comment">/&#42;&#42; Data container &#42;/</span>
 *     <span class="keyword-directive">protected</span> Object[] data;
 *
 *     <span class="keyword-directive">public</span> Object readValue(Key property) {
 *         <span class="keyword-directive">return</span> data==<span class="keyword-directive">null</span> ? data : data[property.getIndex()];
 *     }
 *
 *     <span class="keyword-directive">public</span> <span class="keyword-directive">void</span> writeValue(Key property, Object value) {
 *         <span class="keyword-directive">if</span> (data==<span class="keyword-directive">null</span>) {
 *             data = <span class="keyword-directive">new</span> Object[readKeys().size()];
 *         }
 *         data[property.getIndex()] = value;
 *     }
 *
 *     <span class="keyword-directive">public</span> KeyList&lt;?&gt; readKeys() {
 *         <span class="keyword-directive">return</span> factory.getKeyList();
 *     }
 *
 *     <span class="keyword-directive">public</span> <span class="keyword-directive">boolean</span> readAuthorization(UjoAction action, Key property, Object value) {
 *         <span class="keyword-directive">return</span> <span class="keyword-directive">true</span>;
 *     }
 * }
 * </pre>

 * @author Pavel Ponec
 */
public class KeyFactory<UJO extends Ujo> implements Serializable {

    /** Generate property name using the cammel case. */
    protected static final boolean CAMEL_CASE = true;

    /** Requested modifier of property definitions. */
    public static final int PROPERTY_MODIFIER = Modifier.STATIC|Modifier.PUBLIC|Modifier.FINAL;

    /** Transient property list */
    transient private InnerDataStore<UJO> tmpStore;

    /** Property Store */
    private KeyList<UJO> propertyStore;

    @SuppressWarnings("unchecked")
    public KeyFactory(Class<? extends UJO> type) {
        this(type, false);
    }

    @SuppressWarnings("unchecked")
    public KeyFactory(Class<? extends UJO> type, boolean propertyCamelCase) {
        this(type, propertyCamelCase, null);
    }

    /**
     * Create new Property Factory for objecty of type.
     * @param type The domain class
     * @param propertyCamelCase Property names are created along fild name by a camel case converter.
     * @param abstractSuperProperties Pass a super keys fromo an abstract super class, if any.
     */
    @SuppressWarnings("unchecked")
    public KeyFactory(Class<? extends UJO> type, boolean propertyCamelCase, KeyList<?> abstractSuperProperties) {
        this.tmpStore = new InnerDataStore<UJO>(type, propertyCamelCase);
        if (abstractSuperProperties==null) {
            abstractSuperProperties = getSuperProperties();
        } else {
            assert abstractSuperProperties.getType().isAssignableFrom(type) : "Type parameters is not child of the SuperProperites type: " + abstractSuperProperties.getTypeName();
        }
        if (abstractSuperProperties!=null) {
            for (Key p : abstractSuperProperties) {
                tmpStore.addProperty(p);
            }
        }
    }

    /** Read Keys from the super class */
    protected final KeyList<?> getSuperProperties() {
        final Class<?> superClass = this.tmpStore.type.getSuperclass();
        if (Ujo.class.isAssignableFrom(superClass)) {
            if (Modifier.isAbstract(superClass.getModifiers())) {
                KeyList<?> r1 = null;
                KeyFactory<?> r2 = null;
                for (Field field : superClass.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        try {
                            if (r1==null) {
                                r1 = getFieldValue(KeyList.class, field);
                            }
                            if (r2==null) {
                                r2 = getFieldValue(KeyFactory.class, field);
                            }
                        } catch (Exception e) {
                            final String msg = String.format("Pass the %s attribute of the superlass %s to the constructor of the class %s, please"
                                    , KeyList.class.getSimpleName()
                                    , superClass
                                    , getClass().getSimpleName());
                           throw new IllegalArgumentException(msg, e);
                        }
                    }
                }
                return r1 != null ? r1 //
                     : r2 != null ? r2.getKeyList() //
                     : null;
            } else {
                try {
                    return ((Ujo) superClass.newInstance()).readKeys();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Can't create instance of " + superClass, e);
                }
            }
        }
        return null;
    }

    /** Returns a field value */
    private <T> T getFieldValue(Class<T> type, Field field) throws Exception {
        if (type.isAssignableFrom(field.getType())) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return (T) field.get(null);
        }
        return null;
    }

    /** Add an new property for an internal use. */
    protected boolean addKey(Property p) {
        checkLock();
        return tmpStore.addProperty(p);
    }

    /** Lock the property factory */
    public final void lock() {
        lockAndSize();
    }

    /** Lock the property factory
     * @return count of the direct keys.
     */
    public final int lockAndSize() {
        return getKeyList().size();
    }

    /** Get KeyRing */
    public KeyList<UJO> getKeyList() {
        if (propertyStore==null) {
            propertyStore = createPropertyList();
            onCreate(propertyStore, tmpStore);
            tmpStore = null;
        }
        return propertyStore;
    }

    /** Create a property List */
    protected KeyList<UJO> createPropertyList() throws IllegalStateException {
        final List<Field> fields = tmpStore.getFields();
        try {
            for (Key<UJO, ?> p : tmpStore.getProperties()) {
                if (p instanceof Property) {
                    final Property pr = (Property) p;
                    if (PropertyModifier.isLock(pr)) {
                        continue;
                    }
                    Field field = findField(p, fields);
                    if (p.getName() == null) {
                        PropertyModifier.setName(createPropertyName(field, this.tmpStore.camelCase), pr);
                    }
                    if (p.getType() == null) {
                        PropertyModifier.setType(getGenericClass(field, 1), pr);
                    }
                    if (p instanceof AbstractCollectionProperty) {
                        final AbstractCollectionProperty lp = (AbstractCollectionProperty) pr;
                        if (lp.getItemType() == null) {
                            PropertyModifier.setItemType(getGenericClass(field,1), lp);
                        }
                    }
                    PropertyModifier.lock(pr); // Lock all attributes:
                    tmpStore.addAnnotations(p, field); // Save all annotation s annotations.
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Can't initialize a property of the " + tmpStore.type, e);
        }
        return tmpStore.createPropertyList();
    }

    /** Create a property name along the field. */
    protected String createPropertyName(Field field, boolean camelCase) {
        if (camelCase) {
            final StringBuilder result = new StringBuilder(32);
            final String name = field.getName();
            boolean lower = true;
            for (int i = 0, max = name.length(); i < max; i++) {
                final char c = name.charAt(i);
                if (c == '_') {
                    lower = false;
                } else {
                    result.append(lower ? Character.toLowerCase(c) : c);
                    lower = true;
                }
            }
            return result.toString();
        } else {
            return field.getName();
        }
    }

    /** Find field */
    private Field findField(Key p, List<Field> fields) throws Exception {
        for (Field field : fields) {
            if (field.get(null) == p) {
                return field;
            }
        }
        final String msg = String.format("Can't get a field for the property index #%d - %s"
                , p.getIndex()
                , p.getName());
        throw new IllegalStateException(msg);
    }

    /** Create new Key */
    public <T> Key<UJO,T> newKey() {
        return createProperty(null, null);
    }

    /** Create new Key */
    public <T> Key<UJO,T> newKey(String name) {
        return createProperty(name, null);
    }

    /** Create new Key with a default value */
    public <T> Key<UJO,T> newKeyDefault(T defaultValue) {
        return createProperty(null, defaultValue);
    }

    /** Create new Key */
    public <T> Key<UJO,T> newKey(String name, T defaultValue) {
        return createProperty(name, defaultValue);
    }

    /** Create new Key for a value type class */
    public <T> Key<UJO,T> newClassKey(String name, Class<?> defaultClassValue) {
        return createProperty(name, (T) defaultClassValue);
    }

    /** Add new Key for a value type class, index must be undefied */
    public <P extends Property<UJO,?>> P add(P key) {
        if (key.getIndex()>=0) {
            throw new IllegalArgumentException("Property index must be undefined");
        }
        addKey(key);
        return (P) key;
    }

    /** Common protected factory method */
    protected <T> Key<UJO,T> createProperty(String name, T defaultValue) {
        final Property<UJO,T> p = Property.newInstance(name, null, defaultValue, tmpStore.size(), false);
        addKey(p);
        return p;
    }

    /** Create new Key */
    public final <T> ListProperty<UJO,T> newListProperty() {
        return newListProperty(null);
    }

    /** Create new Key */
    public <T> ListProperty<UJO,T> newListProperty(String name) {
        checkLock();
        final ListProperty<UJO,T> p = ListProperty.newListProperty(name, null, tmpStore.size(), false);
        tmpStore.addProperty(p);
        return p;
    }

    /** Check if the class is locked */
    protected void checkLock() throws IllegalStateException {
        if (propertyStore!=null) {
            throw new IllegalStateException(getClass().getSimpleName() + " is locked");
        }
    }

    /** An event on Create */
    protected void onCreate(KeyList<UJO> list, InnerDataStore<UJO> innerData) {
        UjoManager.getInstance().register(list, innerData);
    }

    /* ================== STATIC METHOD ================== */


    /** Regurns array of generic parameters */
    @PackagePrivate static Class getGenericClass(final Field field, final int position) throws IllegalArgumentException {
        try {
            final ParameterizedType type = (ParameterizedType) field.getGenericType();
            final Type result = type.getActualTypeArguments()[position];
            return (result instanceof Class) ? (Class) result : Class.class;
        } catch (Exception e) {
            final String msg = String.format("The field '%s' generic scan failed", field.getName());
            throw new IllegalArgumentException(msg, e);
        }
    }

    // ================== INNER CLASS ==================

    /** A temporarry data store. */
    protected static final class InnerDataStore<UJO extends Ujo> {

        /** The Ujo type is serializad */
        private final Class<? extends UJO> type;

        /** Convert <strong>field names<strong> to a camelCase name.*/
        private final boolean camelCase;

        /** Transient property list */
        private final List<Key<UJO,?>> propertyList;

        /** Property annotations */
        private final Map<Key<UJO,?>, Map<Class<? extends Annotation>,Annotation>> annotationsMap;

        /** Constructor */
        public InnerDataStore(Class<? extends UJO> type, boolean propertyCamelCase) {
            this.type = type;
            this.camelCase = propertyCamelCase;
            this.propertyList = new ArrayList<Key<UJO,?>>(32);
            this.annotationsMap = new HashMap<Key<UJO,?>,Map<Class<? extends Annotation>,Annotation>>();
        }

        /** Add all annotation for required property. */
        public void addAnnotations(Key<UJO,?> p, Field field) {
            final Annotation[] annotations = field.getAnnotations();
            Map<Class<? extends Annotation>,Annotation> annots = annotationsMap.get(field);
            if (annots==null && annotations.length>0) {
                annots = new HashMap<Class<? extends Annotation>,Annotation>(annotations.length);
                annotationsMap.put(p, annots);
            }
            for (Annotation annotation : annotations) {
                annots.put(annotation.annotationType(), annotation);
            }
        }

        /** Add property to a list */
        public boolean addProperty(Key p) {
            return propertyList.add(p);
        }

        /** Get all keys */
        public Iterable<Key<UJO,?>> getProperties() {
            return propertyList;
        }

        /** Create a new Property List */
        public KeyList<UJO> createPropertyList() {
            return KeyRing.of(type, (List) propertyList);
        }

        /** Returns a count of the Key items */
        public int size() {
            return propertyList.size();
        }

        /** Returns a domain type. */
        public Class<? extends UJO> getDomainType() {
            return type;
        }

        /**
         * @return the cammelCase
         */
        public boolean isCammelCase() {
            return camelCase;
        }

        /** Get all UjoPorperty fields */
        public List<Field> getFields() {
            final Field[] fields = type.getFields();
            final List<Field> result = new LinkedList<Field>();
            for (int j = 0; j < fields.length; j++) {
                final Field field = fields[j];
                if (field.getModifiers()==UjoManager.PROPERTY_MODIFIER
                &&  Key.class.isAssignableFrom(field.getType()) ){
                    result.add(field);
                }
            }
            return result;
        }

        /**
         * Returns the Property annotations Set.
         */
        @PackagePrivate Map<Class<? extends Annotation>,Annotation> getAnnotations(Key<UJO,?> p) {
            Map<Class<? extends Annotation>,Annotation> result = annotationsMap.get(p);
            if (result==null) {
                result = Collections.emptyMap();
            }
            return result;
        }

        /**
         * Returns required annotation or {@code null}, if no annotatin was not found.
         */
        public <A extends Annotation> A getAnnotation(Key<UJO,?> p, Class<A> annoType) {
            final Map<Class<? extends Annotation>,Annotation> result = getAnnotations(p);
            return (A) result.get(annoType);
        }
    }

    /** The base factory */
    public static final class Builder {

        /** Return an instance of the {@link KeyFactory} class */
        public static <UJO extends Ujo> KeyFactory<UJO> get(Class<UJO> baseClass) {
            return new KeyFactory(baseClass);
        }

        /** Return an instance of the {@link KeyFactory} class.
         * @param baseClass The domain class
         * @param superProperties Keys form an abstract super class
         */
        public static <UJO extends Ujo> KeyFactory<UJO> get(Class<UJO> baseClass, KeyList<?> superProperties) {
            return new KeyFactory(baseClass, false, superProperties);
        }
   }

    /** The base factory */
    public static final class CamelBuilder {

        /** Return an instance of the {@link KeyFactory} class
         * @param baseClass Base class
         * @param propertyCamelCase {@link #CAMEL_CASE}
         * @return Return an instance of the {@link KeyFactory} class
         */
        public static <UJO extends Ujo> KeyFactory<UJO> get(Class<UJO> baseClass) {
            return new KeyFactory(baseClass, CAMEL_CASE, null);
        }

        /** Return an instance of the {@link KeyFactory} class.
         * @param baseClass The domain class
         * @param propertyCamelCase {@link #CAMEL_CASE}
         * @param superProperties Keys form an abstract super class
         */
        public static <UJO extends Ujo> KeyFactory<UJO> get(Class<UJO> baseClass, KeyList<?> superProperties) {
            return new KeyFactory(baseClass, CAMEL_CASE, superProperties);
        }

   }


}