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


package org.ujoframework.orm.ao;

/**
 * Possible values to create the DLL (Data Definition Language) for for defining data structures.
 * @author Pavel Ponec
 */
public enum Orm2ddlPolicy {

    /** Framework is expected to match the DDL structure with the ORM model and do not make any validation. */
    DO_NOTHING,
    /** Create full DDL structure in condition that the the database structure was not found. */
    CREATE_DDL,
    /** Create or update full DDL structure. It is the default value. */
    CREATE_OR_UPDATE_DDL,
    /** Throw the IllegalStateException in case missing a table, index, or column in the connected database. */
    VALIDATE,
    ;

}
