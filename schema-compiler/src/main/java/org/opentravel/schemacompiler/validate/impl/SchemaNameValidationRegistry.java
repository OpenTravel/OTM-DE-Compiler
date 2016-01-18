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
package org.opentravel.schemacompiler.validate.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * Registry that includes values for the global type and element names of all <code>TLModel</code>
 * elements as they will be generated in the compiler's generated XSD output.
 * 
 * @author S. Livezey
 */
public class SchemaNameValidationRegistry {

    private Map<NamedEntity, QName> entityTypeNames = new HashMap<NamedEntity, QName>();
    private Map<NamedEntity, Set<QName>> entityElementNames = new HashMap<NamedEntity, Set<QName>>();
    private Map<QName, Set<NamedEntity>> typeNameEntities = new HashMap<QName, Set<NamedEntity>>();
    private Map<QName, Set<NamedEntity>> elementNameEntities = new HashMap<QName, Set<NamedEntity>>();

    /**
     * Constructor that initializes the registry using all <code>NamedEntity</code> symbols from the
     * given model.
     * 
     * @param model
     *            the model from which to initialize the registry
     */
    public SchemaNameValidationRegistry(TLModel model) {
        ModelNavigator.navigate(model, new SchemaNameVisitor());
    }

    /**
     * Returns the name of the type that will be generated in the XML schema for the given entity.
     * If the entity does not have a corresponding type name, this method will return null.
     * 
     * @param entity
     *            the entity for which to return the schema type name
     * @return QName
     */
    public QName getSchemaTypeName(NamedEntity entity) {
        return entityTypeNames.get(entity);
    }

    /**
     * Returns the collection of global element names that will be generated in the XML schema for
     * the given entity. If the entity does not have a corresponding element name, this method will
     * return an empty collection.
     * 
     * @param entity
     *            the entity for which to return the schema element name
     * @return Collection<QName>
     */
    public Collection<QName> getSchemaElementNames(NamedEntity entity) {
        Collection<QName> elementNames = new HashSet<QName>();

        if (entityElementNames.containsKey(entity)) {
            elementNames.addAll(entityElementNames.get(entity));
        }
        return elementNames;
    }

    /**
     * Returns one or more entities whose type name will match the one provided in the generated
     * schema. If no such entity matches the name provided, this method will return an empty
     * collection.
     * 
     * @param schemaTypeName
     *            the type name for which to return the matching entities
     * @return Collection<NamedEntity>
     */
    public Collection<NamedEntity> findEntitiesBySchemaTypeName(QName schemaTypeName) {
        Collection<NamedEntity> entities = new HashSet<NamedEntity>();

        if (entityTypeNames.containsKey(schemaTypeName)) {
            entities.addAll(typeNameEntities.get(schemaTypeName));
        }
        return entities;
    }

    /**
     * Returns one or more entities whose global element name will match the one provided in the
     * generated schema. If no such entity matches the name provided, this method will return an
     * empty collection.
     * 
     * @param schemaElementName
     *            the global element name for which to return the matching entities
     * @return Collection<NamedEntity>
     */
    public Collection<NamedEntity> findEntitiesBySchemaElementName(QName schemaElementName) {
        Collection<NamedEntity> entities = new HashSet<NamedEntity>();

        if (elementNameEntities.containsKey(schemaElementName)) {
            entities.addAll(elementNameEntities.get(schemaElementName));
        }
        return entities;
    }

    /**
     * Returns true if more than one entity in this registry is assigned the same type name as the
     * entity provided.
     * 
     * @param entity
     *            the entity to check for duplicate schema names
     * @return boolean
     */
    public boolean hasTypeNameConflicts(NamedEntity entity) {
        Collection<NamedEntity> entityTypeMatches = typeNameEntities.get(entityTypeNames.get(entity));
        return ((entityTypeMatches != null) && (entityTypeMatches.size() > 1) && entityTypeMatches.contains(entity));
    }

