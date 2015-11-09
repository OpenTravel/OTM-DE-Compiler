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
 * Model element that specifies the parameters and payload for a REST Action request.
 * 
 * @author S. Livezey
 */
public class TLActionRequest extends TLModelElement implements LibraryElement, TLDocumentationOwner {
	
	private TLAction owner;
	private TLHttpMethod httpMethod;
	private TLParamGroup paramGroup;
	private String paramGroupName;
	private String pathTemplate;
	private TLActionFacet actionFacet;
	private String actionFacetName;
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
        identity.append("Request");
        return identity.toString();
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
	 * Returns the value of the 'httpMethod' field.
	 *
	 * @return TLHttpMethod
	 */
	public TLHttpMethod getHttpMethod() {
		return httpMethod;
	}

	/**
	 * Assigns the value of the 'httpMethod' field.
	 *
	 * @param httpMethod  the field value to assign
	 */
	public void setHttpMethod(TLHttpMethod httpMethod) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.HTTP_METHOD_MODIFIED, this)
        		.setOldValue(this.httpMethod).setNewValue(httpMethod).buildEvent();

		this.httpMethod = httpMethod;
        publishEvent(event);
	}

	/**
	 * Returns the value of the 'paramGroup' field.
	 *
	 * @return TLParamGroup
	 */
	public TLParamGroup getParamGroup() {
		return paramGroup;
	}

	/**
	 * Assigns the value of the 'paramGroup' field.
	 *
	 * @param paramGroup  the field value to assign
	 */
	public void setParamGroup(TLParamGroup paramGroup) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.PARAM_GROUP_MODIFIED, this)
        		.setOldValue(this.paramGroup).setNewValue(paramGroup).buildEvent();
        
		this.paramGroupName = (paramGroup == null) ? null : paramGroup.getName();
		this.paramGroup = paramGroup;
        publishEvent(event);
	}

	/**
	 * Returns the value of the 'paramGroupName' field.
	 *
	 * @return String
	 */
	public String getParamGroupName() {
		return paramGroupName;
	}

	/**
	 * Assigns the value of the 'paramGroupName' field.
	 *
	 * @param paramGroupName  the field value to assign
	 */
	public void setParamGroupName(String paramGroupName) {
		this.paramGroupName = paramGroupName;
	}

	/**
	 * Returns the value of the 'pathTemplate' field.
	 *
	 * @return String
	 */
	public String getPathTemplate() {
		return pathTemplate;
	}

	/**
	 * Assigns the value of the 'pathTemplate' field.
	 *
	 * @param pathTemplate  the field value to assign
	 */
	public void setPathTemplate(String pathTemplate) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.PATH_TEMPLATE_MODIFIED, this)
				.setOldValue(this.pathTemplate).setNewValue(pathTemplate).buildEvent();

		this.pathTemplate = pathTemplate;
        publishEvent(event);
	}

	/**
	 * Returns the value of the 'actionFacet' field.
	 *
	 * @return TLActionFacet
	 */
	public TLActionFacet getActionFacet() {
		return actionFacet;
	}

	/**
	 * Assigns the value of the 'actionFacet' field.
	 *
	 * @param actionFacet  the field value to assign
	 */
	public void setActionFacet(TLActionFacet actionFacet) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.ACTION_FACET_MODIFIED, this)
        		.setOldValue(this.actionFacet).setNewValue(actionFacet).buildEvent();

		this.actionFacet = actionFacet;
        publishEvent(event);
	}

	/**
	 * Returns the value of the 'actionFacetName' field.
	 *
	 * @return String
	 */
	public String getActionFacetName() {
		return actionFacetName;
	}

	/**
	 * Assigns the value of the 'actionFacetName' field.
	 *
	 * @param actionFacetName  the field value to assign
	 */
	public void setActionFacetName(String actionFacetName) {
		this.actionFacetName = actionFacetName;
	}

	/**
	 * Returns the value of the 'mimeTypes' field.
	 *
	 * @return List<MimeType>
	 */
	public List<TLMimeType> getMimeTypes() {
		return Collections.unmodifiableList( mimeTypes );
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

}
