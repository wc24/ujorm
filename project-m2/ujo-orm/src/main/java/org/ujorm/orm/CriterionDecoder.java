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

package org.ujorm.orm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ujorm.UjoProperty;
import org.ujorm.CompositeProperty;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaDatabase;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.BinaryCriterion;
import org.ujorm.criterion.ValueCriterion;
import org.ujorm.criterion.Operator;

/**
 * SQL Criterion Decoder.
 * @author Pavel Ponec
 * @composed 1 - 1 SqlDialect
 */
public class CriterionDecoder {

    final private OrmHandler handler;
    final private SqlDialect dialect;

    final private Criterion criterion;
    final private List<UjoProperty> orderBy;
    final private StringBuilder sql;
    final private List<ValueCriterion> values;
    final private Set<MetaTable> tables;

    public CriterionDecoder(Criterion e, MetaTable ormTable) {
        this(e, ormTable.getDatabase(), null);
    }

    /**
     * @param orderBy The order item list is not mandatory (can be null).
     */
    public CriterionDecoder(Criterion criterion, MetaDatabase database, List<UjoProperty> orderByItems) {
        this.criterion = criterion;
        this.dialect = database.getDialect();
        this.orderBy = orderByItems;
        this.handler = database.getOrmHandler();
        this.sql = new StringBuilder(64);
        this.values = new ArrayList<ValueCriterion>();
        this.tables = new HashSet<MetaTable>();

        if (criterion!=null) {
            unpack(criterion);
            writeRelations();
        }
    }

    /** Unpack criterion. */
    protected final void unpack(final Criterion c) {
        if (c.isBinary()) {
            unpackBinary((BinaryCriterion)c);
        } else try {
            ValueCriterion value = dialect.printCriterion((ValueCriterion) c, sql);
            if (value!=null) {
                values.add(value);
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

    /** Returns direct column */
    public MetaColumn getColumn(int i) {
        UjoProperty p = values.get(i).getLeftNode();
        MetaColumn ormColumn = (MetaColumn) handler.findColumnModel(p);
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
    public UjoProperty getBaseProperty() {
        UjoProperty result = null;
        for (ValueCriterion eval : values) {
            if (eval.getLeftNode()!=null) {
                result = eval.getLeftNode();
                break;
            }
        }
        while(result!=null
        &&   !result.isDirect()
        ){
            result = ((CompositeProperty)result).getFirstProperty();
        }
        return result;
    }

    /** Writer a relation conditions: */
    @SuppressWarnings("unchecked")
    protected final void writeRelations() {
        UjoProperty[] relations = getPropertyRelations();

        boolean parenthesis = sql.length()>0 && relations.length>0;
        if (parenthesis) {
            sql.append(" AND (");
        }

        boolean andOperator = false;
        for (UjoProperty property : relations) try {
            MetaColumn fk1 = (MetaColumn) handler.findColumnModel(property);
            List<MetaColumn> pk2 = fk1.getForeignColumns();
            MetaTable tab2 =pk2.get(0).getTable();
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
    protected UjoProperty[] getPropertyRelations() {
        Set<UjoProperty> result = new HashSet<UjoProperty>();
        ArrayList<UjoProperty> dirs = new ArrayList<UjoProperty>();

        for (ValueCriterion value : values) {
            UjoProperty p1 = value.getLeftNode();
            Object      p2 = value.getRightNode();

            if (!p1.isDirect()) {
                ((CompositeProperty) p1).exportProperties(dirs);
                dirs.remove(dirs.size()-1); // remove the last direct property
            }
            if (p2 instanceof CompositeProperty) {
                ((CompositeProperty) p2).exportProperties(dirs);
                dirs.remove(dirs.size()-1); // remove the last direct property
            }
        }

        // Get relations from the 'order by':
        if (orderBy!=null) {
            for (UjoProperty p1 : orderBy) {
                if (!p1.isDirect()) {
                    ((CompositeProperty) p1).exportProperties(dirs);
                    dirs.remove(dirs.size()-1); // remove the last direct property
                }
            }
        }

        result.addAll(dirs);
        return result.toArray(new UjoProperty[result.size()]);
    }

    /** Returns all participated tables include the parameter table. */
    public MetaTable[] getTables(MetaTable baseTable) {
        tables.add(baseTable);
        return tables.toArray(new MetaTable[tables.size()]);
    }

    /** Returns all participated tables include the parameter table. The 'baseTable' is on the first position always. */
    public MetaTable[] getTablesSorted(final MetaTable baseTable) {
        MetaTable[] result = getTables(baseTable);
        if (result.length>1 && result[0]!=baseTable) {
            Arrays.sort(result, new Comparator<MetaTable>() {
                @Override public int compare(final MetaTable o1, final MetaTable o2) {                    
                    return o1==baseTable ? -1 : o2==baseTable ? 1 : 0;
                }
            });
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