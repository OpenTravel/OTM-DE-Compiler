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

package org.opentravel.schemacompiler.ic;

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;

/**
 * Integrity checker component that automatically manages the list of imports and includes maintained by a
 * <code>TLLibrary</code> instance. It is invoked whenever type assignments are modified within the model, taking action
 * to add or remove imports/includes as required.
 * 
 * @author S. Livezey
 */
public class TypeAssignmentChangeIntegrityChecker
    extends ImportManagementIntegrityChecker<ValueChangeEvent<TLModelElement,NamedEntity>,TLModelElement> {

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
     */
    @Override
    public void processModelEvent(ValueChangeEvent<TLModelElement,NamedEntity> event) {
        if ((event.getType() == ModelEventType.TYPE_ASSIGNMENT_MODIFIED)
            || (event.getType() == ModelEventType.EXTENDS_ENTITY_MODIFIED)
            || (event.getType() == ModelEventType.FACET_OWNER_MODIFIED)
            || (event.getType() == ModelEventType.PARENT_RESOURCE_MODIFIED)
            || (event.getType() == ModelEventType.BASE_PAYLOAD_MODIFIED)
            || (event.getType() == ModelEventType.PARAM_GROUP_MODIFIED)
            || (event.getType() == ModelEventType.BO_REFERENCE_MODIFIED)
            || (event.getType() == ModelEventType.FACET_REF_MODIFIED)
            || (event.getType() == ModelEventType.FIELD_REF_MODIFIED)
            || (event.getType() == ModelEventType.PAYLOAD_TYPE_MODIFIED)) {
            AbstractLibrary owningLibrary = getOwningLibrary( event.getSource() );

            if (owningLibrary instanceof TLLibrary) {
                verifyReferencedLibraries( (TLLibrary) owningLibrary );
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#getEventClass()
     */
    @Override
    public Class<?> getEventClass() {
        return ValueChangeEvent.class;
    }

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#getSourceObjectClass()
     */
    @Override
    public Class<TLModelElement> getSourceObjectClass() {
        return TLModelElement.class;
    }

}
