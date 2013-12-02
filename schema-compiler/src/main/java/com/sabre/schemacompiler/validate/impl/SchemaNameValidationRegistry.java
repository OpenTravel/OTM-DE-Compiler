/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.validate.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import com.sabre.schemacompiler.codegen.util.AliasCodegenUtils;
import com.sabre.schemacompiler.codegen.util.XsdCodegenUtils;
import com.sabre.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLAliasOwner;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLClosedEnumeration;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLListFacet;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.model.TLOpenEnumeration;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.model.XSDComplexType;
import com.sabre.schemacompiler.model.XSDElement;
import com.sabre.schemacompiler.model.XSDSimpleType;
import com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter;
import com.sabre.schemacompiler.visitor.ModelNavigator;

/**
 * Registry that includes values for the global type and element names of all <code>TLModel</code>
 * elements as they will be generated in the compiler's generated XSD output.
 * 
 * @author S. Livezey
 */
public class SchemaNameValidationRegistry {
	
	private Map<NamedEntity,QName> entityTypeNames = new HashMap<NamedEntity,QName>();
	private Map<NamedEntity,Set<QName>> entityElementNames = new HashMap<NamedEntity,Set<QName>>();
	private Map<QName,Set<NamedEntity>> typeNameEntities = new HashMap<QName,Set<NamedEntity>>();
	private Map<QName,Set<NamedEntity>> elementNameEntities = new HashMap<QName,Set<NamedEntity>>();
	
	/**
	 * Constructor that initializes the registry using all <code>NamedEntity</code> symbols from the
	 * given model.
	 * 
	 * @param model  the model from which to initialize the registry
	 */
	public SchemaNameValidationRegistry(TLModel model) {
		ModelNavigator.navigate( model, new SchemaNameVisitor() );
	}
	
	/**
	 * Returns the name of the type that will be generated in the XML schema for the given entity.  If
	 * the entity does not have a corresponding type name, this method will return null.
	 * 
	 * @param entity  the entity for which to return the schema type name
	 * @return QName
	 */
	public QName getSchemaTypeName(NamedEntity entity) {
		return entityTypeNames.get( entity );
	}
	
	/**
	 * Returns the collection of global element names that will be generated in the XML schema for the
	 * given entity.  If the entity does not have a corresponding element name, this method will return
	 * an empty collection.
	 * 
	 * @param entity  the entity for which to return the schema element name
	 * @return Collection<QName>
	 */
	public Collection<QName> getSchemaElementNames(NamedEntity entity) {
		Collection<QName> elementNames = new HashSet<QName>();
		
		if (entityElementNames.containsKey( entity )) {
			elementNames.addAll( entityElementNames.get( entity ) );
		}
		return elementNames;
	}
	
	/**
	 * Returns one or more entities whose type name will match the one provided in the generated
	 * schema.  If no such entity matches the name provided, this method will return an empty
	 * collection.
	 * 
	 * @param schemaTypeName  the type name for which to return the matching entities
	 * @return Collection<NamedEntity>
	 */
	public Collection<NamedEntity> findEntitiesBySchemaTypeName(QName schemaTypeName) {
		Collection<NamedEntity> entities = new HashSet<NamedEntity>();
		
		if (entityTypeNames.containsKey(schemaTypeName)) {
			entities.addAll( typeNameEntities.get(schemaTypeName) );
		}
		return entities;
	}
	
	/**
	 * Returns one or more entities whose global element name will match the one provided in the
	 * generated schema.  If no such entity matches the name provided, this method will return an empty
	 * collection.
	 * 
	 * @param schemaElementName  the global element name for which to return the matching entities
	 * @return Collection<NamedEntity>
	 */
	public Collection<NamedEntity> findEntitiesBySchemaElementName(QName schemaElementName) {
		Collection<NamedEntity> entities = new HashSet<NamedEntity>();
		
		if (elementNameEntities.containsKey(schemaElementName)) {
			entities.addAll( elementNameEntities.get(schemaElementName) );
		}
		return entities;
	}
	
	/**
	 * Returns true if more than one entity in this registry is assigned the same type name as the
	 * entity provided.
	 * 
	 * @param entity  the entity to check for duplicate schema names
	 * @return boolean
	 */
	public boolean hasTypeNameConflicts(NamedEntity entity) {
		Collection<NamedEntity> entityTypeMatches = typeNameEntities.get( entityTypeNames.get( entity ) );
		return ((entityTypeMatches != null) && (entityTypeMatches.size() > 1) && entityTypeMatches.contains( entity ));
	}
	
