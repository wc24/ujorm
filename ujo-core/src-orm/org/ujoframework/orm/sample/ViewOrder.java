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

package org.ujoframework.orm.sample;


import java.util.Date;
import org.ujoframework.UjoProperty;
import org.ujoframework.orm.annot.Column;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.annot.View;


/**
 * The column mapping to FROM view.
 * @hidden
 */
@View(select="SELECT ord_order.id, count(*) AS itemCount"
    + " FROM db1.ord_order ord_order, db1.ord_item ord_item"
    + " WHERE ord_order.id = ord_item.fk_order"
    + " GROUP BY ord_order.id"
    + " ORDER BY ord_order.id")
public class ViewOrder extends TableUjo<ViewOrder> {

    /** Unique key */
    @Column(pk=true)
    public static final UjoProperty<ViewOrder,Long> ID = newProperty("id", Long.class);
    /** ItemCount */
    public static final UjoProperty<ViewOrder,Integer> ITEM_COUNT = newProperty("itemCount", 0);

}