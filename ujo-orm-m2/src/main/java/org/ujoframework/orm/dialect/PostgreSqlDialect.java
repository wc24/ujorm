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

package org.ujoframework.orm.dialect;

import java.io.IOException;
import org.ujoframework.orm.SqlDialect;
import org.ujoframework.orm.metaModel.MetaColumn;

/** PostgreSQL (http://www.postgresql.org/) */
public class PostgreSqlDialect extends SqlDialect {

    @Override
    public String getJdbcUrl() {
        return "";
    }

    @Override
    public String getJdbcDriver() {
        return "org.postgresql.Driver";
    }

    /** Print SQL 'CREATE SCHEMA' */
    @Override
    public Appendable printCreateSchema(String schema, Appendable out) throws IOException {
        out.append("CREATE SCHEMA ");
        out.append(schema);
        return out;
    }

    /** PostgreSql dialect uses a database type OID (instead of the BLBO). */
    @Override
    protected String getColumnType(final MetaColumn column) {
        switch (MetaColumn.DB_TYPE.of(column)) {
            case BLOB:
                return "OID";
            default:
                return super.getColumnType(column);
        }
    }


}