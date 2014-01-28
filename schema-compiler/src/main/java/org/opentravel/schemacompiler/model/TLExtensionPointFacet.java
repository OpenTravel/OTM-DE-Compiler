/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.model;

import java.util.Comparator;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventBuilder;
import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.model.TLAttribute.AttributeListManager;
import org.opentravel.schemacompiler.model.TLIndicator.IndicatorListManager;
import org.opentravel.schemacompiler.model.TLProperty.PropertyListManager;

/**
 * Facet definition for types that are designed to be encapsulated within another
 * facet's extension point element.
 * 
 * <p>NOTE: In spite of its name, developers should note this class <u>does not</u> extends
 * the <code>TLAbstractFacet</code> class.  This is because several of the key facet behaviors
 * are not applicable to extension point facets (e.g. they are not assignable as property types
 * and cannot be assigned to an arbitrary <code>TLFacetOwner</code>).  Instead, the key
 * behaviors for extension point facets were inherited from the interfaces that define the
 * required behaviors.
 * 
 * @author S. Livezey
 */
public class TLExtensionPointFacet extends LibraryMember implements LibraryElement, TLExtensionOwner,
		TLAttributeOwner, TLPropertyOwner, TLIndicatorOwner, TLDocumentationOwner {
	
	private TLExtension extension;
	private AttributeListManager attributeManager = new AttributeListManager(this);
	private PropertyListManager elementManager = new PropertyListManager(this);
	private IndicatorListManager indicatorManager = new IndicatorListManager(this);
	private TLDocumentation documentation;
	
	/**
	 * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
		AbstractLibrary owningLibrary = getOwningLibrary();
		StringBuilder identity = new StringBuilder();
		String localName = getLocalName();
		
		if (owningLibrary != null) {
			identity.append(owningLibrary.getValidationIdentity()).append(" : ");
		}
		if (localName == null) {
			identity.append("[Unnamed Extension Point Facet]");
		} else {
			identity.append(localName);
		}
		return identity.toString();
	}

	/**
	 * Returns the base namespace for this extension point facet's owning library.
	 * 
	 * @return String
	 */
	public String getBaseNamespace() {
		AbstractLibrary owningLibrary = getOwningLibrary();
		String baseNamespace;
		
		if (owningLibrary instanceof TLLibrary) {
			baseNamespace = ((TLLibrary) owningLibrary).getBaseNamespace();
		} else {
			baseNamespace = getNamespace();
		}
		return baseNamespace;
	}
	
	/**
	 * Returns the version scheme identifier for this extension point facet's owning library.
	 * 
	 * @return String
	 */
	public String getVersionScheme() {
		AbstractLibrary owningLibrary = getOwningLibrary();
		String versionScheme;
		
		if (owningLibrary instanceof TLLibrary) {
			versionScheme = ((TLLibrary) owningLibrary).getVersionScheme();
		} else {
			versionScheme = null;
		}
		return versionScheme;
	}
	
	/**
	 * Returns the version identifier of the library that owns this extension point
	 * facet.
	 *
	 * @return String
	 */
	public String getVersion() {
		AbstractLibrary owningLibrary = getOwningLibrary();
		String version;
		
		if (owningLibrary instanceof TLLibrary) {
			version = ((TLLibrary) owningLibrary).getVersion();
		} else {
			version = null;
		}
		return version;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
	 */
	@Override
	public String getLocalName() {
		String localName = null;
		
		if (extension != null) {
			NamedEntity extendsEntity = extension.getExtendsEntity();
			String extendsEntityName = extension.getExtendsEntityName();
			
			if (extendsEntity != null) {
				localName = extendsEntity.getLocalName();
				
			} else if ((extendsEntityName != null) && (extendsEntityName.length() > 0)) {
				localName = extendsEntityName;
			}
			
			if (localName != null) {
				localName = "ExtensionPoint_" + localName;
			}
		}
		return localName;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.TLExtensionOwner#getExtension()
	 */
	@Override
	public TLExtension getExtension() {
		return extension;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.TLExtensionOwner#setExtension(org.opentravel.schemacompiler.model.TLExtension)
	 */
	@Override
	public void setExtension(TLExtension extension) {
		if (extension != this.extension) {
			// Even though there is only one extension, send to events so that all extension owners behave
			// the same (as if there is a list of multiple extensions).
			if (this.extension != null) {
				ModelEvent<?> event = new ModelEventBuilder(ModelEventType.EXTENDS_REMOVED, this)
					.setAffectedItem(this.extension).buildEvent();

				this.extension.setOwner(null);
				this.extension = null;
				publishEvent(event);
			}
			if (extension != null) {
				ModelEvent<?> event = new ModelEventBuilder(ModelEventType.EXTENDS_ADDED, this)
					.setAffectedItem(extension).buildEvent();
				
				extension.setOwner(this);
				this.extension = extension;
				publishEvent(event);
			}
		}
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
	 * @see org.opentravel.schemacompiler.model.TLAttributeOwner#addAttribute(int, org.opentravel.schemacompiler.model.TLAttribute)
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
	 * @see org.opentravel.schemacompiler.model.TLPropertyOwner#addElement(int, org.opentravel.schemacompiler.model.TLProperty)
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
	 * @see org.opentravel.schemacompiler.model.TLIndicatorOwner#addIndicator(int, org.opentravel.schemacompiler.model.TLIndicator)
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
