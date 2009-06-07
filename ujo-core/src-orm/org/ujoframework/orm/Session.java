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
package org.ujoframework.orm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.extensions.PathProperty;
import org.ujoframework.implementation.orm.RelationToMany;
import org.ujoframework.orm.ao.CacheKey;
import org.ujoframework.orm.metaModel.OrmColumn;
import org.ujoframework.orm.metaModel.OrmDatabase;
import org.ujoframework.orm.metaModel.OrmPKey;
import org.ujoframework.orm.metaModel.OrmParameters;
import org.ujoframework.orm.metaModel.OrmRelation2Many;
import org.ujoframework.orm.metaModel.OrmTable;
import org.ujoframework.criterion.Criterion;
import org.ujoframework.criterion.BinaryCriterion;
import org.ujoframework.criterion.ValueCriterion;

/**
 * The ORM session.
 * <br />Methods of the session are not thread safe.
 * @author Pavel Ponec
 */
@SuppressWarnings(value = "unchecked")
public class Session {

    /** Common title to print the SQL VALUES */
    private static final String SQL_VALUES  = "\n-- SQL VALUES: ";
    /** Exception SQL message prefix */
    private static final String SQL_ILLEGAL = "ILLEGAL SQL: ";

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(Session.class.toString());

    /** Handler. */
    final private OrmHandler handler;
    /** Orm parameters. */
    final private OrmParameters params;

    /** Database connection */
    private Map<OrmDatabase, Connection> connections = new HashMap<OrmDatabase, Connection>(2);

    /** A session cache */
    private Map<CacheKey, OrmUjo> cache;
    
    public Session(OrmHandler handler) {
        this.handler = handler;
        this.params = handler.getParameters();
        this.cache = OrmParameters.CACHE_WEAK.of(params)
            ? new WeakHashMap<CacheKey, OrmUjo>()
            : new HashMap<CacheKey, OrmUjo>()
            ;
    }

    /** Returns a handler */
    final public OrmHandler getHandler() {
        return handler;
    }

    /** Make a commit for all databases. */
    public void commit() {
        commit(true);
    }

    /** Make a rollback for all databases. */
    public void rollback() {
        commit(false);
    }

