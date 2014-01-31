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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

/**
 * Component that recursively analyzes references between simple types to identify any circular
 * references among model components.
 * 
 * @author S. Livezey
 */
public class CircularReferenceChecker {

    /**
     * Performs a recursive check to determine whether any circular references exist for the given
     * model element.
     * 
     * @param simple
     *            the simple type to be analyzed
     * @return boolean
     */
    public static boolean hasCircularReference(TLSimple simple) {
        return checkCircularReference(simple.getParentType(), simple, new HashSet<NamedEntity>());
    }

    /**
     * Performs a recursive check to determine whether any circular references exist for the given
     * model element.
     * 
     * @param simpleFacet
     *            the simple facet to be analyzed
     * @return boolean
     */
    public static boolean hasCircularReference(TLSimpleFacet simpleFacet) {
        return checkCircularReference(simpleFacet.getSimpleType(), simpleFacet,
                new HashSet<NamedEntity>());
    }

    /**
     * Recursive method that searches the dependency tree to identify circular references for the
     * given entity.
     * 
     * @param referencedEntity
     *            the referenced entity to be analyzed
     * @param originalEntity
     *            the original element that is being checked for circular references
     * @param visitedEntities
     *            the set of entities that have already been checked
     * @return boolean
     */
    private static boolean checkCircularReference(NamedEntity referencedEntity,
            NamedEntity originalEntity, Set<NamedEntity> visitedEntities) {
        boolean result = false;

        if (referencedEntity != null) {
            if (referencedEntity == originalEntity) {
                result = true;

            } else if (!visitedEntities.contains(referencedEntity)) {

                visitedEntities.add(referencedEntity);

                if (referencedEntity instanceof TLSimple) {
                    result = checkCircularReference(((TLSimple) referencedEntity).getParentType(),
                            originalEntity, visitedEntities);

                } else if (referencedEntity instanceof TLSimpleFacet) {
                    result = checkCircularReference(
                            ((TLSimpleFacet) referencedEntity).getSimpleType(), originalEntity,
                            visitedEntities);

                } else if (referencedEntity instanceof TLCoreObject) {
                    result = checkCircularReference(
                            ((TLCoreObject) referencedEntity).getSimpleFacet(), originalEntity,
                            visitedEntities);
                }
            }
        }
        return result;
    }

    /**
     * Performs a recursive check to determine whether any circular references exist for the given
     * VWA.
     * 
     * @param vwa
     *            the value-with-attributes to be analyzed
     * @return boolean
     */
    public static boolean hasCircularReference(TLValueWithAttributes vwa) {
        return checkCircularReference(vwa, vwa, new HashSet<TLValueWithAttributes>());
    }

