/*
 *  Copyright 2012-2012 Pavel Ponec
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
package org.ujorm.validator.impl;

import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;
import org.ujorm.Ujo;
import org.ujorm.Validator;
import org.ujorm.criterion.Criterion;
import static org.ujorm.validator.impl.NotNullValidator.*;

/**
 * Input Validator Factory.
 * @author Pavel Ponec
 * @see Validator
 */
public class ValidatorFactory {

    /** A modifier of the validator */
    public static enum CheckType {
        /** The nullable value is allowed (the default option) */
        NULLABLE,
        /** Only notnull value is allowed */
        MANDATORY;
    }
    /** The nullable value is allowed (the default option) */
    public static final CheckType NULLABLE = CheckType.NULLABLE;
    /** Only notnull value is allowed */
    public static final CheckType MANDATORY = CheckType.MANDATORY;
    /** An alias for the {@link #MANDATORY} option */
    public static final CheckType NOTNULL = CheckType.MANDATORY;

    /** A child class can add new method only,
     * because the current method have hot static modifications. */
    protected ValidatorFactory() {
    }

    /** Not null
     * @see NotNullValidator
     */
    public static <T extends Object> Validator<T> notNull(Class<T> type) {
        return NOT_NULL;
    }

    /** Not null
     * @see NotNullValidator
     */
    public static Validator notNull() {
        return NOT_NULL;
    }

    /** Check the not {@code null} and not empty value type of String. Method <strong>is not</strong> type save!
     * @see NotEmptyValidator
     */
    public static Validator notEmpty() {
        return NotEmptyValidator.NOT_EMPTY;
    }

    /** Input value is valid if the trimmed String length is great than zero. Method <strong>is not</strong> type save!
     * @see NotEmptyValidator
     */
    public static Validator notBlank() {
        return NotEmptyValidator.NOT_BLANK;
    }

    /** Not null and not empty. The method is type safe!
     * @param type Argument type can be the CharSequence or the Collection
     * @see NotEmptyValidator
     * @see NotEmptyCollectionValidator
     */
    public static <T> Validator<T> notEmpty(Class<T> type) {
        if (CharSequence.class.isAssignableFrom(type)) {
            return (Validator<T>) NotEmptyValidator.NOT_EMPTY;
        }
        if (Collection.class.isAssignableFrom(type)) {
            return new NotEmptyCollectionValidator();
        } else {
            throw new IllegalArgumentException("The notEmpty() method does not support argument type: " + type.getName());
        }
    }

    /** Value from min (inxlusive) to max (exclusive)
     * @see BetweenValidator
     */
    public static <VALUE extends Comparable> Validator<VALUE> between(VALUE min, VALUE max) {
        return new BetweenValidator(min, max);
    }

    /** Value from min (inxlusive) to max (exclusive)
     * @see BetweenValidator
     */
    public static <VALUE extends Comparable> Validator<VALUE> between(CheckType type, VALUE min, VALUE max) {
        final Validator<VALUE> result = between(min, max);
        return type == MANDATORY ? NOT_NULL.and(result) : result;
    }

    /** Value from min (inxlusive) to max (inxlusive)
     * @see RangeValidator
     */
    public static <VALUE extends Comparable> Validator<VALUE> range(VALUE min, VALUE max) {
        return new RangeValidator(min, max);
    }

    /** Value from min (inxlusive) to max (inxlusive)
     * @see RangeValidator
     */
    public static <VALUE extends Comparable> Validator<VALUE> range(CheckType type, VALUE min, VALUE max) {
        final Validator<VALUE> result = range(min, max);
        return type == MANDATORY ? NOT_NULL.and(result) : result;
    }

    /** Value from min (inclusive)
     * @see ComparableValidator
     */
    public static <VALUE extends Comparable> Validator<VALUE> min(VALUE min) {
        return new ComparableValidator(min, false);
    }

    /** Value from min (inclusive)
     * @see ComparableValidator
     */
    public static <VALUE extends Comparable> Validator<VALUE> min(CheckType type, VALUE min) {
        final Validator<VALUE> result = min(min);
        return type == MANDATORY ? NOT_NULL.and(result) : result;
    }

    /** Value from to max (inclusive)
     * @see ComparableValidator
     */
    public static <VALUE extends Comparable> Validator<VALUE> max(VALUE max) {
        return new ComparableValidator(max, true);
    }

    /** Value from to max (inclusive)
     * @see ComparableValidator
     */
    public static <VALUE extends Comparable> Validator<VALUE> max(CheckType type, VALUE max) {
        final Validator<VALUE> result = max(max);
        return type == MANDATORY ? NOT_NULL.and(result) : result;
    }

    /** Validator compares an input value with a set of constants by the hashCode() and equals() methods.
     * @see ConstantsValidator
     */
    public static <VALUE> Validator<VALUE> required(VALUE... requiredSet) {
        return new ConstantsValidator(false, requiredSet);
    }

    /** Validator compares an input value with a set of constants by the hashCode() and equals() methods.
     * @see ConstantsValidator
     */
    public static <VALUE> Validator<VALUE> required(CheckType type, VALUE... requiredSet) {
        final Validator<VALUE> result = required(requiredSet);
        return type == MANDATORY ? NOT_NULL.and(result) : result;
    }

    /** Validator compares an input value with a set of constants by the hashCode() and equals() methods.
     * @see ConstantsValidator
     */
    public static <VALUE> Validator<VALUE> forbidden(VALUE... forbiddenSet) {
        return new ConstantsValidator(true, forbiddenSet);
    }

