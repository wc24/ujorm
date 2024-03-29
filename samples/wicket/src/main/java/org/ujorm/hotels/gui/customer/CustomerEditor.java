/*
 * Copyright 2013, Pavel Ponec
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
package org.ujorm.hotels.gui.customer;

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.services.AuthService;
import org.ujorm.wicket.component.dialog.domestic.EntityDialogPane;
import org.ujorm.wicket.component.tools.LocalizedModel;

/**
 * Customer Editor
 * @author Pavel Ponec
 */
public class CustomerEditor extends EntityDialogPane<Customer> {
    private static final long serialVersionUID = 0L;

    @SpringBean private AuthService authService;

    public CustomerEditor(ModalWindow modalWindow, IModel<Customer> model) {
        super(modalWindow, model);

        // Editable fields:
        fields.add(Customer.LOGIN);
        fields.add(Customer.PASSWORD);
        fields.add(Customer.TITLE);
        fields.add(Customer.FIRSTNAME);
        fields.add(Customer.SURENAME);
        fields.add(Customer.EMAIL);
        fields.add(Customer.ADMIN);
        fields.add(Customer.ACTIVE);
    }

    /** Modify attribute(s): */
    @Override
    protected void onBeforeRender() {
        final boolean newMode = isNew();
        fields.setEnabled(Customer.LOGIN, newMode);
        fields.setVisible(Customer.ACTIVE, !newMode);
        fields.setVisible(Customer.ADMIN, authService.isAdmin());
        super.onBeforeRender();
    }

    /** Dialog for a new Customer */
    private boolean isNew() {
        return ((Customer)getDefaultModelObject()).getId() == null;
    }

    /** Create the editor dialog */
    public static CustomerEditor create(String componentId, int width, int height) {
        IModel<Customer> model = Model.of(new Customer());
        final ModalWindow modalWindow = new ModalWindow(componentId, model);
        modalWindow.setCssClassName(ModalWindow.CSS_CLASS_BLUE);

        final CustomerEditor result = new CustomerEditor(modalWindow, model);
        modalWindow.setInitialWidth(width);
        modalWindow.setInitialHeight(height);
        modalWindow.setTitle(new LocalizedModel("dialog.edit.title"));
        //modalWindow.setCookieName("modal-dialog");

        return result;
    }
}
