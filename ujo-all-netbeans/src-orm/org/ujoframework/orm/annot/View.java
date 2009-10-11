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

package org.ujoframework.orm.annot;
import java.lang.annotation.*;

/** 
 * View is a description of database view. Use it simillary like a table.
 * @see Table
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface View {

    /** A view name. Default value is taken from a relation property name. */
    String name() default "";
    /** View alias name. The default value is taken from a name. */
    String alias() default "";
    /** Mapping a VIEW to the SQL SELECT. */
    String  select() default "";
    /** Name of schema. If the value is empty than a default database schema is used. */
    String schema() default "";
    
}