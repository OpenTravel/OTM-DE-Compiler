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
import org.opentravel.schemacompiler.model.TLParameter.ParameterListManager;

import java.util.Comparator;
import java.util.List;


/**
 * A collection of REST action parameters.
 * 
 * @author S. Livezey
 */
public class TLParamGroup extends TLModelElement implements LibraryElement, TLDocumentationOwner {

    private TLResource owner;
    private String name;
    private boolean idGroup;
    private TLFacet facetRef;
    private String facetRefName;
    private TLDocumentation documentation;
    private ParameterListManager parameterManager = new ParameterListManager( this );

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();

        if (owner != null) {
            identity.append( owner.getValidationIdentity() ).append( "/" );
        }
        if (name == null) {
            identity.append( "[Unnamed Parameter Group]" );
        } else {
            identity.append( name );
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
     * @param owner the field value to assign
     */
    public void setOwner(TLResource owner) {
        this.owner = owner;
    }

    /**
     * Returns the value of the 'name' field.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Assigns the value of the 'name' field.
     *
     * @param name the field value to assign
     */
    public void setName(String name) {
        ModelEvent<?> event = new ModelEventBuilder( ModelEventType.NAME_MODIFIED, this ).setOldValue( this.name )
            .setNewValue( name ).buildEvent();

        this.name = name;
        publishEvent( event );
    }

    /**
     * Returns the value of the 'idGroup' field.
     *
     * @return boolean
     */
    public boolean isIdGroup() {
        return idGroup;
    }

    /**
     * Assigns the value of the 'idGroup' field.
     *
     * @param idGroup the field value to assign
     */
    public void setIdGroup(boolean idGroup) {
        ModelEvent<?> event = new ModelEventBuilder( ModelEventType.ID_GROUP_FLAG_MODIFIED, this )
            .setOldValue( this.idGroup ).setNewValue( idGroup ).buildEvent();

        this.idGroup = idGroup;
        publishEvent( event );
    }

    /**
     * Returns the value of the 'facetRef' field.
     *
     * @return TLFacet
     */
    public TLFacet getFacetRef() {
        return facetRef;
    }

    /**
     * Assigns the value of the 'facetRef' field.
     *
     * @param facetRef the field value to assign
     */
    public void setFacetRef(TLFacet facetRef) {
        ModelEvent<?> event = new ModelEventBuilder( ModelEventType.FACET_REF_MODIFIED, this )
            .setOldValue( this.facetRef ).setNewValue( facetRef ).buildEvent();

        this.facetRef = facetRef;
        publishEvent( event );
    }

    /**
     * Returns the value of the 'facetRefName' field.
     *
     * @return String
     */
    public String getFacetRefName() {
        return facetRefName;
    }

    /**
     * Assigns the value of the 'facetRefName' field.
     *
     * @param facetRefName the field value to assign
     */
    public void setFacetRefName(String facetRefName) {
        this.facetRefName = facetRefName;
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
            ModelEvent<?> event = new ModelEventBuilder( ModelEventType.DOCUMENTATION_MODIFIED, this )
                .setOldValue( this.documentation ).setNewValue( documentation ).buildEvent();

            if (documentation != null) {
                documentation.setOwner( this );
            }
            if (this.documentation != null) {
                this.documentation.setOwner( null );
            }
            this.documentation = documentation;
            publishEvent( event );
        }
    }

    /**
     * Returns the value of the 'parameters' field.
     * 
     * @return List&lt;TLParameter&gt;
     */
    public List<TLParameter> getParameters() {
        return parameterManager.getChildren();
    }

    /**
     * Returns the parameter with the specified field name.
     * 
     * @param fieldName the field name of the parameter to return
     * @return TLParameter
     */
    public TLParameter getParameter(String fieldName) {
        return parameterManager.getChild( fieldName );
    }

    /**
     * Adds a <code>TLParameter</code> element to the current list.
     * 
     * @param parameter the parameter to add
     */
    public void addParameter(TLParameter parameter) {
        parameterManager.addChild( parameter );
    }

    /**
     * Adds a <code>TLParameter</code> element to the current list.
     * 
     * @param index the index at which the given parameter should be added
     * @param parameter the parameter to add
     */
    public void addParameter(int index, TLParameter parameter) {
        parameterManager.addChild( index, parameter );
    }

    /**
     * Removes the specified <code>TLParameter</code> from the current list.
     * 
     * @param parameter the parameter value to remove
     */
    public void removeParameter(TLParameter parameter) {
        parameterManager.removeChild( parameter );
    }

    /**
     * Moves this parameter up by one position in the list. If the parameter is not owned by this object or it is
     * already at the front of the list, this method has no effect.
     * 
     * @param parameter the parameter to move
     */
    public void moveUp(TLParameter parameter) {
        parameterManager.moveUp( parameter );
    }

    /**
     * Moves this parameter down by one position in the list. If the parameter is not owned by this object or it is
     * already at the end of the list, this method has no effect.
     * 
     * @param parameter the parameter to move
     */
    public void moveDown(TLParameter parameter) {
        parameterManager.moveDown( parameter );
    }

    /**
     * Sorts the list of parameters using the comparator provided.
     * 
     * @param comparator the comparator to use when sorting the list
     */
    public void sortParameters(Comparator<TLParameter> comparator) {
        parameterManager.sortChildren( comparator );
    }

    /**
     * Manages lists of <code>TLParamGroup</code> entities.
     * 
     * @author S. Livezey
     */
    protected static class ParamGroupListManager extends ChildEntityListManager<TLParamGroup,TLResource> {

        /**
         * Constructor that specifies the owner of the unerlying list.
         * 
         * @param owner the owner of the underlying list of children
         */
        public ParamGroupListManager(TLResource owner) {
            super( owner, ModelEventType.PARAMETER_ADDED, ModelEventType.PARAMETER_REMOVED );
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
         */
        @Override
        protected String getChildName(TLParamGroup child) {
            return child.getName();
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
        protected void assignOwner(TLParamGroup child, TLResource owner) {
            child.setOwner( owner );
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object,
         *      org.opentravel.schemacompiler.event.ModelEvent)
         */
        @Override
        protected void publishEvent(TLResource owner, ModelEvent<?> event) {
            TLModel owningModel = owner.getOwningModel();

            if (owningModel != null) {
                owningModel.publishEvent( event );
            }
        }

    }

}
