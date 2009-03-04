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

package org.ujoframework.orm.metaModel;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.annot.Transient;
import org.ujoframework.core.annot.XmlAttribute;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.orm.DbType;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.implementation.orm.RelationToMany;
import java.sql.*;
import org.ujoframework.orm.DbHandler;
import org.ujoframework.orm.JdbcStatement;
import org.ujoframework.orm.Query;
import org.ujoframework.orm.SqlRenderer;
import org.ujoframework.orm.annot.Db;

/**
 * A logical database description.
 * @author pavel
 */
public class DbModel extends AbstractMetaModel {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(DbModel.class.toString());


    /** DbModel name */
    @XmlAttribute
    public static final UjoProperty<DbModel,String> NAME = newProperty("name", "");
    /**  SQL renderer type of SqlRenderer. */
    public static final UjoProperty<DbModel,Class> RENDERER = newProperty("renderer", Class.class);
    /** List of tables */
    public static final ListProperty<DbModel,DbTable> TABLES = newPropertyList("table", DbTable.class);
    /** JDBC URL connection */
    public static final UjoProperty<DbModel,String> JDBC_URL = newProperty("jdbcUrl", "");
    /** DB user */
    public static final UjoProperty<DbModel,String> USER = newProperty("user", "");
    /** DB password */
    @Transient
    public static final UjoProperty<DbModel,String> PASSWORD = newProperty("password", "");
    /** DB class root instance */
    @Transient
    public static final UjoProperty<DbModel,TableUjo> ROOT = newProperty("root", TableUjo.class);
    /** LDPA */
    public static final UjoProperty<DbModel,String> LDAP = newProperty("ldap", "");

    // --------------------

    private SqlRenderer renderer;

    public DbModel(TableUjo database) {
        ROOT.setValue(this, database);

        Db annotDB = database.getClass().getAnnotation(Db.class);
        if (annotDB!=null) {
            NAME.setValue(this, annotDB.name());
            RENDERER.setValue(this, annotDB.renderer());
            JDBC_URL.setValue(this, annotDB.jdbcUrl());
            USER.setValue(this, annotDB.user());
            PASSWORD.setValue(this, annotDB.password());
            LDAP.setValue(this, annotDB.ldap());
        }
        if (NAME.isDefault(this)) {
            NAME.setValue(this, database.getClass().getSimpleName());
        }
        if (JDBC_URL.isDefault(this)) {
            JDBC_URL.setValue(this, getRenderer().getJdbcUrl());
        }


        for (UjoProperty tableProperty : database.readProperties()) {

            if (tableProperty instanceof RelationToMany) {
                RelationToMany tProperty = (RelationToMany) tableProperty;

                DbTable table = new DbTable(this, tProperty);
                TABLES.addItem(this, table);
            }
        }
    }

