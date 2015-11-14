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
import java.util.Collections;
import java.util.List;

import org.opentravel.ns.ota2.librarymodel_v01_05.MimeType;
import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventBuilder;
import org.opentravel.schemacompiler.event.ModelEventType;

/**
 * Model element that specifies the response code and payload for a REST Action response.
 *
 * @author S. Livezey
 */
public class TLActionResponse extends TLModelElement implements NamedEntity, TLDocumentationOwner {
	
	private TLAction owner;
	private List<Integer> statusCodes = new ArrayList<>();
	private NamedEntity payloadType;
	private String payloadTypeName;
	private List<TLMimeType> mimeTypes = new ArrayList<>();
    private TLDocumentation documentation;
    
	/**
	 * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();
        
        if (owner == null) {
            identity.append("[Unnamed Action]");
            
        } else {
            identity.append(owner.getValidationIdentity()).append("/");
        }
        identity.append("Response");
        return identity.toString();
	}

	/**
	 * @see org.opentravel.schemacompiler.model.NamedEntity#getNamespace()
	 */
	@Override
	public String getNamespace() {
		AbstractLibrary owningLibrary = getOwningLibrary();
		
		return (owningLibrary == null) ? null : owningLibrary.getNamespace();
	}

	/**
	 * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
	 */
	@Override
	public String getLocalName() {
		StringBuilder localName = new StringBuilder();
		String actionId = (owner == null) ? null : owner.getActionId();
		
		if (actionId != null) {
			localName.append( actionId );
		} else {
			localName.append( "Unknown_Action" );
		}
		localName.append('_').append( "Response" );
		return localName.toString();
	}

	/**
	 * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
	 */
	@Override
	public AbstractLibrary getOwningLibrary() {
		return (owner == null) ? null : owner.getOwningLibrary();
	}

    /**
	 * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
	 */
	@Override
	public TLModel getOwningModel() {
		return (owner == null) ? null : owner.getOwningModel();
	}

	/**
	 * Returns the value of the 'owner' field.
	 *
	 * @return TLAction
	 */
	public TLAction getOwner() {
		return owner;
	}

	/**
	 * Assigns the value of the 'owner' field.
	 *
	 * @param owner  the field value to assign
	 */
	public void setOwner(TLAction owningAction) {
		this.owner = owningAction;
	}

	/**
	 * Returns the value of the 'statusCodes' field.
	 *
	 * @return List<Integer>
	 */
	public List<Integer> getStatusCodes() {
		return Collections.unmodifiableList( statusCodes );
	}

