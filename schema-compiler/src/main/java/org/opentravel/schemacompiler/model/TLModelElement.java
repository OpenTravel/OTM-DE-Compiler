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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.util.ModelElementCloner;

/**
 * Base class for all type library model elements.
 * 
 * @author S. Livezey
 */
public abstract class TLModelElement implements ModelElement {
	
	private List<ModelElementListener> listeners = new ArrayList<>();
	
    /**
     * Default constructor.
     */
    public TLModelElement() {
    }
    
    /**
     * Registers a listener for this model element.
     * 
     * @param listener  the listener instance to register
     */
    public void addListener(ModelElementListener listener) {
    	if ((listener != null) && !listeners.contains( listener )) {
    		listeners.add( listener );
    	}
    }
    
    /**
     * Unregisteres a listener from this model element.
     * 
     * @param listener  the lister instance to unregister
     */
    public void removeListener(ModelElementListener listener) {
    	if (listener != null) {
    		listeners.remove( listener );
    	}
    }
    
    /**
     * Returns an unmodifiable collections of all listeners that have been registered
     * with this model element.
     * 
     * @return Collection<ModelElementListener>
     */
    public Collection<ModelElementListener> getListeners() {
    	return Collections.unmodifiableCollection( listeners );
    }

    /**
     * Publishes the given event to all registered listeners of the owning model that are capable of
     * processing it.
     * 
     * @param event
     *            the event to publish
     */
    protected void publishEvent(ModelEvent<?> event) {
        TLModel owningModel = getOwningModel();

        if (owningModel != null) {
            owningModel.publishEvent(event);
            
            if (event instanceof ValueChangeEvent) {
            	for (ModelElementListener listener : listeners) {
            		listener.processValueChangeEvent( (ValueChangeEvent<?,?>) event );
            	}
            	
            } else if (event instanceof OwnershipEvent) {
            	for (ModelElementListener listener : listeners) {
            		listener.processOwnershipEvent( (OwnershipEvent<?,?>) event );
            	}
            }
        }
    }

    /**
     * Creates a deep-copy of this model element. The only difference between this one and the new
     * copy is that the new copy will not yet be assigned to an owning library.
     * 
     * @return TLModelElement
     * @throws IllegalArgumentException
     *             thrown if this model element cannot be cloned
     */
    public LibraryElement cloneElement() {
        return cloneElement((this instanceof LibraryElement) ? ((LibraryElement) this)
                .getOwningLibrary() : null);
    }

    /**
     * Creates a deep-copy of this model element. The only difference between this one and the new
     * copy is that the new copy will not yet be assigned to an owning library.
     * 
     * @param namingContext
     *            the library whose owning that should be used for reference lookups when resolving
     *            names in the cloned entity; the library itself is used to resolve namespace prefix
     *            references during reference lookups
     * @return LibraryElement
     * @throws IllegalArgumentException
     *             thrown if this model element cannot be cloned
     */
    public LibraryElement cloneElement(AbstractLibrary namingContext) {
        return (LibraryElement) new ModelElementCloner((namingContext == null) ? null
                : namingContext.getOwningModel()).clone(this, namingContext);
    }

}