    /** Create an SQL insert */
    public String createInsert(TableUjo ujo) {
        SqlRenderer renderer = getRenderer();
        StringBuilder result = new StringBuilder();
        try {
            renderer.printInsert(ujo, result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result.toString();
    }


    /** Create an SQL select */
    public String createSelect(Query query) {
        // TODO;
        return "";
    }

    /** Returns an SQL renderer. */
    public SqlRenderer getRenderer() {
        if (renderer==null) try {
            renderer = (SqlRenderer) RENDERER.of(this).newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Can't create an instance of " + renderer, e);
        }
        return renderer;
    }



    /** Change DbType by a Java property */
    public void changeDbType(DbColumn column) {
       UjoProperty property = DbColumn.TABLE_PROPERTY.of(column);

       Class type = property.getType();

        if (String.class==type) {
            DbColumn.DB_TYPE.setValue(column, DbType.VARCHAR);
        }
        else if (Integer.class==type) {
            DbColumn.DB_TYPE.setValue(column, DbType.INT);
        }
        else if (Long.class==type) {
            DbColumn.DB_TYPE.setValue(column, DbType.BIGINT);
        }
        else if (BigInteger.class.isAssignableFrom(type)) {
            DbColumn.DB_TYPE.setValue(column, DbType.BIGINT);
        }
        else if (Double.class==type || BigDecimal.class==type) {
            DbColumn.DB_TYPE.setValue(column, DbType.DECIMAL);
        }
        else if (java.sql.Date.class.isAssignableFrom(type)) {
            DbColumn.DB_TYPE.setValue(column, DbType.DATE);
        }
        else if (Date.class.isAssignableFrom(type)) {
            DbColumn.DB_TYPE.setValue(column, DbType.TIMESTAMP);
        }
        else if (TableUjo.class.isAssignableFrom(type)) {
            DbColumn.DB_TYPE.setValue(column, DbType.INT);
        }
    }

    /** Change DbType by a Java property */
    public void changeDbLength(DbColumn column) {

        switch (DbColumn.DB_TYPE.of(column)) {
            case DECIMAL:
                changeDefault(column, DbColumn.MAX_LENGTH, 8);
                changeDefault(column, DbColumn.PRECISION, 2);
                break;
            case VARCHAR:
            case VARCHAR_IGNORECASE:
                changeDefault(column, DbColumn.MAX_LENGTH, 128);
                break;
            default:
        }


       UjoProperty property = DbColumn.TABLE_PROPERTY.of(column);

       Class type = property.getType();

        if (String.class==type) {
            DbColumn.DB_TYPE.setValue(column, DbType.VARCHAR);
            changeDefault(column, DbColumn.MAX_LENGTH, 128);
        }
        else if (Integer.class==type) {
            DbColumn.DB_TYPE.setValue(column, DbType.INT);
            changeDefault(column, DbColumn.MAX_LENGTH, 8);
        }
        else if (Long.class==type) {
            DbColumn.DB_TYPE.setValue(column, DbType.BIGINT);
            //changeDefault(column, DbColumn.MAX_LENGTH, 16);
        }
        else if (BigInteger.class.isAssignableFrom(type)) {
            DbColumn.DB_TYPE.setValue(column, DbType.BIGINT);
            changeDefault(column, DbColumn.MAX_LENGTH, 16);
        }
        else if (Double.class==type) {
            DbColumn.DB_TYPE.setValue(column, DbType.DECIMAL);
            changeDefault(column, DbColumn.MAX_LENGTH, 8);
            changeDefault(column, DbColumn.PRECISION, 2);
        }
        else if (BigDecimal.class==type) {
            DbColumn.DB_TYPE.setValue(column, DbType.DECIMAL);
            changeDefault(column, DbColumn.MAX_LENGTH, 8);
            changeDefault(column, DbColumn.PRECISION, 2);
        }
        else if (java.sql.Date.class.isAssignableFrom(type)) {
            DbColumn.DB_TYPE.setValue(column, DbType.DATE);
        }
        else if (Date.class.isAssignableFrom(type)) {
            DbColumn.DB_TYPE.setValue(column, DbType.TIMESTAMP);
        }
        else if (TableUjo.class.isAssignableFrom(type)) {
            DbColumn.DB_TYPE.setValue(column, DbType.INT);
        }
    }


    /** Vytvo�� DB */
    public void create() {
        Connection conn = DbHandler.getInstance().getSession().getConnection(this);
        Statement stat = null;
        StringBuilder sql = new StringBuilder(256);
        try {
            getRenderer().createDatabase(this, sql);
            conn = createConnection();
            stat = conn.createStatement();
            stat.executeUpdate(sql.toString());
            conn.commit();

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info(sql.toString());
            }

        } catch (Throwable e) {
            if (conn!=null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "Can't rollback DB" + toString(), ex);
                }
            }
            throw new IllegalArgumentException("Statement error:\n" + sql, e);
        } finally {
            try {
                close(conn, stat, null, true);
            } catch (IllegalStateException ex) {
                LOGGER.log(Level.WARNING, "Can't rollback DB" + toString(), ex);
            }
        }
    }

    /** Close a connection, statement and a result set. */
    public static void close(Connection connection, JdbcStatement statement, ResultSet rs, boolean throwExcepton) throws IllegalStateException {

        try {

            try {
                if (rs != null) {
                    rs.close();
                }
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                } finally {
                    if (connection != null) {
                        connection.close();
                    }
                }
            }
        } catch (Throwable e) {
            String msg = "Can't close a SQL object";
            if (throwExcepton) {
                throw new IllegalStateException(msg, e);
            } else {
                LOGGER.log(Level.SEVERE, msg, e);
            }
        }
    }

    /** Close a connection, statement and a result set. */
    public static void close(Connection connection, Statement statement, ResultSet rs, boolean throwExcepton) throws IllegalStateException {

        try {
            try {
                if (rs != null) {
                    rs.close();
                }
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                } finally {
                    if (connection != null) {
                        connection.close();
                    }
                }
            }
        } catch (Throwable e) {
            String msg = "Can't close a SQL object";
            if (throwExcepton) {
                throw new IllegalStateException(msg, e);
            } else {
                LOGGER.log(Level.SEVERE, msg, e);
            }
        }
    }


    /** Returns a NAME of the DbModel. */
    @Override
    public String toString() {
        return NAME.of(this);
    }

    /** Create connection */
    public Connection createConnection() throws ClassNotFoundException, SQLException {
        Class.forName(getRenderer().getJdbcDriver());
        final Connection result = DriverManager.getConnection
            ( JDBC_URL.of(this)
            , USER.of(this)
            , PASSWORD.of(this)
            );
        return result;
    }

    /** Equals */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DbModel) {
            DbModel db = (DbModel) obj;

            final String name1 = DbModel.NAME.of(this);
            final String name2 = DbModel.NAME.of(db);

            return name1.equals(name2);
        } else {
            return false;
        }
    }

    /** Hash code */
    @Override
    public int hashCode() {
        final String name = DbModel.NAME.of(this);
        return name.hashCode();
    }



}