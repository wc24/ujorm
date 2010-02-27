/*
 *  Copyright 2010 pavel.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.ujoframework.orm.ao;

/**
 * How to use the report of any check message?
 * @author Ponec
 */
public enum CheckReport {

    /** Skip the check test. */
    SKIP,
    /** Log a WARNING with a bug message. */
    WARNING,
    /** Throw an EXCEPTION with a bug message. */
    EXCEPTION;
}
