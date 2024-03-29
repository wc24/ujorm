/*
 *  Copyright 2009-2013 Pavel Ponec
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

package org.ujorm.orm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.criterion.BinaryCriterion;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.Operator;
import org.ujorm.criterion.ValueCriterion;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaDatabase;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.orm.metaModel.MoreParams;

/**
 * SQL Criterion Decoder.
 * @author Pavel Ponec
 * @composed 1 - 1 SqlDialect
 */
public class CriterionDecoder {

    final protected OrmHandler handler;
    final protected SqlDialect dialect;

    final protected Criterion criterion;
    final protected List<Key> orderBy;
    final protected StringBuilder sql;
    final protected List<ValueCriterion> values;
    final protected List<ValueCriterion> nullValues;
    final protected Set<MetaTable> tables;
    final protected MetaTable baseTable;
    /** EFFECTIVA REQUEST: to enforce printing all Ujorm joined tables */
    final protected boolean printAllJoinedTables;

    /**
     * Constructor
     * @param criterion Criterion non-null
     * @param baseTable Base ORM table model
     */
    public CriterionDecoder(Criterion criterion, MetaTable baseTable) {
        this(criterion, baseTable, null);
    }

    /**
     * Constructor
     * @param criterion Criterion non-null
     * @param baseTable Base ORM table model
     * @param orderByItems The order item list is not mandatory (can be null).
     */
    public CriterionDecoder(Criterion criterion, MetaTable baseTable, List<Key> orderByItems) {
        final MetaDatabase database = baseTable.getDatabase();
        this.baseTable = baseTable;
        this.criterion = criterion;
        this.dialect = database.getDialect();
        this.orderBy = orderByItems;
        this.handler = database.getOrmHandler();
        this.sql = new StringBuilder(64);
        this.values = new ArrayList<ValueCriterion>();
        this.nullValues = new ArrayList<ValueCriterion>();
        this.tables = new HashSet<MetaTable>();
        this.tables.add(baseTable);
        this.printAllJoinedTables = MetaParams.MORE_PARAMS.add(MoreParams.PRINT_All_JOINED_TABLES).of(handler.getParameters());

        if (this.criterion!=null) {
            unpack(this.criterion);
        }

        if (this.criterion!=null
        ||  this.orderBy!=null) {
            writeRelations();
        }
    }