	/**
	 * Assigns the value of the 'statusCodes' field.
	 *
	 * @param statusCodes  the field value to assign
	 */
	public void setStatusCodes(List<Integer> statusCodes) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.STATUS_CODES_MODIFIED, this)
				.setOldValue(this.statusCodes).setNewValue(statusCodes).buildEvent();

		this.statusCodes = (statusCodes == null) ? new ArrayList<Integer>() : statusCodes;
        publishEvent(event);
	}

	/**
	 * Adds an HTTP response status code to the current list.
	 * 
	 * @param statusCode  the status code to add
	 */
	public void addStatusCode(int statusCode) {
		ModelEventBuilder eventBuilder = null;
		
		if (!this.statusCodes.contains(statusCode)) {
			eventBuilder = new ModelEventBuilder(ModelEventType.STATUS_CODES_MODIFIED, this)
    				.setOldValue( new ArrayList<>( this.statusCodes ) );
		}
		this.statusCodes.add( statusCode );
		
		if (eventBuilder != null) {
			publishEvent( eventBuilder.setNewValue( new ArrayList<>( this.statusCodes ) ).buildEvent() );
		}
	}

	/**
	 * Removes an HTTP response status code from the current list.
	 * 
	 * @param statusCode  the status code to remove
	 */
	public void removeStatusCode(int statusCode) {
		ModelEventBuilder eventBuilder = null;
		
		if (this.statusCodes.contains(statusCode)) {
			eventBuilder = new ModelEventBuilder(ModelEventType.STATUS_CODES_MODIFIED, this)
    				.setOldValue( new ArrayList<>( this.statusCodes ) );
		}
		this.statusCodes.remove( statusCode );
		
		if (eventBuilder != null) {
			publishEvent( eventBuilder.setNewValue( new ArrayList<>( this.statusCodes ) ).buildEvent() );
		}
	}

	/**
	 * Returns the value of the 'payloadType' field.
	 *
	 * @return NamedEntity
	 */
	public NamedEntity getPayloadType() {
		return payloadType;
	}

	/**
	 * Assigns the value of the 'payloadType' field.
	 *
	 * @param payloadType  the field value to assign
	 */
	public void setPayloadType(NamedEntity payloadType) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.PAYLOAD_TYPE_MODIFIED, this)
        		.setOldValue(this.payloadType).setNewValue(payloadType).buildEvent();

		this.payloadType = payloadType;
        publishEvent(event);
	}

	/**
	 * Returns the value of the 'payloadTypeName' field.
	 *
	 * @return String
	 */
	public String getPayloadTypeName() {
		return payloadTypeName;
	}

	/**
	 * Assigns the value of the 'payloadTypeName' field.
	 *
	 * @param payloadTypeName  the field value to assign
	 */
	public void setPayloadTypeName(String payloadTypeName) {
		this.payloadTypeName = payloadTypeName;
	}

	/**
	 * Returns the value of the 'mimeTypes' field.
	 *
	 * @return List<TLMimeType>
	 */
	public List<TLMimeType> getMimeTypes() {
		return mimeTypes;
	}

	/**
	 * Assigns the value of the 'mimeTypes' field.
	 *
	 * @param mimeTypes  the field value to assign
	 */
	public void setMimeTypes(List<TLMimeType> mimeTypes) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.MIME_TYPES_MODIFIED, this)
        		.setOldValue(this.mimeTypes).setNewValue(mimeTypes).buildEvent();

		this.mimeTypes = (mimeTypes == null) ? new ArrayList<TLMimeType>() : mimeTypes;
        publishEvent(event);
	}

	/**
	 * Adds a MIME type to the current list.
	 * 
	 * @param mimeType  the MIME type to add
	 */
	public void addMimeType(TLMimeType mimeType) {
		ModelEventBuilder eventBuilder = null;
		
		if (!this.mimeTypes.contains(mimeType)) {
			eventBuilder = new ModelEventBuilder(ModelEventType.MIME_TYPES_MODIFIED, this)
    				.setOldValue( new ArrayList<>( this.mimeTypes ) );
		}
		this.mimeTypes.add( mimeType );
		
		if (eventBuilder != null) {
			publishEvent( eventBuilder.setNewValue( new ArrayList<>( this.mimeTypes ) ).buildEvent() );
		}
	}

	/**
	 * Removes a MIME type from the current list.
	 * 
	 * @param mimeType  the MIME type to remove
	 */
	public void removeMimeType(MimeType mimeType) {
		ModelEventBuilder eventBuilder = null;
		
		if (this.mimeTypes.contains(mimeType)) {
			eventBuilder = new ModelEventBuilder(ModelEventType.MIME_TYPES_MODIFIED, this)
    				.setOldValue( new ArrayList<>( this.mimeTypes ) );
		}
		this.mimeTypes.remove( mimeType );
		
		if (eventBuilder != null) {
			publishEvent( eventBuilder.setNewValue( new ArrayList<>( this.mimeTypes ) ).buildEvent() );
		}
	}

	/**
     * @see org.opentravel.schemacompiler.model.TLDocumentationOwner#getDocumentation()
     */
    public TLDocumentation getDocumentation() {
        return documentation;
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLDocumentationOwner#setDocumentation(org.opentravel.schemacompiler.model.TLDocumentation)
     */
    public void setDocumentation(TLDocumentation documentation) {
        if (documentation != this.documentation) {
            ModelEvent<?> event = new ModelEventBuilder(ModelEventType.DOCUMENTATION_MODIFIED, this)
                    .setOldValue(this.documentation).setNewValue(documentation).buildEvent();

            if (documentation != null) {
                documentation.setOwner(this);
            }
            if (this.documentation != null) {
                this.documentation.setOwner(null);
            }
            this.documentation = documentation;
            publishEvent(event);
        }
    }

    /**
     * Manages lists of <code>TLParameter</code> entities.
     * 
     * @author S. Livezey
     */
    protected static class ActionResponseListManager extends
            ChildEntityListManager<TLActionResponse, TLAction> {

        /**
         * Constructor that specifies the owner of the unerlying list.
         * 
         * @param owner  the owner of the underlying list of children
         */
        public ActionResponseListManager(TLAction owner) {
            super(owner, ModelEventType.ACTION_RESPONSE_ADDED, ModelEventType.ACTION_RESPONSE_REMOVED);
        }
        
        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
         */
        @Override
        protected String getChildName(TLActionResponse child) {
        	return null; // get by name not supported
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
        protected void assignOwner(TLActionResponse child, TLAction owner) {
            child.setOwner(owner);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object,
         *      org.opentravel.schemacompiler.event.ModelEvent)
         */
        @Override
        protected void publishEvent(TLAction owner, ModelEvent<?> event) {
            TLModel owningModel = owner.getOwningModel();

            if (owningModel != null) {
                owningModel.publishEvent(event);
            }
        }

    }

}