    /** Validator compares an input value with a set of constants by the hashCode() and equals() methods.
     * @see ConstantsValidator
     */
    public static <VALUE> Validator<VALUE> forbidden(CheckType type, VALUE... forbiddenSet) {
        final Validator<VALUE> result = forbidden(forbiddenSet);
        return type == MANDATORY ? NOT_NULL.and(result) : result;
    }

    /** Match the input value to the simple email pattern.
     * For a better regular expression see the <a href="http://ex-parrot.com/~pdw/Mail-RFC822-Address.html">next link</a>.
     * @see PatternValidator
     */
    public static Validator<String> email() {
        return regexp(PatternValidator.EMAIL);
    }

    /** Match the input value to the simple email pattern.
     * For a better regular expression see the <a href="http://ex-parrot.com/~pdw/Mail-RFC822-Address.html">next link</a>.
     * @see PatternValidator
     */
    public static Validator<String> email(CheckType type) {
        return regexp(type, PatternValidator.EMAIL);
    }

    /** Match the input value to pattern
     * @see PatternValidator
     */
    public static Validator<String> regexp(String pattern) {
        return new PatternValidator(Pattern.compile(pattern));
    }

    /** Match the input value to pattern
     * @see PatternValidator
     */
    public static Validator<String> regexp(CheckType type, String pattern) {
        return regexp(type, Pattern.compile(pattern));
    }

    /** Match the input value to pattern
     * @see PatternValidator
     */
    public static Validator<String> regexp(CheckType type, Pattern pattern) {
        final Validator<String> result = new PatternValidator(pattern);
        return type == MANDATORY ? NOT_NULL.and(result) : result;
    }

    /** Check the date is in the future in compare to a local time
     * @see DateValidator
     */
    public static <VALUE extends Date> Validator<VALUE> future() {
        return new DateValidator<VALUE>(false);
    }

    /** Check the date is in the future in compare to a local time
     * @see DateValidator
     */
    public static <VALUE extends Date> Validator<VALUE> future(CheckType type) {
        final Validator<VALUE> result = future();
        return type == MANDATORY ? NOT_NULL.and(result) : result;
    }

    /** Check the date is in the past or equals to now in compare to the local time
     * @see NotNullValidator
     */
    public static <VALUE extends Date> Validator<VALUE> past() {
        return new DateValidator<VALUE>(true);
    }

    /** Check the date is in the past or equals to now in compare to the local time
     * @see NotNullValidator
     */
    public static <VALUE extends Date> Validator<VALUE> past(CheckType type) {
        final Validator<VALUE> result = past();
        return type == MANDATORY ? NOT_NULL.and(result) : result;
    }

    /** Check the maximal length of the String.
     * The {@code null} value is allowed.
     * @param max String maximal length (inclusive)
     * @see StringLengthValidator
     */
    public static Validator<String> length(int max) {
        return new LengthValidator<String>(max);
    }

    /** Check the maximal length of the String.
     * The {@code null} value is allowed.
     * @param max String maximal length (inclusive)
     * @see StringLengthValidator
     */
    public static Validator<String> length(CheckType type, int max) {
        final Validator<String> result = length(max);
        return type == MANDATORY ? NOT_NULL.and(result) : result;
    }

    /** Check the minimal and maximal length of the String.
     * The {@code null} value is allowed.
     * @param min String minimal length (inclusive)
     * @param max String maximal length (inclusive)
     * @see StringLengthValidator
     */
    public static Validator<String> length(int min, int max) {
        return new LengthValidator<String>(min, max);
    }

    /** Check the minimal and maximal length of the String.
     * The {@code null} value is allowed.
     * @param min String minimal length (inclusive)
     * @param max String maximal length (inclusive)
     * @see StringLengthValidator
     */
    public static Validator<String> length(CheckType type, int min, int max) {
        final Validator<String> result = length(min, max);
        return type == MANDATORY ? NOT_NULL.and(result) : result;
    }

    /** Check the maximal length of the String
     * @param min String minimal length (inclusive)
     * @param max String maximal length (inclusive)
     * @see StringLengthValidator
     * @deprecated Use the method {@link #length(int, int)} instead of.
     */
    @Deprecated
    public static Validator<String> size(int min, int max) {
        return new LengthValidator<String>(min, max);
    }

    /** The validator allows to read only default values. No value is allowed including the {@code null} value. */
    public static Validator readOnly() {
        return new ReadOnlyValidator<String>(true);
    }

    /** It is a logical empty validator. Each value is allowed. */
    public static Validator allAllowed() {
        return new ReadOnlyValidator<String>(false);
    }

    /** Check a content of another related Ujo object using the Criterion.
     * <br>Note 1: this result is not serialiable object, because the Criterion is not serializable
     * <br>Note 2: a static field type of Key is Serializable allways, including the CriterionValidator inside.
     * @see CriterionValidator
     */
    public static <VALUE extends Ujo> Validator<VALUE> relation(Criterion<VALUE> criterion) {
        return new CriterionValidator<VALUE>(criterion);
    }

    /** Check a content of another related Ujo object using the Criterion.
     * <br>Note 1: this result is not serialiable object, because the Criterion is not serializable
     * <br>Note 2: a static field type of Key is Serializable allways, including the CriterionValidator inside.
     * @see CriterionValidator
     */
    public static <VALUE extends Ujo> Validator<VALUE> relation(CheckType type, Criterion<VALUE> criterion) {
        Validator<VALUE> result = relation(criterion);
        return type == MANDATORY ? NOT_NULL.and(result) : result;
    }
}