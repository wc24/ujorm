/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.orm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.orm.metaModel.OrmDatabase;

/**
 * Sequence provider
 * @author pavel
 */
public class UjoSequencer {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(UjoSequencer.class.toString());

    final private OrmDatabase database;
    private long sequence = 0;
    private long seqLimit = 0;
    private int step = 20;

    public UjoSequencer(OrmDatabase database) {
        this.database = database;
    }

    protected void loadBuffer() {

    }

    /** Returns the <strong>next sequence value</strong> */
    public synchronized long nextValue(final Session session) {

        if (sequence<seqLimit) {
            return ++sequence;
        } else {
            JdbcStatement statement = null;
            ResultSet res = null;            
            String sql = "";
            try {
                sql = database.getRenderer().printSeqNextValue(this, new StringBuilder(64)).toString();
                statement = session.getStatement(database, sql);
                res = statement.executeQuery();
                res.next();
                sequence = res.getLong(1) + 1; // The last assigned number + 1;
                seqLimit = res.getLong(2);
            } catch (Throwable e) {
                throw new IllegalStateException("ILLEGAL SQL: " + sql, e);
            } finally {
                OrmDatabase.close(null, statement, res, true);
            }
            return sequence;
        }
    }

    /** Returns a next sequence value */
    public synchronized void createSequence(Connection conn) {
        Statement stat = null;
        ResultSet res = null;
        String sql = "";
        try {
            sql = database.getRenderer().printCreateSequence(this, new StringBuilder(30)).toString();
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info(sql);
            }

            stat = conn.createStatement();
            stat.executeUpdate(sql);
            conn.commit();
        } catch (Throwable e) {
            throw new IllegalStateException("ILLEGAL SQL: " + sql, e);
        } finally {
            OrmDatabase.close(null, stat, res, true);
        }
    }


    /** Returns sequence name */
    public String getSequenceName() {
        return "UjoOrmSequence";
    }

    /** Returns a database name */
    public String getDatabasName() {
        return OrmDatabase.NAME.of(database);
    }

    /** Returns an init sequence value. */
    public long getInitValue() {
        return getInitIncrement();
    }

    /** The UJO cache is the number of pre-allocated numbers. */
    public int getInitIncrement() {
        return 32;
    }

    /** The DB cache is the number of pre-allocated numbers */
    public int getInitCacheSize() {
        return 0;
    }




}