    /** Unpack criterion. */
    protected void unpack(final Criterion c) {
        if (c.isBinary()) {
            unpackBinary((BinaryCriterion)c);
        } else try {
            final ValueCriterion origCriterion = (ValueCriterion) c;
            final ValueCriterion newCriterion = dialect.printCriterion(origCriterion, sql);
            if (newCriterion!=null) {
                values.add(newCriterion);
            } else if (origCriterion != null) {
                nullValues.add(origCriterion);
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** Unpack criterion. */
    @SuppressWarnings("fallthrough")
    private void unpackBinary(final BinaryCriterion eb) {

        boolean or = false;
        switch (eb.getOperator()) {
            case OR:
                or = true;
            case AND:
                if (or) sql.append(" (");
                unpack(eb.getLeftNode());
                sql.append(" ");
                sql.append(eb.getOperator().name());
                sql.append(" ");
                unpack(eb.getRightNode());
                if (or) sql.append(") ");
                break;
            default:
                String message = "Operator is not supported in the SQL statement: " + eb.getOperator();
                throw new UnsupportedOperationException(message);
        }
    }

    /** Returns a column count */
    public int getColumnCount() {
        return values.size();
    }

    /** Returns direct column or throw an exception */
    public MetaColumn getColumn(int i) throws IllegalArgumentException {
        final Key p = values.get(i).getLeftNode();
        final MetaColumn ormColumn = (MetaColumn) handler.findColumnModel(p, true);
        return ormColumn;
    }

    /** Returns operator. */
    public Operator getOperator(int i) {
        final Operator result = values.get(i).getOperator();
        return result;
    }

    /** Returns value */
    public Object getValue(int i) {
        Object result = values.get(i).getRightNode();
        return result;
    }

    /** Returns an extended value to the SQL statement */
    public Object getValueExtended(int i) {
        ValueCriterion crit = values.get(i);
        Object value = crit.getRightNode();

        if (value==null) {
            return value;
        }
        if (crit.isInsensitive()) {
            value = value.toString().toUpperCase();
        }
        switch (crit.getOperator()) {
            case CONTAINS:
            case CONTAINS_CASE_INSENSITIVE:
                return "%"+value+"%";
            case STARTS:
            case STARTS_CASE_INSENSITIVE:
                return value+"%";
            case ENDS:
            case ENDS_CASE_INSENSITIVE:
                return "%"+value;
            default:
                return value;
        }
    }

    /** Returns the criterion from costructor. */
    public Criterion getCriterion() {
        return criterion;
    }

    /** Returns a SQL WHERE 'expression' of an empty string if no conditon is found. */
    public String getWhere() {
        return sql.toString();
    }

    /** Is the SQL statement empty?  */
    public boolean isEmpty() {
        return sql.length()==0;
    }

    /** Returns the first direct property. */
    public Key getBaseProperty() {
        Key result = null;
        for (ValueCriterion eval : values) {
            if (eval.getLeftNode()!=null) {
                result = eval.getLeftNode();
                break;
            }
        }
        while(result instanceof CompositeKey) {
            result = ((CompositeKey)result).getFirstKey();
        }
        return result;
    }

    /** Writer a relation conditions: */
    @SuppressWarnings("unchecked")
    protected void writeRelations() {
        Key[] relations = getPropertyRelations();

        boolean parenthesis = sql.length()>0 && relations.length>0;
        if (parenthesis) {
            sql.append(" AND (");
        }

        boolean andOperator = false;
        for (Key property : relations) try {
            final MetaColumn fk1 = (MetaColumn) handler.findColumnModel(property, true);
            final List<MetaColumn> pk2 = fk1.getForeignColumns();
            final MetaTable tab2 = pk2.get(0).getTable();
            //
            tables.add(MetaColumn.TABLE.of(fk1));
            tables.add(tab2);

            for (int i=fk1.getForeignColumns().size()-1; i>=0; i--) {

                if (andOperator) {
                    sql.append(" AND ");
                } else {
                    andOperator=true;
                }

                fk1.printForeignColumnFullName(i, sql);
                sql.append(" = ");
                dialect.printColumnAlias(pk2.get(i), sql);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        if (parenthesis) {
            sql.append(")");
        }
    }

    /** Returns the unique direct property relations. */
    @SuppressWarnings("unchecked")
    protected Key[] getPropertyRelations() {
        Set<Key> result = new HashSet<Key>();
        ArrayList<Key> dirs = new ArrayList<Key>();
        ArrayList<ValueCriterion> allValues = new ArrayList<ValueCriterion>(values.size() + nullValues.size());
        allValues.addAll(values);
        allValues.addAll(nullValues);

        for (ValueCriterion value : allValues) {
            Key p1 = value.getLeftNode();
            Object p2 = value.getRightNode();

            if (p1 != null) {
                if (p1.isComposite()) {
                    ((CompositeKey) p1).exportKeys(dirs);
                    dirs.remove(dirs.size()-1); // remove the last direct property
                }
                if (p2 instanceof CompositeKey) {
                    ((CompositeKey) p2).exportKeys(dirs);
                    dirs.remove(dirs.size()-1); // remove the last direct property
                }
            }
        }

        // Get relations from the 'order by':
        if (orderBy!=null) {
            for (Key p1 : orderBy) {
                if (p1.isComposite()) {
                    ((CompositeKey) p1).exportKeys(dirs);
                    dirs.remove(dirs.size()-1); // remove the last direct property
                }
            }
        }

        result.addAll(dirs);
        return result.toArray(new Key[result.size()]);
    }

    /** Get Base Table */
    public MetaTable getBaseTable() {
        return baseTable;
    }

    /** Returns all participated tables include the parameter table. */
    public int getTableCount() {
        return tables.size();
    }

    /** Returns all participated tables include the parameter table. */
    public TableWrapper[] getTables() {
        if (printAllJoinedTables) {
            Set<MetaTable> result = new HashSet<MetaTable>();
            result.addAll(tables);

            //EFFECTIVA REQUEST: TR-1771: to enforce printing Ujorm joined tables
            ArrayList<ValueCriterion> allValues = new ArrayList<ValueCriterion>(values.size() + nullValues.size());
            allValues.addAll(values);
            allValues.addAll(nullValues);
            for (ValueCriterion value : allValues) {
                Object o1 = value.getLeftNode();
                Object o2 = value.getRightNode();
                if (o1 instanceof Key) {
                    MetaTable table = handler.findColumnModel((Key) o1).getTable();
                    result.add(table);
                }
                if (o2 instanceof Key) {
                    MetaTable table = handler.findColumnModel((Key) o2).getTable();
                    result.add(table);
                }
            }

            return result.toArray(new TableWrapper[result.size()]);
        } else {
            // The original Ujorm code:
            return tables.toArray(new TableWrapper[tables.size()]);
        }
    }

    /** Returns all participated tables include the parameter table. The 'baseTable' is on the first position always. */
    public TableWrapper[] getTablesSorted() {
        final TableWrapper[] result = getTables();
        if (result.length > 1 && result[0] != baseTable) {
            for (int i = result.length - 1; i >= 1; i--) {
                if (result[i] == baseTable) {
                    result[i] = result[0];
                    result[0] = baseTable;
                }
            }
        }
        return result;
    }

    /** Returns handler */
    public OrmHandler getHandler() {
        return handler;
    }

    /** Returns the criterion */
    @Override
    public String toString() {
        return criterion!=null ? criterion.toString() : null ;
    }
}
