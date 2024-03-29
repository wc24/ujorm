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

import java.util.Collections;
import java.util.List;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.core.annot.Immutable;
import org.ujorm.extensions.AbstractUjo;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.metaModel.MoreParams;
import org.ujorm.orm.utility.OrmTools;

/**
 * Abstract Metamodel
 * @author Pavel Ponec
 */
@Immutable
abstract public class AbstractMetaModel extends AbstractUjo {

    /** Read-only state */
    private boolean readOnly = false;

    /** Property values are locked to read-only. */
    public boolean readOnly() {
        return readOnly;
    }

    /** Unlock the meta-model. the method is for internal use only.
     * The method must be enabled by parameter: {@link MoreParams#ENABLE_TO_UNLOCK_IMMUTABLE_METAMODEL}.
     */
    protected void clearReadOnly(OrmHandler handler) {
        final Key<MetaParams,Boolean> enabledKey = MetaParams.MORE_PARAMS.add(MoreParams.ENABLE_TO_UNLOCK_IMMUTABLE_METAMODEL);
        if (enabledKey.of(handler.getParameters())) {
            readOnly = false;
        } else {
            throw new UnsupportedOperationException("The method must be enabled by parameter: " + enabledKey.toStringFull());
        }
    }
    
    /** Set a read-only state. */
    @SuppressWarnings("unchecked")
    public void setReadOnly(boolean recurse) {
        if (readOnly) return;

        for (Key p : readKeys()) {

            if (p instanceof ListKey) {
               final List list = (List) p.of(this);
               p.setValue(this, list!=null
                   ? Collections.unmodifiableList(list)
                   : Collections.EMPTY_LIST
               );
            }
        }

        this.readOnly = true; // <<<<<< LOCK THE OBJECT !!!

        if (recurse) {
            for (Key p : readKeys()) {

                final Object value = p.of(this);
                if (value instanceof AbstractMetaModel) {
                    ((AbstractMetaModel) value).setReadOnly(recurse);
                } else if (p instanceof ListKey) {
                    if (AbstractMetaModel.class.isAssignableFrom(((ListKey) p).getItemType())) {
                        for (AbstractMetaModel m : ((ListKey<AbstractMetaModel, AbstractMetaModel>) p).getList(this)) {
                            m.setReadOnly(recurse);
                        }
                    }
                }
            }
        }
    }

    /** Test a read-only state */
    public boolean checkReadOnly(final boolean exception) throws UnsupportedOperationException {
        if (readOnly && exception) {
            throw new UnsupportedOperationException("The model have got a read-only state: " + getClass().getSimpleName());
        }
        return readOnly;
    }

    @Override
    public void writeValue(final Key property, final Object value) {
        this.checkReadOnly(true);
        super.writeValue(property, value);
    }

    /** Assign a 'valid value' over a default UJO property value only */
    protected <UJO extends Ujo, VALUE> void changeDefault
    ( final UJO ujo
    , final Key<UJO, VALUE> property
    , final VALUE value
    ) {
        if (property.isDefault(ujo) && OrmTools.isFilled(value)) {
            property.setValue(ujo, value);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean readAuthorization(UjoAction action, Key property, Object value) {
        if (action.getType()==UjoAction.ACTION_XML_EXPORT) {
            return !property.isDefault(this);
        }
        return super.readAuthorization(action, property, value);
    }

    /** Getter based on one Key */
    @SuppressWarnings("unchecked")
    public <UJO extends AbstractMetaModel, VALUE> VALUE get ( Key<UJO, VALUE> property) {
        return property.of((UJO) this);
    }

}