    /** Make commit/rollback for all databases.
     * @param commit if parameters is false than make a rollback.
     */
    protected void commit(boolean commit) {
        Throwable exception = null;
        OrmDatabase database = null;
        String errMessage = "Can't make commit of DB ";

        for (OrmDatabase db : connections.keySet()) {
            try {
                Connection conn = connections.get(db);
                if (commit) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, errMessage + db, e);
                if (exception == null) {
                    exception = e;
                    database = db;
                }
            }
        }
        if (exception != null) {
            throw new IllegalStateException(errMessage + database, exception);
        }
    }

    /** For all rows. */
    public <UJO extends OrmUjo> Query<UJO> createQuery(Class<UJO> aClass) {
        final Criterion<UJO> criterion = Criterion.newInstance(true);
        return createQuery(aClass, criterion);
    }

    public <UJO extends OrmUjo> Query<UJO> createQuery(Class<UJO> aClass, Criterion<UJO> criterion) {
        return new Query<UJO>(aClass, criterion, this);
    }

    /** The table class is derived from the first criterion column. */
    public <UJO extends OrmUjo> Query<UJO> createQuery(Criterion<UJO> criterion) {
        OrmRelation2Many column = getBasicColumn(criterion);
        OrmTable table = OrmRelation2Many.TABLE.of(column);
        return new Query<UJO>(table, criterion, this);
    }

    /** Returns the first "basic" column of criterion. */
    public OrmRelation2Many getBasicColumn(Criterion criterion) {
        while (criterion.isBinary()) {
            criterion = ((BinaryCriterion) criterion).getLeftNode();
        }

        ValueCriterion exprValue = (ValueCriterion) criterion;
        if (exprValue.getLeftNode()==null) {
            return null;
        }
        UjoProperty property = exprValue.getLeftNode();
        while (!property.isDirect()) {
            property = ((PathProperty) property).getProperty(0);
        }

        OrmRelation2Many result = handler.findColumnModel(property);
        return result;
    }

    /** Returns a Database instance */
    public <DB extends OrmUjo> DB getDatabase(Class<DB> dbType) {
        try {
            DB result = dbType.newInstance();
            result.writeSession(this);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Can't create database from: " + dbType);
        }
    }

    /** INSERT object into table. */
    public void save(OrmUjo ujo) throws IllegalStateException {
        JdbcStatement statement = null;
        String sql = "";

        try {
            OrmTable table = handler.findTableModel((Class) ujo.getClass());
            ujo.writeSession(this);
            table.assignPrimaryKey(ujo);
            OrmDatabase db = OrmTable.DATABASE.of(table);
            sql = db.getDialect().printInsert(ujo, out(128)).toString();
            LOGGER.log(Level.INFO, sql);
            statement = getStatement(db, sql);
            statement.assignValues(ujo);
            LOGGER.log(Level.INFO, SQL_VALUES + statement.getAssignedValues());
            statement.executeUpdate(); // execute insert statement
        } catch (Throwable e) {
            OrmDatabase.close(null, statement, null, false);
            throw new IllegalStateException(SQL_ILLEGAL + sql, e);
        } finally {
            OrmDatabase.close(null, statement, null, true);
        }
    }

    /** UPDATE object into table. */
    public int update(OrmUjo ujo) throws IllegalStateException {
        int result = 0;
        JdbcStatement statement = null;
        String sql = null;

        try {
            OrmTable table = handler.findTableModel((Class) ujo.getClass());
            OrmDatabase db = OrmTable.DATABASE.of(table);
            List<OrmColumn> changedColumns = getOrmColumns(ujo.readChangedProperties(true));
            if (changedColumns.size()==0) {
                LOGGER.warning("No changes to update in the object: " + ujo);
                return result;
            }
            final Criterion criterion = createPkCriterion(ujo);
            final OrmTable ormTable = handler.findTableModel(ujo.getClass());
            final CriterionDecoder decoder = new CriterionDecoder(criterion, ormTable);
            sql = db.getDialect().printUpdate(ormTable, changedColumns, decoder, out(64)).toString();
            statement = getStatement(db, sql);
            statement.assignValues(ujo, changedColumns);
            statement.assignValues(decoder);

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, sql + SQL_VALUES + statement.getAssignedValues());
            }
            result = statement.executeUpdate(); // execute update statement
            ujo.writeSession(this);
        } catch (Throwable e) {
            OrmDatabase.close(null, statement, null, false);
            throw new IllegalStateException(SQL_ILLEGAL + sql, e);
        } finally {
            OrmDatabase.close(null, statement, null, true);
        }
        return result;
    }

    /** Delete all object object form parameter.
     * @param criterion filter for deleting tables.
     * @return Returns a number of the realy deleted objects.
     */
    public <UJO extends OrmUjo> int delete(final Criterion<UJO> criterion) {
        final OrmRelation2Many column = getBasicColumn(criterion);
        final OrmTable table = OrmRelation2Many.TABLE.of(column);
        return delete(table, criterion);
    }


    /** Delete all object object form parameter.
     * @param tableType Type of table to delete
     * @param criterion filter for deleting tables.
     * @return Returns a number of the realy deleted objects.
     */
    public <UJO extends OrmUjo> int delete(final Class<UJO> tableType, final Criterion<UJO> criterion) {
        final OrmTable table = handler.findTableModel(tableType);
        return delete(table, criterion);
    }



    /** Delete all object object form parameter.
     * @param ormUjo Type of table to delete
     * @param criterion filter for deleting tables.
     * @return Returns a number of the realy deleted objects.
     */
    protected <UJO extends OrmUjo> int delete(final OrmTable ormUjo, final Criterion<UJO> criterion) {
        int result = 0;
        JdbcStatement statement = null;
        String sql = "";

        try {
            final OrmDatabase db = OrmTable.DATABASE.of(ormUjo);
            final CriterionDecoder decoder = new CriterionDecoder(criterion, ormUjo);
            sql = db.getDialect().printDelete(ormUjo, decoder, out(64)).toString();
            statement = getStatement(db, sql);
            statement.assignValues(decoder);

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, sql + SQL_VALUES + statement.getAssignedValues());
            }
            result = statement.executeUpdate(); // execute delete statement
        } catch (Throwable e) {
            OrmDatabase.close(null, statement, null, false);
            throw new IllegalStateException(SQL_ILLEGAL + sql, e);
        } finally {
            OrmDatabase.close(null, statement, null, true);
        }
        return result;

    }


    /** Convert a property array to a column list. */
    protected List<OrmColumn> getOrmColumns(UjoProperty... properties) {
        final List<OrmColumn> result = new ArrayList<OrmColumn>(properties.length);

        for (UjoProperty property : properties) {
            OrmRelation2Many column = handler.findColumnModel(property);
            if (column instanceof OrmColumn) {
                result.add((OrmColumn) column);
            }
        }
        return result;
    }

    /** Returns an criterion by a PrimaryKey */
    protected Criterion createPkCriterion(OrmUjo ormUjo) {
        Criterion result = null;
        OrmTable ormTable = handler.findTableModel(ormUjo.getClass());
        OrmPKey ormKey = OrmTable.PK.of(ormTable);
        List<OrmColumn> keys = OrmPKey.COLUMNS.of(ormKey);

        for (OrmColumn ormColumn : keys) {
            Criterion crn = Criterion.newInstance(ormColumn.getProperty(), ormColumn.getValue(ormUjo));
            result = result!=null
                ? result.and(crn)
                : crn
                ;
        }
        return result!=null
            ? result
            : Criterion.newInstance(false)
            ;
    }

    /** Returns a count of rows */
    public <UJO extends OrmUjo> long getRowCount(Query<UJO> query) {
        long result = -1;
        JdbcStatement statement = null;
        ResultSet rs = null;

        OrmTable table = query.getTableModel();
        OrmDatabase db = OrmTable.DATABASE.of(table);
        String sql = "";

        try {
            sql = db.getDialect().printSelect(table, query, true, out(128)).toString();
            LOGGER.log(Level.INFO, sql);

            statement = getStatement(db, sql);
            statement.assignValues(query.getDecoder());
            LOGGER.log(Level.INFO, SQL_VALUES + statement.getAssignedValues());

            rs = statement.executeQuery(); // execute a select statement
            result = rs.next() ? rs.getLong(1) : 0 ;
        } catch (Exception e) {
            throw new RuntimeException(SQL_ILLEGAL + sql, e);
        } finally {
            OrmDatabase.close(null, statement, rs, false);
        }
        return result;
    }

    /** Run SQL SELECT by query. */
    public <UJO extends OrmUjo> UjoIterator<UJO> iterate(Query<UJO> query) {
        JdbcStatement statement = null;
        String sql = "";

        try {
            OrmTable table = query.getTableModel();
            OrmDatabase db = OrmTable.DATABASE.of(table);

            sql = db.getDialect().printSelect(table, query, false, out(128)).toString();
            statement = getStatement(db, sql);
            statement.assignValues(query.getDecoder());

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, sql + SQL_VALUES + statement.getAssignedValues());
            }
            ResultSet rs = statement.executeQuery(); // execute a select statement
            UjoIterator<UJO> result = UjoIterator.getInstance(query, rs);
            return result;

        } catch (Throwable e) {
            throw new IllegalStateException(SQL_ILLEGAL + sql, e);
        }
    }

    public <UJO extends OrmUjo> UJO single(Query query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** Find column by a table type. */
    private OrmColumn findOrmColumn(OrmTable table, Class tableType) {
        for (OrmColumn column : OrmTable.COLUMNS.of(table)) {
            if (column.isForeignKey()
            &&  column.getProperty().getType()==tableType) {
                return column;
            }
        }
        return null;
    }

    /** Iterate property of values
     * @param property Table property
     * @param value A value type of OrmUjo
     */
    public <UJO extends OrmUjo> UjoIterator<UJO> iterateInternal(RelationToMany property, OrmUjo value) {

        final Class tableClass = property.getItemType();
        final OrmTable table   = handler.findTableModel(tableClass);
        final OrmColumn column = findOrmColumn(table, value.getClass());

        if (column==null) {
            OrmTable origTable = handler.findTableModel(value.getClass());
            if (origTable.isPersistent()) {
                String msg = "Can't find a foreign key of " + table + " to a " + value.getClass().getSimpleName();
                throw new IllegalStateException(msg);
            }
        }

        Criterion crit = column!=null
            ? Criterion.newInstance(column.getProperty(), value)
            : Criterion.newInstanceTrue(table.getFirstPK().getProperty())
            ;
        Query query = createQuery(tableClass, crit);
        UjoIterator result = iterate(query);

        return result;
    }

    /** Get connection for a required database and set an autocommit na false. */
    public Connection getConnection(OrmDatabase database) throws IllegalStateException {
        Connection result = connections.get(database);
        if (result == null) {
            try {
                result = database.createConnection();
            } catch (Exception e) {
                throw new IllegalStateException("Can't create an connection for " + database, e);
            }
            connections.put(database, result);
        }
        return result;
    }

    /** Create new statement */
    public JdbcStatement getStatement(OrmDatabase database, CharSequence sql) throws SQLException {
        final JdbcStatement result = new JdbcStatement(getConnection(database), sql);
        return result;
    }



    /**
     * Load UJO by a unique id. If a result is not found then a null value is passed.
     * @param tableType Type of Ujo
     * @param id Value ID
     */
    public <UJO extends OrmUjo> UJO load
        ( final Class<UJO> tableType
        , final Object id
        ) throws NoSuchElementException
    {
        final boolean mandatory = false;
        final OrmTable table = handler.findTableModel(tableType);
        final OrmColumn column = table.getFirstPK();

        Criterion crn = Criterion.newInstance(column.getProperty(), id);
        Query query = createQuery(crn);
        UjoIterator iterator = iterate(query);

        final UJO result
            = (mandatory || iterator.hasNext())
            ? (UJO) iterator.next()
            : null
            ;
        if (iterator.hasNext()) {
            throw new RuntimeException("Ambiguous key " + id);
        }
        return result;
    }

    /**
     * Load UJO by a unique id. If the result is not unique, then an exception is throwed.
     * @param relatedProperty Related property
     * @param id Valud ID
     * @param mandatory If result is mandatory then the method throws an exception if no object was found else returns null;
     */
    public <UJO extends OrmUjo> UJO loadInternal
        ( final UjoProperty relatedProperty
        , final Object id
        , final boolean mandatory
        ) throws NoSuchElementException
    {
        OrmColumn column = (OrmColumn) handler.findColumnModel(relatedProperty);
        List<OrmColumn> columns = column.getForeignColumns();
        if (columns.size() != 1) {
            throw new UnsupportedOperationException("There is supported only a one-column foreign key now: " + column);
        }

        // FIND CACHE:
        boolean cache = params.isCacheEnabled();
        OrmTable tableModel = null;
        if (cache) {
            tableModel = OrmColumn.TABLE.of(columns.get(0));
            OrmUjo r = findCache(OrmTable.DB_PROPERTY.of(tableModel).getItemType(), id);
            if (r!=null) {
                return (UJO) r;
            }
        }

        // SELECT DB
        Criterion crn = Criterion.newInstance(columns.get(0).getProperty(), id);
        Query query = createQuery(crn);
        UjoIterator iterator = iterate(query);

        final UJO result
            = (mandatory || iterator.hasNext())
            ? (UJO) iterator.next()
            : null
            ;
        if (iterator.hasNext()) {
            throw new RuntimeException("Ambiguous key " + id);
        }
        
        if (cache) {
            addCache(result, OrmTable.PK.of(tableModel));
        }
        return result;
    }

    /** Close all DB connections.
     * @throws java.lang.IllegalStateException The exception contains a bug from Connection close;
     */
    public void close() throws IllegalStateException {

        cache = null;
        Throwable exception = null;
        OrmDatabase database = null;
        String errMessage = "Can't close connection for DB ";

        for (OrmDatabase db : connections.keySet()) {
            try {
                Connection conn = connections.get(db);
                conn.close();
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, errMessage + db, e);
                if (exception == null) {
                    exception = e;
                    database = db;
                }
            }
        }
        if (exception != null) {
            throw new IllegalStateException(errMessage + database, exception);
        }
    }

    /** Create new StringBuilder instance */
    private StringBuilder out(int capacity) {
        return new StringBuilder(capacity);
    }

    /** Add value into cache */
    public void addCache(OrmUjo ujo, OrmPKey pkey) {
        CacheKey key = CacheKey.newInstance(ujo, pkey);
        cache.put(key, ujo);
    }


    public OrmUjo findCache(Class type, Object value) {
        CacheKey key = CacheKey.newInstance(type, value);
        return cache.get(key);
    }

    public OrmUjo findCache(Class type, Object... values) {
        CacheKey key = CacheKey.newInstance(type, values);
        return cache.get(key);
    }

    /** Clear the cache. */
    public void cacheClear() {
        new HashMap().clear();
        cache.clear();
    }

    /** Returns parameters */
    final public OrmParameters getParameters() {
        return params;
    }

}

