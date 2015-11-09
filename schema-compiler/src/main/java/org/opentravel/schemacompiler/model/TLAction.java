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

import java.util.List;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventBuilder;
import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.model.TLActionResponse.ActionResponseListManager;

/**
 * Model element that specifies the request and response for a <code>TLResource</code>
 * operation.
 * 
 * @author S. Livezey
 */
public class TLAction extends TLModelElement implements LibraryElement, TLDocumentationOwner {
	
	private TLResource owner;
	private String actionId;
	private boolean commonAction;
	private TLActionRequest request;
	private ActionResponseListManager responseManager = new ActionResponseListManager( this );
    private TLDocumentation documentation;
	
	/**
	 * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();

        if (owner != null) {
            identity.append(owner.getValidationIdentity()).append("/");
        }
        if (actionId == null) {
            identity.append("[Unnamed Resource Action]");
        } else {
            identity.append(actionId);
        }
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
	 * @return TLResource
	 */
	public TLResource getOwner() {
		return owner;
	}
	
	/**
	 * Assigns the value of the 'owner' field.
	 *
	 * @param owner  the field value to assign
	 */
	public void setOwner(TLResource owner) {
		this.owner = owner;
	}

	/**
	 * Returns the value of the 'actionId' field.
	 *
	 * @return String
	 */
	public String getActionId() {
		return actionId;
	}

	/**
	 * Assigns the value of the 'actionId' field.
	 *
	 * @param actionId  the field value to assign
	 */
	public void setActionId(String actionId) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.ACTION_ID_MODIFIED, this)
				.setOldValue(this.actionId).setNewValue(actionId).buildEvent();

		this.actionId = actionId;
        publishEvent(event);
	}

	/**
	 * Returns the value of the 'commonAction' field.
	 *
	 * @return boolean
	 */
	public boolean isCommonAction() {
		return commonAction;
	}

	/**
	 * Assigns the value of the 'commonAction' field.
	 *
	 * @param commonAction  the field value to assign
	 */
	public void setCommonAction(boolean commonAction) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.COMMON_FLAG_MODIFIED, this)
				.setOldValue(this.actionId).setNewValue(actionId).buildEvent();

		this.commonAction = commonAction;
        publishEvent(event);
	}

	/**
	 * Returns the value of the 'request' field.
	 *
	 * @return TLActionRequest
	 */
	public TLActionRequest getRequest() {
		return request;
	}

    /**
	 * Assigns the value of the 'request' field.
	 *
	 * @param request  the field value to assign
	 */
	public void setRequest(TLActionRequest request) {
		TLActionRequest origRequest = this.request;
        
		if (request != origRequest) {
			this.request = request;
			
			if (origRequest != null) {
				origRequest.setOwner(null);
				publishEvent( new ModelEventBuilder(ModelEventType.ACTION_REQUEST_REMOVED, this)
						.setAffectedItem(origRequest).buildEvent() );
			}
			if (request != null) {
				request.setOwner(this);
				publishEvent( new ModelEventBuilder(ModelEventType.ACTION_REQUEST_ADDED, this)
						.setAffectedItem(request).buildEvent() );
			}
		}
	}

	/**
     * Returns the value of the 'actionResponse' field.
     * 
     * @return List<TLActionResponse>
     */
    public List<TLActionResponse> getResponses() {
        return responseManager.getChildren();
    }

    /**
     * Adds a <code>TLActionResponse</code> element to the current list.
     * 
     * @param response  the response to add
     */
    public void addResponse(TLActionResponse response) {
    	responseManager.addChild(response);
    }

    /**
     * Adds a <code>TLActionResponse</code> element to the current list.
     * 
     * @param index  the index at which the given response should be added
     * @param parameter  the response to add
     */
    public void addResponse(int index, TLActionResponse response) {
    	responseManager.addChild(index, response);
    }

    /**
     * Removes the specified <code>TLActionResponse</code> from the current list.
     * 
     * @param parameter  the response value to remove
     */
    public void removeResponse(TLActionResponse parameter) {
    	responseManager.removeChild(parameter);
    }

    /**
     * Moves this response up by one position in the list. If the response is not owned by this
     * object or it is already at the front of the list, this method has no effect.
     * 
     * @param parameter  the parameter to move
     */
    public void moveUp(TLActionResponse parameter) {
    	responseManager.moveUp(parameter);
    }

    /**
     * Moves this response down by one position in the list. If the response is not owned by this
     * object or it is already at the end of the list, this method has no effect.
     * 
     * @param response  the response to move
     */
    public void moveDown(TLActionResponse response) {
    	responseManager.moveDown(response);
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
     * Manages lists of <code>TLAction</code> entities.
     * 
     * @author S. Livezey
     */
    protected static class ActionListManager extends
    		ChildEntityListManager<TLAction, TLResource> {

        /**
         * Constructor that specifies the owner of the unerlying list.
         * 
         * @param owner  the owner of the underlying list of children
         */
        public ActionListManager(TLResource owner) {
            super(owner, ModelEventType.ACTION_ADDED, ModelEventType.ACTION_REMOVED);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
         */
        @Override
        protected String getChildName(TLAction child) {
            return child.getActionId();
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
        protected void assignOwner(TLAction child, TLResource owner) {
            child.setOwner(owner);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object,
         *      org.opentravel.schemacompiler.event.ModelEvent)
         */
        @Override
        protected void publishEvent(TLResource owner, ModelEvent<?> event) {
            TLModel owningModel = owner.getOwningModel();

            if (owningModel != null) {
                owningModel.publishEvent(event);
            }
        }

    }

}
