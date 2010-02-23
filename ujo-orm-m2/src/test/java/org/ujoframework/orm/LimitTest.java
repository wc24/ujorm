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

package org.ujoframework.orm;

import java.util.Date;
import junit.framework.TestCase;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.criterion.Criterion;
import org.ujoframework.criterion.Operator;
import org.ujoframework.orm_tutorial.sample.Database;
import org.ujoframework.orm_tutorial.sample.Item;
import org.ujoframework.orm_tutorial.sample.Order;

/**
 *
 * @author Pavel Ponec
 */
public class LimitTest extends TestCase {
    
    public LimitTest(String testName) {
        super(testName);
    }


    private static Class suite() {
        return LimitTest.class;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ----------------------------------------------------


    /** Using INSERT */
    public void useCreateItem() {

        OrmHandler.getInstance().loadDatabase(Database.class);
        Session session = OrmHandler.getInstance().getSession();

        Order order = new Order();
        Order.CREATED.setValue(order, new Date());
        Order.DESCR.setValue(order, "test order");

        Item item = new Item();
        Item.DESCR.setValue(item, "yellow table");
        Item.ORDER.setValue(item, order);

        session.save(order);
        session.save(item);

        if (true) {
           session.commit();
        } else {
           session.rollback();
        }
    }

    /** Using SELECT by an object relations */
    public void useRelation() {
        Session session = OrmHandler.getInstance().getSession();
        Database db = session.getDatabase(Database.class);

        UjoIterator<Order> orders  = Database.ORDERS.of(db);
        for (Order order : orders) {
            Long id = Order.ID.of(order);
            String descr = Order.DESCR.of(order);
            System.out.println("Order id: " + id + " descr: " + descr);

            for (Item item : Order.ITEMS.of(order)) {
                Long itemId = Item.ID.of(item);
                String itemDescr = Item.DESCR.of(item);
                System.out.println(" Item id: " + itemId + " descr: " + itemDescr);
            }
        }
    }

    /** Using SELECT by QUERY */
    public void useSelection() {
        Session session = OrmHandler.getInstance().getSession();

        Criterion<Order> crn1 = Criterion.newInstance(Order.DESCR, "test order");
        Criterion<Order> crn2 = Criterion.newInstance(Order.CREATED, Operator.LE, new Date());
        Criterion<Order> crit = crn1.and(crn2);

        Query<Order> query = session.createQuery(Order.class, crit);

        for (Order o : query.iterate()) {
            Long id = Order.ID.of(o);
            String descr = Order.DESCR.of(o);
            System.out.println("Order id: " + id + " descr: " + descr);
        }
    }


    // -----------------------------------------------------


    /**
     * Test of getItemCount method, of class AbstractPropertyList.
     */
    public void testGetItemCount() {
    }
 

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }

}