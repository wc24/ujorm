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
import org.ujoframework.orm.SqlRenderer;

/** 
 * Use the annotation to mark a UjoProperty static field like XML Attribute.
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value=ElementType.TYPE)
public @interface Db {

    /** Name of database */
    String name() default "";
    /** SQL renderer by a DB Vendor. */
    Class<? extends SqlRenderer> renderer();
    /** LDAP */
    String ldap() default "";
    /** JDBC Url */
    String jdbcUrl() default "";
    /** Connection User */
    String user()     default "";
    /** Connection password */
    String password() default "";


    
}