    /**
     * If the given entity has an element name that conflicts with the name of another entity, this
     * method will return the conflicting name. If no conflicts exist, this method will return null.
     * 
     * @param entity
     *            the entity to check for duplicate schema names
     * @return boolean
     */
    public QName getElementNameConflicts(NamedEntity entity) {
        Set<QName> elementNames = entityElementNames.get(entity);
        QName conflictingElement = null;

        if (elementNames != null) {
            for (QName elementName : elementNames) {
                Collection<NamedEntity> entityElementMatches = elementNameEntities.get(elementName);
                boolean hasConflict = ((entityElementMatches != null)
                        && (entityElementMatches.size() > 1) && entityElementMatches
                        .contains(entity));

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
     * @param entity
     *            the entity to be added to the registry
     */
    private void addTypeNameToRegistry(NamedEntity entity) {
        QName typeName = null;
        
        if (hasGlobalType( entity )) {
            if ((entity instanceof XSDComplexType) || (entity instanceof XSDSimpleType)) {
                typeName = new QName(entity.getNamespace(), entity.getLocalName());

            } else {
            	String localTypeName = XsdCodegenUtils.getGlobalTypeName(entity);
                typeName = (localTypeName == null) ? null : new QName(entity.getNamespace(), localTypeName);
            }
        }
        
        if (typeName != null) {
            Set<NamedEntity> registeredEntities = typeNameEntities.get(typeName);

            if (registeredEntities == null) {
                registeredEntities = new HashSet<NamedEntity>();
                typeNameEntities.put(typeName, registeredEntities);
            }
            registeredEntities.add(entity);
            entityTypeNames.put(entity, typeName);
        }
    }

    /**
     * Adds a registry entry for the given entity using all of the global element names (if any)
     * that will be assigned during schema generation.
     * 
     * @param entity
     *            the entity to be added to the registry
     */
    private void addElementNamesToRegistry(NamedEntity entity) {
        if (entity instanceof TLFacet) {
            TLFacet entityFacet = (TLFacet) entity;
            boolean hasContent = new FacetCodegenDelegateFactory(null).getDelegate(entityFacet).hasContent();

            if (hasContent && !XsdCodegenUtils.isSimpleCoreObject(entityFacet.getOwningEntity())) {
                addElementNameToRegistry(XsdCodegenUtils.getGlobalElementName(entity), entity);
            }

        } else if (entity instanceof TLActionFacet) {
        	// No additional member elements to add
        	
        } else if (entity instanceof TLAlias) {
            TLAliasOwner aliasOwner = ((TLAlias) entity).getOwningEntity();

            if (aliasOwner instanceof TLFacet) {
                TLFacet aliasedFacet = (TLFacet) aliasOwner;
                boolean hasContent = new FacetCodegenDelegateFactory(null)
                        .getDelegate(aliasedFacet).hasContent();

                if (hasContent
                        && !XsdCodegenUtils.isSimpleCoreObject(aliasedFacet.getOwningEntity())) {
                    addElementNameToRegistry(XsdCodegenUtils.getGlobalElementName(entity), entity);
                }

            } else { // Must be an alias of a business object or core that has a substitution group
                TLFacet substitutableFacet = null;

                if (aliasOwner instanceof TLCoreObject) {
                    substitutableFacet = ((TLCoreObject) aliasOwner).getSummaryFacet();

                } else if (aliasOwner instanceof TLBusinessObject) {
                    substitutableFacet = ((TLBusinessObject) aliasOwner).getIdFacet();
                    
                } else if (aliasOwner instanceof TLChoiceObject) {
                    substitutableFacet = ((TLChoiceObject) aliasOwner).getSharedFacet();
                }
                if (substitutableFacet != null) {
                    TLAlias substitutableAlias = AliasCodegenUtils.getFacetAlias((TLAlias) entity,
                            substitutableFacet.getFacetType(), substitutableFacet.getContext(),
                            substitutableFacet.getLabel());

                    if (substitutableAlias != null) {
                        addElementNameToRegistry(
                                XsdCodegenUtils.getSubstitutableElementName(substitutableAlias),
                                entity);
                    }
                }
                addElementNameToRegistry(XsdCodegenUtils.getSubstitutionGroupElementName(entity),
                        entity);
            }

        } else if (XsdCodegenUtils.isSimpleCoreObject(entity)) {
            addElementNameToRegistry(XsdCodegenUtils.getGlobalElementName(entity), entity);

        } else { // Must be a business, core, or choice object that has a substitution group
            TLFacet substitutableFacet = null;

            if (entity instanceof TLCoreObject) {
                substitutableFacet = ((TLCoreObject) entity).getSummaryFacet();

            } else if (entity instanceof TLBusinessObject) {
                substitutableFacet = ((TLBusinessObject) entity).getIdFacet();
                
            } else if (entity instanceof TLChoiceObject) {
                substitutableFacet = ((TLChoiceObject) entity).getSharedFacet();
            }
            if (substitutableFacet != null) {
                addElementNameToRegistry(
                        XsdCodegenUtils.getSubstitutableElementName(substitutableFacet), entity);
            }
            addElementNameToRegistry(XsdCodegenUtils.getSubstitutionGroupElementName(entity),
                    entity);
        }
    }

    /**
     * Adds the given entity to the registry using the specified XML element name.
     * 
     * @param elementName
     *            the name of the schema element for the entity
     * @param entity
     *            the entity to be added to the registry
     */
    private void addElementNameToRegistry(QName elementName, NamedEntity entity) {
        if ((elementName != null) && (entity != null) && hasGlobalType(entity)) {
            Set<NamedEntity> registeredEntities = elementNameEntities.get(elementName);
            Set<QName> elementNames = entityElementNames.get(entity);

            if (registeredEntities == null) {
                registeredEntities = new HashSet<NamedEntity>();
                elementNameEntities.put(elementName, registeredEntities);
            }
            if (elementNames == null) {
                elementNames = new HashSet<QName>();
                entityElementNames.put(entity, elementNames);
            }
            registeredEntities.add(entity);
            elementNames.add(elementName);
        }
    }
    
    /**
     * Returns true if the given entity has a global schema type definition associated with it.
     * 
     * @param entity  the entity to check
     * @return boolean
     */
    private boolean hasGlobalType(NamedEntity entity) {
    	boolean result = true;
    	
    	if (entity instanceof TLActionFacet) {
    		NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( (TLActionFacet) entity );
    		result = (payloadType == entity);
    	}
    	return result;
    }

    /**
     * Visitor that constructs the registry of all global type and element names in the model.
     */
    private class SchemaNameVisitor extends ModelElementVisitorAdapter {

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
         */
        @Override
        public boolean visitSimple(TLSimple simple) {
            addTypeNameToRegistry(simple);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitClosedEnumeration(org.opentravel.schemacompiler.model.TLClosedEnumeration)
         */
        @Override
        public boolean visitClosedEnumeration(TLClosedEnumeration enumeration) {
            addTypeNameToRegistry(enumeration);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitOpenEnumeration(org.opentravel.schemacompiler.model.TLOpenEnumeration)
         */
        @Override
        public boolean visitOpenEnumeration(TLOpenEnumeration enumeration) {
            addTypeNameToRegistry(enumeration);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
         */
        @Override
        public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
            addTypeNameToRegistry(valueWithAttributes);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitCoreObject(org.opentravel.schemacompiler.model.TLCoreObject)
         */
        @Override
        public boolean visitCoreObject(TLCoreObject coreObject) {
            addElementNamesToRegistry(coreObject);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitBusinessObject(org.opentravel.schemacompiler.model.TLBusinessObject)
         */
        @Override
        public boolean visitBusinessObject(TLBusinessObject businessObject) {
            addElementNamesToRegistry(businessObject);
            return true;
        }

        /**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitChoiceObject(org.opentravel.schemacompiler.model.TLChoiceObject)
		 */
		@Override
		public boolean visitChoiceObject(TLChoiceObject choiceObject) {
            addElementNamesToRegistry(choiceObject);
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResource(org.opentravel.schemacompiler.model.TLResource)
		 */
		@Override
		public boolean visitResource(TLResource resource) {
            addTypeNameToRegistry(resource);
            return true;
		}

		/**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitFacet(org.opentravel.schemacompiler.model.TLFacet)
         */
        @Override
        public boolean visitFacet(TLFacet facet) {
            boolean hasContent = new FacetCodegenDelegateFactory(null).getDelegate(facet).hasContent();

            if (hasContent) {
                addTypeNameToRegistry(facet);
            }
            addElementNamesToRegistry(facet);
            return true;
        }

        /**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionFacet(org.opentravel.schemacompiler.model.TLActionFacet)
		 */
		@Override
		public boolean visitActionFacet(TLActionFacet facet) {
            boolean hasContent = (facet.getReferenceType() != null) ||
            		(facet.getReferenceType() != TLReferenceType.NONE);

            if (hasContent) {
                addElementNameToRegistry(XsdCodegenUtils.getGlobalElementName(facet), facet);
                addTypeNameToRegistry(facet);
            }
            return true;
		}

		/**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
         */
        @Override
        public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
            if (!simpleFacet.isEmptyType()) {
                addTypeNameToRegistry(simpleFacet);
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitListFacet(org.opentravel.schemacompiler.model.TLListFacet)
         */
        @Override
        public boolean visitListFacet(TLListFacet listFacet) {
            boolean hasContent = new FacetCodegenDelegateFactory(null).getDelegate(listFacet)
                    .hasContent();

            if (hasContent) {
                addTypeNameToRegistry(listFacet);
            }
            addElementNamesToRegistry(listFacet);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAlias(org.opentravel.schemacompiler.model.TLAlias)
         */
        @Override
        public boolean visitAlias(TLAlias alias) {
            addElementNamesToRegistry(alias);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDSimpleType(org.opentravel.schemacompiler.model.XSDSimpleType)
         */
        @Override
        public boolean visitXSDSimpleType(XSDSimpleType xsdSimple) {
            addTypeNameToRegistry(xsdSimple);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDComplexType(org.opentravel.schemacompiler.model.XSDComplexType)
         */
        @Override
        public boolean visitXSDComplexType(XSDComplexType xsdComplex) {
            addTypeNameToRegistry(xsdComplex);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDElement(org.opentravel.schemacompiler.model.XSDElement)
         */
        @Override
        public boolean visitXSDElement(XSDElement xsdElement) {
            addElementNameToRegistry(
                    new QName(xsdElement.getNamespace(), xsdElement.getLocalName()), xsdElement);
            return true;
        }

    }

}
