/*
 ** Copyright 2013, Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.hotels.gui.hotel;

import org.apache.wicket.markup.html.panel.Panel;
import org.ujorm.hotels.entity.City;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.wicket.component.gridView.KeyColumn;
import org.ujorm.wicket.component.gridView.UjoDataProvider;
import static org.ujorm.wicket.component.gridView.KeyColumn.*;

/**
 * HotelPanel tab
 * @author Pavel Ponec
 */
public class HotelPanel extends Panel {

    public HotelPanel(String id) {
        super(id);

        UjoDataProvider<Hotel> dataProvider = UjoDataProvider.of(Hotel.ACTIVE.whereEq(true));
        
        dataProvider.addColumn(Hotel.NAME);
        dataProvider.addColumn(Hotel.CITY.add(City.NAME)); // An example of relations
        dataProvider.addColumn(Hotel.STREET);
        dataProvider.addColumn(Hotel.PRICE);
        dataProvider.addColumn(KeyColumn.of(Hotel.CURRENCY, SORTING_OFF));
        dataProvider.addColumn(Hotel.STARS);
        dataProvider.addColumn(Hotel.PHONE);
        dataProvider.setSort(Hotel.NAME);
        
        add(dataProvider.createDataTable("datatable", 10));
    }
}
