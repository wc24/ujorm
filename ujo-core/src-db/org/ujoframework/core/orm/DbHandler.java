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

package org.ujoframework.core.orm;

import org.ujoframework.UjoProperty;
import org.ujoframework.implementation.db.TableUjo;

/**
 * The basic class for an ORM support.
 * @author pavel
 */
public class DbHandler {

    private static DbHandler handler = new DbHandler();
    private Session session = new Session();

    /** The Sigleton constructor */
    protected DbHandler() {
    }

    public static DbHandler getInstance() {
        return handler;
    }

     /** Get Session */
    public Session getSession() {
        return session;
    }


    public boolean isPersistent(UjoProperty property) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** Load a metada */
    public <UJO extends TableUjo> void loadMeta(Class<UJO> root) {

    }

    /** Load a metada */
    public <UJO extends TableUjo> void createDb(Class<UJO> root) {
        loadMeta(root);
    }



}
