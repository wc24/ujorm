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
package org.ujorm.wicket.component.dialog;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.ujorm.wicket.CssAppender;
import org.ujorm.wicket.UjoEvent;

/**
 * Abstract Message Dialog Content
 * @author Pavel Ponec
 */
public abstract class AbstractDialogContent<T> extends Panel {
    private static final long serialVersionUID = 20130621L;

    protected static final String BUTTON_PREFIX = "button.";
    protected static final String ACTION_BUTTON_ID = "actionButton";
    protected static final String CANCEL_BUTTON_ID = "cancelButton";
    protected static final String REPEATER_ID = "repeater";

    /** Dialog form */
    protected final Form<?> form;
    /** Dialog modal window */
    protected final ModalWindow modalWindow;
    /** Dialog repeater */
    protected final RepeatingView repeater;
    /** Action code */
    private String action = "";

    public AbstractDialogContent(ModalWindow modalWindow, IModel<T> model) {
        super(modalWindow.getContentId(), model);
        this.modalWindow = modalWindow;
        this.setOutputMarkupId(true);
        this.setOutputMarkupPlaceholderTag(true);

        // Form:
        this.add(form = new Form("dialogForm"));
        form.setOutputMarkupId(true);
        form.add(createSaveButton(ACTION_BUTTON_ID, "save"));
        form.add(createCancelButton(CANCEL_BUTTON_ID, "cancel"));

        // Dialog content:
        form.add(repeater = new RepeatingView(REPEATER_ID));

        // Set content to a Modal window:
        modalWindow.setContent(this);
    }

    /** Action code */
    public String getAction() {
        return action;
    }

    /** Action code */
    public void setAction(String action) {
        this.action = action;
    }

    /** Returns a base model object / entity */
    public T getBaseModelObject() {
        return (T) getDefaultModelObject();
    }

    /** Vytvoří textfield pro aktuání model */
    private AjaxButton createSaveButton(String id, String propertyName) {
        final AjaxButton result = new AjaxButton
                ( id
                , getButtonModel(propertyName)
                , form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
                modalWindow.close(target);
                send(getPage(), Broadcast.BREADTH, new UjoEvent<T>(getAction(), getBaseModelObject(), target));
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
            }
        };
        result.add(new CssAppender("btn btn-primary"));
        form.setDefaultButton(result);
        return result;
    }

    /** Vytvoří textfield pro aktuání model */
    private AjaxButton createCancelButton(String id, String propertyName) {
        final AjaxButton result = new AjaxButton
                ( id
                , getButtonModel(propertyName)
                , form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                close(target, form);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                close(target, form);
            }

            /** Close action */
            private void close(AjaxRequestTarget target, Form<?> form) {
                form.clearInput();
                target.add(form);
                modalWindow.close(target);
            }
        };
        result.add(new CssAppender("btn"));
        return result;
    }

    /**
     * Show dialog and assign a data from domain object
     * @param domain Domain object
     * @param target target
     */
    public void show(IModel<T> body, AjaxRequestTarget target) {
        show(null, body, null, target);
    }

    /**
     * Show dialog and assign a data from domain object
     * @param domain Domain object
     * @param title Window title
     * @param target target
     */
    public void show(IModel<String> title, IModel<T> body, AjaxRequestTarget target) {
        show(title, body, null, target);
    }

    /**
     * Show dialog and assign a data from domain object
     * @param title Dialog title
     * @param body Dialog body
     * @param actionButtonProperty Action button property
     * @param target Target
     */
    public void show(IModel<String> title, IModel<T> body, String actionButtonProperty, AjaxRequestTarget target) {
        setDefaultModel(body);
        if (title != null) {
           getModalWindow().setTitle(title);
        }
        if (actionButtonProperty != null) {
           form.get(ACTION_BUTTON_ID).setDefaultModel(getButtonModel(actionButtonProperty));
        }
        getModalWindow().show(target);
        target.add(form);
    }

    /** Returns modal WIndow */
    public ModalWindow getModalWindow() {
        return modalWindow;
    }

    /** Get Save button property key */
    protected IModel<String> getButtonModel(String propertyName) {
        return new ResourceModel(BUTTON_PREFIX + propertyName, propertyName);
    }

}