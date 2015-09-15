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
import java.util.Comparator;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventBuilder;
import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.model.TLAttribute.AttributeListManager;
import org.opentravel.schemacompiler.model.TLIndicator.IndicatorListManager;
import org.opentravel.schemacompiler.model.TLProperty.PropertyListManager;

/**
 * Facet definition for REST resources.
 * 
 * @author S. Livezey
 */
public class TLActionFacet extends TLAbstractFacet implements TLAttributeOwner,
		TLPropertyOwner, TLIndicatorOwner {
	
	private String name;
    private AttributeListManager attributeManager = new AttributeListManager(this);
    private PropertyListManager elementManager = new PropertyListManager(this);
    private IndicatorListManager indicatorManager = new IndicatorListManager(this);
    protected TLReferenceType referenceType;
    protected String referenceFacetName;
    protected int referenceRepeat;
    private boolean notExtendable;
	
    /**
     * @see org.opentravel.schemacompiler.model.TLAbstractFacet#setOwningEntity(org.opentravel.schemacompiler.model.TLFacetOwner)
     */
    @Override
    public void setOwningEntity(TLFacetOwner owningEntity) {
        super.setOwningEntity(owningEntity);
    }
    
    /**
	 * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();
        TLFacetOwner owner = getOwningEntity();
        
        if (owner != null) {
            identity.append(owner.getValidationIdentity()).append("/");
        }
        if (name == null) {
            identity.append("[Unnamed Action Facet]");
        } else {
            identity.append(name);
        }
        return identity.toString();
	}
	
	/**
     * @see org.opentravel.schemacompiler.model.TLAbstractFacet#declaresContent()
     */
    @Override
    public boolean declaresContent() {
        return super.declaresContent() || (attributeManager.getChildren().size() > 0)
                || (elementManager.getChildren().size() > 0)
                || (indicatorManager.getChildren().size() > 0);
    }

    /**
     * Clears the contents of this facet.
     */
    public void clearFacet() {
        attributeManager.clearChildren();
        elementManager.clearChildren();
        indicatorManager.clearChildren();
        setDocumentation(null);
        setNotExtendable(false);
        publishEvent(new ModelEventBuilder(ModelEventType.FACET_CLEARED, this)
                .setAffectedItem(this).buildEvent());
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
	 * @param name  the field value to assign
	 */
	public void setName(String name) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.NAME_MODIFIED, this)
        		.setOldValue(this.name).setNewValue(name).buildEvent();

        this.name = name;
        publishEvent(event);
	}

	/**
     * @see org.opentravel.schemacompiler.model.TLAttributeOwner#getAttributes()
     */
    public List<TLAttribute> getAttributes() {
        return attributeManager.getChildren();
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAttributeOwner#getAttribute(java.lang.String)
     */
    public TLAttribute getAttribute(String attributeName) {
        return attributeManager.getChild(attributeName);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAttributeOwner#addAttribute(org.opentravel.schemacompiler.model.TLAttribute)
     */
    public void addAttribute(TLAttribute attribute) {
        attributeManager.addChild(attribute);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAttributeOwner#addAttribute(int,
     *      org.opentravel.schemacompiler.model.TLAttribute)
     */
    @Override
    public void addAttribute(int index, TLAttribute attribute) {
        attributeManager.addChild(index, attribute);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAttributeOwner#removeAttribute(org.opentravel.schemacompiler.model.TLAttribute)
     */
    public void removeAttribute(TLAttribute attribute) {
        attributeManager.removeChild(attribute);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAttributeOwner#moveUp(org.opentravel.schemacompiler.model.TLAttribute)
     */
    @Override
    public void moveUp(TLAttribute attribute) {
        attributeManager.moveUp(attribute);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAttributeOwner#moveDown(org.opentravel.schemacompiler.model.TLAttribute)
     */
    @Override
    public void moveDown(TLAttribute attribute) {
        attributeManager.moveDown(attribute);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAttributeOwner#sortAttributes(java.util.Comparator)
     */
    @Override
    public void sortAttributes(Comparator<TLAttribute> comparator) {
        attributeManager.sortChildren(comparator);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLPropertyOwner#getElements()
     */
    @Override
    public List<TLProperty> getElements() {
        return elementManager.getChildren();
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLPropertyOwner#getElement(java.lang.String)
     */
    @Override
    public TLProperty getElement(String elementName) {
        return elementManager.getChild(elementName);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLPropertyOwner#addElement(org.opentravel.schemacompiler.model.TLProperty)
     */
    @Override
    public void addElement(TLProperty element) {
        elementManager.addChild(element);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLPropertyOwner#addElement(int,
     *      org.opentravel.schemacompiler.model.TLProperty)
     */
    @Override
    public void addElement(int index, TLProperty element) {
        elementManager.addChild(index, element);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLPropertyOwner#removeProperty(org.opentravel.schemacompiler.model.TLProperty)
     */
    @Override
    public void removeProperty(TLProperty element) {
        elementManager.removeChild(element);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLPropertyOwner#moveUp(org.opentravel.schemacompiler.model.TLProperty)
     */
    @Override
    public void moveUp(TLProperty element) {
        elementManager.moveUp(element);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLPropertyOwner#moveDown(org.opentravel.schemacompiler.model.TLProperty)
     */
    @Override
    public void moveDown(TLProperty element) {
        elementManager.moveDown(element);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLPropertyOwner#sortElements(java.util.Comparator)
     */
    @Override
    public void sortElements(Comparator<TLProperty> comparator) {
        elementManager.sortChildren(comparator);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLIndicatorOwner#getIndicators()
     */
    @Override
    public List<TLIndicator> getIndicators() {
        return indicatorManager.getChildren();
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLIndicatorOwner#getIndicator(java.lang.String)
     */
    @Override
    public TLIndicator getIndicator(String indicatorName) {
        return indicatorManager.getChild(indicatorName);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLIndicatorOwner#addIndicator(org.opentravel.schemacompiler.model.TLIndicator)
     */
    @Override
    public void addIndicator(TLIndicator indicator) {
        indicatorManager.addChild(indicator);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLIndicatorOwner#addIndicator(int,
     *      org.opentravel.schemacompiler.model.TLIndicator)
     */
    @Override
    public void addIndicator(int index, TLIndicator indicator) {
        indicatorManager.addChild(index, indicator);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLIndicatorOwner#removeIndicator(org.opentravel.schemacompiler.model.TLIndicator)
     */
    @Override
    public void removeIndicator(TLIndicator indicator) {
        indicatorManager.removeChild(indicator);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLIndicatorOwner#moveUp(org.opentravel.schemacompiler.model.TLIndicator)
     */
    @Override
    public void moveUp(TLIndicator indicator) {
        indicatorManager.moveUp(indicator);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLIndicatorOwner#moveDown(org.opentravel.schemacompiler.model.TLIndicator)
     */
    @Override
    public void moveDown(TLIndicator indicator) {
        indicatorManager.moveDown(indicator);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLIndicatorOwner#sortIndicators(java.util.Comparator)
     */
    @Override
    public void sortIndicators(Comparator<TLIndicator> comparator) {
        indicatorManager.sortChildren(comparator);
    }

    /**
	 * @see org.opentravel.schemacompiler.model.TLMemberFieldOwner#getMemberFields()
	 */
	@Override
	public List<TLMemberField<?>> getMemberFields() {
		List<TLMemberField<?>> memberFields = new ArrayList<>();
		
		memberFields.addAll( getAttributes() );
		memberFields.addAll( getElements() );
		memberFields.addAll( getIndicators() );
		return memberFields;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.TLMemberFieldOwner#getMemberField(java.lang.String)
	 */
	@Override
	public TLMemberField<?> getMemberField(String fieldName) {
		TLMemberField<?> memberField = getAttribute( fieldName );
		
		if (memberField == null) {
			memberField = getElement( fieldName );
		}
		if (memberField == null) {
			memberField = getIndicator( fieldName );
		}
		return memberField;
	}

    /**
     * Returns the value of the 'notExtendable' field.
     * 
     * @return boolean
     */
    public boolean isNotExtendable() {
        return notExtendable;
    }

    /**
     * Assigns the value of the 'notExtendable' field.
     * 
     * @param notExtendable
     *            the field value to assign
     */
    public void setNotExtendable(boolean notExtendable) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.NOT_EXTENDABLE_FLAG_MODIFIED,
                this).setOldValue(this.notExtendable).setNewValue(notExtendable).buildEvent();

        this.notExtendable = notExtendable;
        publishEvent(event);
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
	 * @param referenceType  the field value to assign
	 */
	public void setReferenceType(TLReferenceType businessObjectReferenceType) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.REFERENCE_TYPE_MODIFIED,
                this).setOldValue(this.referenceType).setNewValue(businessObjectReferenceType).buildEvent();

		this.referenceType = businessObjectReferenceType;
        publishEvent(event);
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
	 * @param referenceFacetName  the field value to assign
	 */
	public void setReferenceFacetName(String businessObjectFacetName) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.FACET_REF_NAME_MODIFIED,
                this).setOldValue(this.referenceFacetName).setNewValue(businessObjectFacetName).buildEvent();

		this.referenceFacetName = businessObjectFacetName;
        publishEvent(event);
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
	 * @param referenceRepeat  the field value to assign
	 */
	public void setReferenceRepeat(int businessObjectRepeat) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.FACET_REF_REPEAT_MODIFIED,
                this).setOldValue(this.referenceRepeat).setNewValue(businessObjectRepeat).buildEvent();

		this.referenceRepeat = businessObjectRepeat;
        publishEvent(event);
	}

	/**
     * Manages lists of <code>TLActionFacet</code> entities.
     * 
     * @author S. Livezey
     */
    protected static class ActionFacetListManager extends ChildEntityListManager<TLActionFacet, TLFacetOwner> {

        /**
         * Constructor that specifies the owner of the underlying list.
         * 
         * @param owner  the owner of the underlying list of children
         */
        public ActionFacetListManager(TLFacetOwner owner) {
            super(owner, ModelEventType.ACTION_FACET_ADDED, ModelEventType.ACTION_FACET_REMOVED);
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
        protected void assignOwner(TLActionFacet child, TLFacetOwner owner) {
            child.setFacetType(TLFacetType.ACTION);
            child.setOwningEntity(owner);
        }

        /**
         * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object,
         *      org.opentravel.schemacompiler.event.ModelEvent)
         */
        @Override
        protected void publishEvent(TLFacetOwner owner, ModelEvent<?> event) {
            TLModel owningModel = owner.getOwningModel();

            if (owningModel != null) {
                owningModel.publishEvent(event);
            }
        }

    }

}
