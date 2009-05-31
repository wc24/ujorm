/*
 *  Copyright 2007-2008 Paul Ponec
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


package org.ujoframework.criterion;

import java.util.Locale;
import java.util.regex.Pattern;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;

/**
 * The value criterion implementation.
 * @since 0.90
 * @author Pavel Ponec
 */
public class ValueCriterion<UJO extends Ujo> extends Criterion<UJO> {

    /** True constant criterion */
    public static final Criterion<Ujo> TRUE  = new ValueCriterion<Ujo>(true);
    /** False constant criterion */
    public static final Criterion<Ujo> FALSE = new ValueCriterion<Ujo>(false);
    
    final private UjoProperty property;
    final private Operator    operator;
    final protected Object    value;
    
    /** Creante an Criterion constant */
    public ValueCriterion(boolean value) {
        this(null, Operator.X_FIXED, value);
    }

    /** An undefined operator (null) is replaced by EQ. */
    public ValueCriterion(UjoProperty<UJO,? extends Object> property, Operator operator, UjoProperty<UJO,Object> value) {
        this(property, operator, (Object) value);    
    }

    /** An undefined operator (null) is replaced by EQ. */
    public ValueCriterion(UjoProperty<UJO,? extends Object> property, Operator operator, Object value) {

        if (property==null) {
            value = (Boolean) value; // Type test for the CriterionConstant.
        }
        if (operator==null) {
            operator = Operator.EQ;  // The default operator.
        }
        
        // A validation test:
        switch (operator) {
            case EQUALS_CASE_INSENSITIVE:
            case STARTS:
            case STARTS_CASE_INSENSITIVE:
            case ENDS:
            case ENDS_CASE_INSENSITIVE:
            case CONTAINS:
            case CONTAINS_CASE_INSENSITIVE:
                 makeCharSequenceTest(property);
                 makeCharSequenceTest(value);
                 break;
        }

        this.property = property;
        this.value    = value;
        this.operator = operator;
    }

    /** Returns the left node of the parrent */
    @Override
    public final UjoProperty getLeftNode() {
        return property;
    }

    /** Returns the right node of the parrent */
    @Override
    public final Object getRightNode() {
        return value;
    }

    /** Returns an operator */
    @Override
    public final Operator getOperator() {
        return operator;
    }


    @SuppressWarnings("unchecked")
    public boolean evaluate(UJO ujo) {
        if (isConstant()) {
            return (Boolean) value;
        }
        Object value2 = value instanceof UjoProperty
            ? ((UjoProperty)value).getValue(ujo)
            : value
            ;
        boolean caseInsensitve = true;
        
        switch (operator) {
            case EQ:
            case NOT_EQ:
                boolean result = property.equals(ujo, value2);
                return operator==Operator.EQ ? result : !result ;
            case REGEXP:
            case NOT_REGEXP:
                Pattern p = value2 instanceof Pattern 
                    ? (Pattern) value2 
                    : Pattern.compile(value2.toString())
                    ;
                Object val1 = property.getValue(ujo);
                boolean result2 = val1!=null && p.matcher(val1.toString()).matches();
                return operator==Operator.REGEXP ? result2 : !result2 ;
            case STARTS:
            case ENDS:
            case CONTAINS:
                 caseInsensitve = false;
            case EQUALS_CASE_INSENSITIVE:
            case STARTS_CASE_INSENSITIVE:
            case ENDS_CASE_INSENSITIVE:
            case CONTAINS_CASE_INSENSITIVE: {
                Object object = property.of(ujo);
                if (object==value2              ) { return true ; }
                if (object==null || value2==null) { return false; }

                String t1 = object.toString();
                String t2 = value2.toString();

                if (caseInsensitve) {
                    t1 = t1.toUpperCase(Locale.ENGLISH);
                    t2 = t2.toUpperCase(Locale.ENGLISH);
                }

                switch (operator) {
                    case EQUALS_CASE_INSENSITIVE:
                         return t1.equals(t2);
                    case STARTS:
                    case STARTS_CASE_INSENSITIVE:
                         return t1.startsWith(t2);
                    case ENDS:
                    case ENDS_CASE_INSENSITIVE:
                         return t1.endsWith(t2);
                    case CONTAINS:
                    case CONTAINS_CASE_INSENSITIVE:
                         return t1.contains(t2);
                    default:
                         throw new IllegalStateException("State:" + operator);
                }
            }
        }
        
        Comparable val2 = (Comparable) value2;
        if (null== val2)  {return false; }
        Comparable val1 = (Comparable) property.getValue(ujo);
        if (null== val1)  {return false; }
        int result = compare(val1, val2);
        
        switch (operator) {
            case LT: return result< 0;
            case LE: return result<=0;
            case GT: return result> 0;
            case GE: return result>=0;
        }

        throw new IllegalArgumentException("Illegal operator: " + operator);
    }

    /** Test a value is instance of CharSequence or a type UjoProperty is type of CharSequence.
     * If property is not valid than throw Exception.
     */
    protected void makeCharSequenceTest(Object value) throws IllegalArgumentException {
        if (value instanceof CharSequence
        ||  value instanceof UjoProperty
        && ((UjoProperty)value).isTypeOf(CharSequence.class)
        ){
            return;
        } else {
            final String msg = "Property type must by String or CharSequence";
            throw new IllegalArgumentException(msg);
        }
    }

    /** Compare two object */
    @SuppressWarnings("unchecked")
    protected int compare
        ( final Comparable o1
        , final Comparable o2
    ) {
        if (o1==o2  ) { return  0; }
        if (o1==null) { return +1; }
        if (o2==null) { return -1; }
        return o1.compareTo(o2);
    }
        
    /** Is the operator insensitive. */
    public boolean isInsensitive() {
        switch (operator) {
            case EQUALS_CASE_INSENSITIVE:
            case STARTS_CASE_INSENSITIVE:
            case ENDS_CASE_INSENSITIVE:
            case CONTAINS_CASE_INSENSITIVE:
                 return true;
            default:
                 return false;
        }
    }
    
    /** Is the criterion result independent on the bean object? */
    public final boolean isConstant() {
        return operator==Operator.X_FIXED;
    }

    @Override
    public String toString() {
        String result
            = isConstant()
            ? ""
            : (property + " " + operator.name() + " ")
            ;
        return result + value;
    }

}