    /**
     * Recursive method that searches the dependency tree to identify circular references for the
     * given VWA.
     * 
     * @param referencedEntity
     *            the VWA to be analyzed
     * @param originalEntity
     *            the original VWA that is being checked for circular references
     * @param visitedEntities
     *            the set of VWA's that have already been checked
     * @return boolean
     */
    private static boolean checkCircularReference(TLValueWithAttributes referencedEntity,
            TLValueWithAttributes originalEntity, Set<TLValueWithAttributes> visitedEntities) {
        boolean result = false;

        if (referencedEntity != null) {
            if (visitedEntities.contains(referencedEntity) && (referencedEntity == originalEntity)) {
                result = true;

            } else {
                visitedEntities.add(referencedEntity);

                if (referencedEntity.getParentType() instanceof TLValueWithAttributes) {
                    result = checkCircularReference(
                            (TLValueWithAttributes) referencedEntity.getParentType(),
                            originalEntity, visitedEntities);
                }
                if (!result) {
                    for (TLAttribute attribute : referencedEntity.getAttributes()) {
                        if (attribute.getType() instanceof TLValueWithAttributes) {
                            result = checkCircularReference(
                                    (TLValueWithAttributes) attribute.getType(), originalEntity,
                                    visitedEntities);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Performs a recursive check to determine whether any circular references exist for the given
     * facet property. Normally, circular references are allowed for complex types. Such recursive
     * references are only illegal when a cycle exists in which all of the referenced properties are
     * mandatory. In those cases, a valid XML document can never be constructed even though the
     * resulting schema may be valid.
     * 
     * @param element
     *            the facet property to be analyzed
     * @return boolean
     */
    public static boolean hasCircularReference(TLProperty element) {
        return !element.isMandatory() ? false : checkCircularReference(element.getType(),
                element.getPropertyOwner(), new HashSet<TLPropertyType>());
    }

    /**
     * Recursive method that searches the dependency tree to identify circular references for the
     * owner of the given element type.
     * 
     * @param elementType
     *            the type of the model element that is being checked for circular references
     * @param originalElementOwner
     *            the original element's owner that is being checked for circular references
     * @param visitedEntities
     *            the set of extension owners that have already been checked
     * @return boolean
     */
    private static boolean checkCircularReference(TLPropertyType elementType,
            TLPropertyOwner originalElementOwner, Set<TLPropertyType> visitedEntities) {
        boolean result = false;

        if ((elementType != null) && (originalElementOwner != null)) {
            if (elementType.equals(originalElementOwner)) {
                result = true;

            } else if (!visitedEntities.contains(elementType)) {
                List<TLProperty> referencedElements = null;

                visitedEntities.add(elementType);

                // If the referenced type is an alias, find its owner
                if (elementType instanceof TLAlias) {
                    TLAliasOwner aliasOwner = ((TLAlias) elementType).getOwningEntity();

                    if (aliasOwner instanceof TLPropertyType) {
                        elementType = (TLPropertyType) aliasOwner;
                        visitedEntities.add(elementType);

                    } else {
                        elementType = null;
                    }
                }

                // If the referenced type is a business object or core, find its summary facet
                if (elementType instanceof TLBusinessObject) {
                    elementType = ((TLBusinessObject) elementType).getSummaryFacet();
                    visitedEntities.add(elementType);

                } else if (elementType instanceof TLCoreObject) {
                    elementType = ((TLCoreObject) elementType).getSummaryFacet();
                    visitedEntities.add(elementType);
                }

                // If the resolve element type is a TLFacet, obtain a list of its inherited
                // properties
                if (elementType instanceof TLFacet) {
                    referencedElements = PropertyCodegenUtils
                            .getInheritedProperties((TLFacet) elementType);
                }

                // If we are dealing with a complex facet type, check each inherited element for
                // circular references
                if (referencedElements != null) {
                    for (TLProperty referencedElement : referencedElements) {
                        if (!referencedElement.isMandatory()) {
                            continue; // optional elements cannot cause circular reference errors in
                                      // complex types
                        }
                        result = checkCircularReference(referencedElement.getType(),
                                originalElementOwner, visitedEntities);
                        if (result)
                            break; // stop looking if we found a circular reference
                    }
                }
            }
        }
        return result;
    }

    /**
     * Performs a recursive check to determine whether any circular extension references exist for
     * the owner of the given extension.
     * 
     * @param extension
     *            the entity extension to be analyzed
     * @return boolean
     */
    public static boolean hasCircularExtension(TLExtension extension) {
        boolean result = false;

        if (extension != null) {
            result = checkCircularExtension(extension.getExtendsEntity(), extension.getOwner(),
                    new HashSet<NamedEntity>());
        }
        return result;
    }

    /**
     * Recursive method that searches the dependency tree to identify circular references for the
     * owner of the given extension.
     * 
     * @param extendedEntity
     *            the entity that is referenced by an extension
     * @param originalExtensionOwner
     *            the original extension owner that is being checked for circular references
     * @param visitedEntities
     *            the set of extension owners that have already been checked
     * @return boolean
     */
    private static boolean checkCircularExtension(NamedEntity extendedEntity,
            TLExtensionOwner originalEntity, Set<NamedEntity> visitedEntities) {
        boolean result = false;

        if (extendedEntity != null) {
            if (extendedEntity == originalEntity) {
                result = true;

            } else if (!visitedEntities.contains(extendedEntity)) {

                visitedEntities.add(extendedEntity);

                if (extendedEntity instanceof TLExtensionOwner) {
                    TLExtension extension = ((TLExtensionOwner) extendedEntity).getExtension();

                    if (extension != null) {
                        result = checkCircularExtension(extension.getExtendsEntity(),
                                originalEntity, visitedEntities);
                    }
                }
            }
        }
        return result;
    }

}
