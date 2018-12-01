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

import java.util.HashSet;
import java.util.Set;

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLContextReferrer;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * Integrity checker component that automatically searches for new contexts that need to be created
 * when a library member is added to the model.
 * 
 * @author S. Livezey
 */
public class ContextLibraryMemberAddedIntegrityChecker extends
        ContextAutoCreateIntegrityChecker<OwnershipEvent<TLLibrary, LibraryMember>, TLLibrary> {

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
     */
    @Override
    public void processModelEvent(OwnershipEvent<TLLibrary, LibraryMember> event) {
        if ((event.getType() == ModelEventType.MEMBER_ADDED)) {
            ContextReferrerVisitor visitor = new ContextReferrerVisitor(null);
            Set<String> visitedContextIds = new HashSet<>();

            ModelNavigator.navigate(event.getAffectedItem(), visitor);

            for (TLContextReferrer contextReferrer : visitor.getContextReferrers()) {
                String contextId = contextReferrer.getContext();

                if (!visitedContextIds.contains(contextId)) {
                    autoCreateContextDeclaration(event.getSource(), contextId);
                    visitedContextIds.add(contextId);
                }
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#getEventClass()
     */
    @Override
    public Class<?> getEventClass() {
        return OwnershipEvent.class;
    }

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#getSourceObjectClass()
     */
    @Override
    public Class<TLLibrary> getSourceObjectClass() {
        return TLLibrary.class;
    }

}