	/**
	 * If the given entity has an element name that conflicts with the name of another entity, this method
	 * will return the conflicting name.  If no conflicts exist, this method will return null.
	 * 
	 * @param entity  the entity to check for duplicate schema names
	 * @return boolean
	 */
	public QName getElementNameConflicts(NamedEntity entity) {
		Set<QName> elementNames = entityElementNames.get( entity );
		QName conflictingElement = null;
		
		if (elementNames != null) {
			for (QName elementName : elementNames) {
				Collection<NamedEntity> entityElementMatches = elementNameEntities.get( elementName );
				boolean hasConflict = ((entityElementMatches != null) && (entityElementMatches.size() > 1) && entityElementMatches.contains( entity ));
				
				if (hasConflict) {
					conflictingElement = elementName;
					break;
				}
			}
		}
		return conflictingElement;
	}
	
	/**
	 * Adds a registry entry for the given entity using its schema type name.
	 * 
	 * @param entity  the entity to be added to the registry
	 */
	private void addTypeNameToRegistry(NamedEntity entity) {
		QName typeName;
		
		if ((entity instanceof XSDComplexType) || (entity instanceof XSDSimpleType)) {
			typeName = new QName(entity.getNamespace(), entity.getLocalName());
			
		} else {
			typeName = new QName( entity.getNamespace(), XsdCodegenUtils.getGlobalTypeName( entity ) );
		}
		
		Set<NamedEntity> registeredEntities = typeNameEntities.get( typeName );
		
		if (registeredEntities == null) {
			registeredEntities = new HashSet<NamedEntity>();
			typeNameEntities.put(typeName, registeredEntities);
		}
		registeredEntities.add( entity );
		entityTypeNames.put(entity, typeName);
	}
	
	/**
	 * Adds a registry entry for the given entity using all of the global element names (if any) that
	 * will be assigned during schema generation.
	 * 
	 * @param entity  the entity to be added to the registry
	 */
	private void addElementNamesToRegistry(NamedEntity entity) {
		if (entity instanceof TLFacet) {
			TLFacet entityFacet = (TLFacet) entity;
			boolean hasContent = new FacetCodegenDelegateFactory(null).getDelegate(entityFacet).hasContent();
			
			if (hasContent && !XsdCodegenUtils.isSimpleCoreObject(entityFacet.getOwningEntity())) {
				addElementNameToRegistry( XsdCodegenUtils.getGlobalElementName(entity), entity );
			}
			
		} else if (entity instanceof TLAlias) {
			TLAliasOwner aliasOwner = ((TLAlias) entity).getOwningEntity();
			
			if (aliasOwner instanceof TLFacet) {
				TLFacet aliasedFacet = (TLFacet) aliasOwner;
				boolean hasContent = new FacetCodegenDelegateFactory(null).getDelegate(aliasedFacet).hasContent();
				
				if (hasContent && !XsdCodegenUtils.isSimpleCoreObject(aliasedFacet.getOwningEntity())) {
					addElementNameToRegistry( XsdCodegenUtils.getGlobalElementName(entity), entity );
				}
				
			} else { // Must be an alias of a business object or core that has a substitution group
				TLFacet substitutableFacet = null;
				
				if (aliasOwner instanceof TLCoreObject) {
					substitutableFacet = ((TLCoreObject) aliasOwner).getSummaryFacet();
					
				} else if (aliasOwner instanceof TLBusinessObject) {
					substitutableFacet = ((TLBusinessObject) aliasOwner).getIdFacet();
				}
				if (substitutableFacet != null) {
					TLAlias substitutableAlias = AliasCodegenUtils.getFacetAlias((TLAlias) entity, substitutableFacet.getFacetType(),
							substitutableFacet.getContext(), substitutableFacet.getLabel());
					
					if (substitutableAlias != null) {
						addElementNameToRegistry( XsdCodegenUtils.getSubstitutableElementName(substitutableAlias), entity );
					}
				}
				addElementNameToRegistry( XsdCodegenUtils.getSubstitutionGroupElementName(entity), entity );
			}
			
		} else if (XsdCodegenUtils.isSimpleCoreObject(entity)) {
			addElementNameToRegistry( XsdCodegenUtils.getGlobalElementName(entity), entity );
			
		} else { // Must be a business object or core that has a substitution group
			TLFacet substitutableFacet = null;
			
			if (entity instanceof TLCoreObject) {
				substitutableFacet = ((TLCoreObject) entity).getSummaryFacet();
				
			} else if (entity instanceof TLBusinessObject) {
				substitutableFacet = ((TLBusinessObject) entity).getIdFacet();
			}
			if (substitutableFacet != null) {
				addElementNameToRegistry( XsdCodegenUtils.getSubstitutableElementName(substitutableFacet), entity );
			}
			addElementNameToRegistry( XsdCodegenUtils.getSubstitutionGroupElementName(entity), entity );
		}
	}
	
