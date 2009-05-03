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


import org.ujoframework.UjoProperty;
import org.ujoframework.orm.annot.Column;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.annot.View;


/**
 * The column mapping to FROM view.
 */
@View(select="SELECT count(*) AS itemCount, id, usrId FROM db1.bo_order GROUP BY id ORDER BY id")
public class ViewOrder extends TableUjo<ViewOrder> {

    /** Unique key */
    @Column(pk=true)
    public static final UjoProperty<ViewOrder,Long> ID = newProperty("id", Long.class);
    /** User key */
    public static final UjoProperty<ViewOrder,Integer> USER_ID = newProperty("usrId", Integer.class);
    /** ItemCount */
    public static final UjoProperty<ViewOrder,Integer> ITEM_COUNT = newProperty("itemCount", 0);

}
