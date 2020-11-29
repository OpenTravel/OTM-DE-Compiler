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
 * Facet definition for REST resources.
 * 
 * @author S. Livezey
 */
public class TLActionFacet extends TLModelElement implements NamedEntity, TLDocumentationOwner {

    private String name;
    private TLResource owningResource;
    private TLReferenceType referenceType;
    private String referenceFacetName;
    private int referenceRepeat;
    private NamedEntity basePayload;
    private String basePayloadName;
    private TLDocumentation documentation;

    /**
     * Returns the resource that owns this action facet.
     *
     * @return TLResource
     */
    public TLResource getOwningResource() {
        return owningResource;
    }

    /**
     * Assigns the resource that owns this action facet.
     *
     * @param owningResource the resource instance to assign
     */
    public void setOwningResource(TLResource owningResource) {
        this.owningResource = owningResource;
    }

    /**
     * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
     */
    @Override
    public AbstractLibrary getOwningLibrary() {
        return (owningResource == null) ? null : owningResource.getOwningLibrary();
    }

    /**
     * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
     */
    @Override
    public TLModel getOwningModel() {
        return (owningResource == null) ? null : owningResource.getOwningModel();
    }

    /**
     * @see org.opentravel.schemacompiler.model.NamedEntity#getNamespace()
     */
    @Override
    public String getNamespace() {
        return (owningResource == null) ? null : owningResource.getNamespace();
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAbstractFacet#getLocalName()
     */
    @Override
    public String getLocalName() {
        StringBuilder localName = new StringBuilder();

        if (owningResource != null) {
            localName.append( owningResource.getLocalName() ).append( '_' );
        }
        if (name != null) {
            localName.append( name );

        } else {
            localName.append( "Unnamed_Facet" );
        }
        return localName.toString();
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();

        if (owningResource != null) {
            identity.append( owningResource.getValidationIdentity() ).append( "/" );
        }
        if (name == null) {
            identity.append( "[Unnamed Action Facet]" );
        } else {
            identity.append( name );
        }
        return identity.toString();
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
     * Returns the value of the 'referenceType' field.
     *
     * @return TLReferenceType
     */
    public TLReferenceType getReferenceType() {
        return referenceType;
    }

    /**
     * Assigns the value of the 'referenceType' field.
     *
     * @param referenceType the field value to assign
     */
    public void setReferenceType(TLReferenceType referenceType) {
        ModelEvent<?> event = new ModelEventBuilder( ModelEventType.REFERENCE_TYPE_MODIFIED, this )
            .setOldValue( this.referenceType ).setNewValue( referenceType ).buildEvent();

        this.referenceType = referenceType;
        publishEvent( event );
    }

    /**
     * Returns the value of the 'referenceFacetName' field.
     *
     * @return String
     */
    public String getReferenceFacetName() {
        return referenceFacetName;
    }

    /**
     * Assigns the value of the 'referenceFacetName' field.
     *
     * @param referenceFacetName the field value to assign
     */
    public void setReferenceFacetName(String referenceFacetName) {
        ModelEvent<?> event = new ModelEventBuilder( ModelEventType.FACET_REF_NAME_MODIFIED, this )
            .setOldValue( this.referenceFacetName ).setNewValue( referenceFacetName ).buildEvent();

        this.referenceFacetName = referenceFacetName;
        publishEvent( event );
    }

    /**
     * Returns the value of the 'referenceRepeat' field.
     *
     * @return int
     */
    public int getReferenceRepeat() {
        return referenceRepeat;
    }

    /**
     * Assigns the value of the 'referenceRepeat' field.
     *
     * @param referenceRepeat the field value to assign
     */
    public void setReferenceRepeat(int referenceRepeat) {
        ModelEvent<?> event = new ModelEventBuilder( ModelEventType.FACET_REF_REPEAT_MODIFIED, this )
            .setOldValue( this.referenceRepeat ).setNewValue( referenceRepeat ).buildEvent();

        this.referenceRepeat = referenceRepeat;
        publishEvent( event );
    }

    /**
     * Returns the value of the 'basePayload' field.
     *
     * @return NamedEntity
     */
    public NamedEntity getBasePayload() {
        return basePayload;
    }

    /**
     * Assigns the value of the 'basePayload' field.
     *
     * @param basePayload the field value to assign
     */
    public void setBasePayload(NamedEntity basePayload) {
        ModelEvent<?> event = new ModelEventBuilder( ModelEventType.BASE_PAYLOAD_MODIFIED, this )
            .setOldValue( this.basePayload ).setNewValue( basePayload ).buildEvent();

        this.basePayload = basePayload;
        publishEvent( event );
    }

    /**
     * Returns the value of the 'basePayloadName' field.
     *
     * @return String
     */
    public String getBasePayloadName() {
        return basePayloadName;
    }

    /**
     * Assigns the value of the 'basePayloadName' field.
     *
     * @param basePayloadName the field value to assign
     */
    public void setBasePayloadName(String basePayloadName) {
        this.basePayloadName = basePayloadName;
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
     * Manages lists of <code>TLActionFacet</code> entities.
     * 
     * @author S. Livezey
     */
    protected static class ActionFacetListManager extends ChildEntityListManager<TLActionFacet,TLResource> {

        /**
         * Constructor that specifies the owner of the underlying list.
         * 
         * @param owner the owner of the underlying list of children
         */
        public ActionFacetListManager(TLResource owner) {
            super( owner, ModelEventType.ACTION_FACET_ADDED, ModelEventType.ACTION_FACET_REMOVED );
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
         */
        @Override
        protected String getChildName(TLActionFacet child) {
            return child.getName();
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
        protected void assignOwner(TLActionFacet child, TLResource owner) {
            child.setOwningResource( owner );
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