	/**
	 * Adds the given entity to the registry using the specified XML element name.
	 * 
	 * @param elementName  the name of the schema element for the entity
	 * @param entity  the entity to be added to the registry
	 */
	private void addElementNameToRegistry(QName elementName, NamedEntity entity) {
		if ((elementName != null) && (entity != null)) {
			Set<NamedEntity> registeredEntities = elementNameEntities.get( elementName );
			Set<QName> elementNames = entityElementNames.get( entity );
			
			if (registeredEntities == null) {
				registeredEntities = new HashSet<NamedEntity>();
				elementNameEntities.put( elementName, registeredEntities );
			}
			if (elementNames == null) {
				elementNames = new HashSet<QName>();
				entityElementNames.put( entity, elementNames );
			}
			registeredEntities.add( entity );
			elementNames.add( elementName );
		}
	}
	
	/**
	 * Visitor that constructs the registry of all global type and element names in the model.
	 */
	private class SchemaNameVisitor extends ModelElementVisitorAdapter {

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(com.sabre.schemacompiler.model.TLSimple)
		 */
		@Override
		public boolean visitSimple(TLSimple simple) {
			addTypeNameToRegistry( simple );
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitClosedEnumeration(com.sabre.schemacompiler.model.TLClosedEnumeration)
		 */
		@Override
		public boolean visitClosedEnumeration(TLClosedEnumeration enumeration) {
			addTypeNameToRegistry( enumeration );
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitOpenEnumeration(com.sabre.schemacompiler.model.TLOpenEnumeration)
		 */
		@Override
		public boolean visitOpenEnumeration(TLOpenEnumeration enumeration) {
			addTypeNameToRegistry( enumeration );
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(com.sabre.schemacompiler.model.TLValueWithAttributes)
		 */
		@Override
		public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
			addTypeNameToRegistry( valueWithAttributes );
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitCoreObject(com.sabre.schemacompiler.model.TLCoreObject)
		 */
		@Override
		public boolean visitCoreObject(TLCoreObject coreObject) {
			addElementNamesToRegistry( coreObject );
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitBusinessObject(com.sabre.schemacompiler.model.TLBusinessObject)
		 */
		@Override
		public boolean visitBusinessObject(TLBusinessObject businessObject) {
			addElementNamesToRegistry( businessObject );
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitFacet(com.sabre.schemacompiler.model.TLFacet)
		 */
		@Override
		public boolean visitFacet(TLFacet facet) {
			boolean hasContent = new FacetCodegenDelegateFactory(null).getDelegate(facet).hasContent();
			
			if (hasContent) {
				addTypeNameToRegistry( facet );
			}
			addElementNamesToRegistry( facet );
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(com.sabre.schemacompiler.model.TLSimpleFacet)
		 */
		@Override
		public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
			if (!simpleFacet.isEmptyType()) {
				addTypeNameToRegistry( simpleFacet );
			}
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitListFacet(com.sabre.schemacompiler.model.TLListFacet)
		 */
		@Override
		public boolean visitListFacet(TLListFacet listFacet) {
			boolean hasContent = new FacetCodegenDelegateFactory(null).getDelegate(listFacet).hasContent();
			
			if (hasContent) {
				addTypeNameToRegistry( listFacet );
			}
			addElementNamesToRegistry( listFacet );
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitAlias(com.sabre.schemacompiler.model.TLAlias)
		 */
		@Override
		public boolean visitAlias(TLAlias alias) {
			addElementNamesToRegistry( alias );
			return true;
		}
		
		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDSimpleType(com.sabre.schemacompiler.model.XSDSimpleType)
		 */
		@Override
		public boolean visitXSDSimpleType(XSDSimpleType xsdSimple) {
			addTypeNameToRegistry( xsdSimple );
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDComplexType(com.sabre.schemacompiler.model.XSDComplexType)
		 */
		@Override
		public boolean visitXSDComplexType(XSDComplexType xsdComplex) {
			addTypeNameToRegistry( xsdComplex );
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDElement(com.sabre.schemacompiler.model.XSDElement)
		 */
		@Override
		public boolean visitXSDElement(XSDElement xsdElement) {
			addElementNameToRegistry( new QName(xsdElement.getNamespace(), xsdElement.getLocalName()), xsdElement );
			return true;
		}

	}
	
}
