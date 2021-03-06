/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.schemacompiler.model;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventBuilder;
import org.opentravel.schemacompiler.event.ModelEventType;

/**
 * Encapsulates the context and actual documentation associated with an additional documentation section of the model.
 * 
 * @author S. Livezey
 */
public class TLAdditionalDocumentationItem extends TLDocumentationItem implements TLContextReferrer {

    private String context;

    /**
     * @see org.opentravel.schemacompiler.model.TLContextReferrer#getContext()
     */
    public String getContext() {
        return context;
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLContextReferrer#setContext(java.lang.String)
     */
    public void setContext(String context) {
        ModelEvent<?> event = new ModelEventBuilder( ModelEventType.CONTEXT_MODIFIED, this ).setOldValue( this.context )
            .setNewValue( context ).buildEvent();

        this.context = context;
        publishEvent( event );
    }

    /**
     * Manages lists of <code>TLAdditionalDocumentationItem</code> entities.
     * 
     * @author S. Livezey
     */
    protected static class AdditionalDocumentationItemListManager
        extends ChildEntityListManager<TLAdditionalDocumentationItem,TLDocumentation> {

        /**
         * Constructor that specifies the owner of the unerlying list.
         * 
         * @param owner the owner of the underlying list of children
         * @param addEventType the type of event to publish when a child entity is added
         * @param removeEventType the type of event to publish when a child entity is removed
         */
        public AdditionalDocumentationItemListManager(TLDocumentation owner, ModelEventType addEventType,
            ModelEventType removeEventType) {
            super( owner, addEventType, removeEventType );
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
         */
        @Override
        protected String getChildName(TLAdditionalDocumentationItem child) {
            return child.getContext();
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
        protected void assignOwner(TLAdditionalDocumentationItem child, TLDocumentation owner) {
            child.setOwningDocumentation( owner );
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object,
         *      org.opentravel.schemacompiler.event.ModelEvent)
         */
        @Override
        protected void publishEvent(TLDocumentation owner, ModelEvent<?> event) {
            if (owner != null) {
                owner.publishEvent( event );
            }
        }

    }

}
