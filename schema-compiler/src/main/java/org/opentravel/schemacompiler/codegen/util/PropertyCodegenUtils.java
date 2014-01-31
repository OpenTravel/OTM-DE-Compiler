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
package org.opentravel.schemacompiler.codegen.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

/**
 * Shared static methods used during the code generation for <code>TLProperty</code> elements.
 * 
 * @author S. Livezey
 */
public class PropertyCodegenUtils {

    /**
     * Returns true if a global element declaration is to be generated for the given property type.
     * 
     * @param propertyType
     *            the property type to analyze
     * @return boolean
     */
    public static boolean hasGlobalElement(TLPropertyType propertyType) {
        boolean result;

        if (propertyType != null) {
            if ((propertyType instanceof TLCoreObject) || (propertyType instanceof TLRole)) {
                // Core objects and roles have global elements, even though they also implement
                // TLAttributeType
                result = true;

            } else {
                result = !(propertyType instanceof TLAttributeType)
                        && !(propertyType instanceof TLValueWithAttributes)
                        && !(propertyType instanceof TLOpenEnumeration)
                        && !(propertyType instanceof TLRoleEnumeration)
                        && !(propertyType instanceof TLListFacet);
            }
        } else {
            result = false;
        }
        return result;
    }

    /**
     * Returns the default name that will be assigned in a generated schema for elements of the
     * assigned type. Default element names are only assigned for elements that are typed as cores
     * and business objects; for all other element types, this method will return null.
     * 
     * @param propertyType
     *            the assigned type of the model property
     * @param isReferenceProperty
     *            indicates whether the type is assigned by value or reference
     * @return QName
     */
    public static QName getDefaultSchemaElementName(NamedEntity propertyType,
            boolean isReferenceProperty) {
        TLListFacet listFacet = null;
        QName elementName = null;

        // Special case to process non-simple list facets even though they do not have a
        // globally-defined element name
        if (propertyType instanceof TLListFacet) {
            listFacet = (TLListFacet) propertyType;

        } else if (propertyType instanceof TLAlias) {
            TLAlias alias = (TLAlias) propertyType;

            if (alias.getOwningEntity() instanceof TLListFacet) {
                listFacet = (TLListFacet) alias.getOwningEntity();
            }
        }
        if ((listFacet != null) && (listFacet.getFacetType() == TLFacetType.SIMPLE)) {
            listFacet = null; // Do not process simple list facets
        }

        // Determine the correct method of calculating the element's default name
        if ((propertyType instanceof TLPropertyType)
                && ((listFacet != null) || hasGlobalElement((TLPropertyType) propertyType))) {

            if (XsdCodegenUtils.isSimpleCoreObject(propertyType)) {
                // Special case for simple cores that do not declare a substitution group element
                elementName = XsdCodegenUtils.getGlobalElementName(propertyType);
            }
            if (elementName == null) {
                // If the property type is a non-simple core or business object, this method call
                // will return
                // the QName of the substitution group element (or the substitutable summary
                // element).
                if (!isReferenceProperty) {
                    elementName = XsdCodegenUtils.getSubstitutionGroupElementName(propertyType);

                } else {
                    if (propertyType instanceof TLAlias) {
                        TLAlias propertyAliasType = (TLAlias) propertyType;
                        TLAlias summaryAlias = null;

                        if ((propertyAliasType.getOwningEntity() instanceof TLCoreObject)
                                || (propertyAliasType.getOwningEntity() instanceof TLBusinessObject)) {
                            summaryAlias = AliasCodegenUtils.getFacetAlias(propertyAliasType,
                                    TLFacetType.SUMMARY);
                        }
                        if (summaryAlias != null) {
                            XsdCodegenUtils.getSubstitutableElementName(summaryAlias);
                        }
                    } else {
                        TLFacet summaryFacet = null;

                        if (propertyType instanceof TLCoreObject) {
                            summaryFacet = ((TLCoreObject) propertyType).getSummaryFacet();

                        } else if (propertyType instanceof TLBusinessObject) {
                            summaryFacet = ((TLBusinessObject) propertyType).getSummaryFacet();
                        }
                        if (summaryFacet != null) {
                            XsdCodegenUtils.getSubstitutableElementName(summaryFacet);
                        }
                    }
                }
            }
            if (elementName == null) {
                // Default handling for all element references that were not covered by the previous
                // conditions.
                elementName = XsdCodegenUtils.getGlobalElementName(propertyType);
            }

            // Assign the "Ref" suffix for element references
            if ((elementName != null) && isReferenceProperty) {
                elementName = new QName(elementName.getNamespaceURI(), elementName.getLocalPart()
                        + "Ref");
            }
        }
        return elementName;
    }

    /**
     * Performs a similar name calculation as the <code>getDefaultSchemaElementName()</code> with
     * one exception. If the name of the schema element ends in "SubGrp", the suffix will be removed
     * from the local part of the name that is returned by this method.
     * 
     * @param propertyType
     *            the assigned type of the model property
     * @param isReferenceProperty
     *            indicates whether the type is assigned by value or reference
     * @return QName
     */
    public static QName getDefaultXmlElementName(NamedEntity propertyType,
            boolean isReferenceProperty) {
        QName elementName = getDefaultSchemaElementName(propertyType, isReferenceProperty);

        if ((elementName != null) && elementName.getLocalPart().endsWith("SubGrp")) {
            String localName = elementName.getLocalPart();
            elementName = new QName(elementName.getNamespaceURI(), localName.substring(0,
                    localName.length() - 6));
        }
        return elementName;
    }

    /**
     * Returns the list of attributes that were declared by the given VWA or inherited from other
     * VWA's when assigned as the parent type of the given one. Attributes are guranteed to be in
     * the correct order of their declaration in the sequencing of the inheritance hierarchy.
     * 
     * <p>
     * NOTE: Attributes with duplicate names are NOT included in the results from this method
     * 
     * @param vwa
     *            the value-with-attributes for which to retrieve inherited attributes
     * @return List<TLAttribute>
     */
    public static List<TLAttribute> getInheritedAttributes(TLValueWithAttributes vwa) {
        return getInheritedAttributes(vwa, false);
    }

    /**
     * Returns the list of attributes that were declared by the given VWA or inherited from other
     * VWA's when assigned as the parent type of the given one. Attributes are guranteed to be in
     * the correct order of their declaration in the sequencing of the inheritance hierarchy.
     * 
     * @param vwa
     *            the value-with-attributes for which to retrieve inherited attributes
     * @param includeDuplicateNames
     *            flag indicating whether attributes with duplicate names should be included in the
     *            results
     * @return List<TLAttribute>
     */
    public static List<TLAttribute> getInheritedAttributes(TLValueWithAttributes vwa,
            boolean includeDuplicateNames) {
        List<TLAttribute> attributeList = new ArrayList<TLAttribute>();

        findInheritedAttributes(vwa, includeDuplicateNames, attributeList,
                new HashSet<TLValueWithAttributes>());
        return attributeList;
    }

    /**
     * Recursive method that constructs the list of all VWA attributes, including those attributes
     * inherited from other VWA's.
     * 
     * @param vwa
     *            the value-with-attributes for which to retrieve inherited attributes
     * @param includeDuplicateNames
     *            flag indicating whether attributes with duplicate names should be included in the
     *            results
     * @param attributeList
     *            the list of attributes being constructed
     * @param visitedVWAs
     *            the collection of VWA's that have already been visited (used to prevent infinite
     *            loops)
     */
    private static void findInheritedAttributes(TLValueWithAttributes vwa,
            boolean includeDuplicateNames, List<TLAttribute> attributeList,
            Collection<TLValueWithAttributes> visitedVWAs) {
        if (!visitedVWAs.contains(vwa)) {

            visitedVWAs.add(vwa);

            for (TLAttribute attribute : vwa.getAttributes()) {
                boolean canAdd = true;

                if (!includeDuplicateNames) {
                    String attributeName = attribute.getName();

                    for (TLAttribute existingAttribute : attributeList) {
                        if ((attributeName != null)
                                && attributeName.equals(existingAttribute.getName())) {
                            canAdd = false;
                            break;
                        }
                    }
                }
                if (canAdd) {
                    attributeList.add(attribute);
                }
            }

            // Recurse into VWA attributes to find inherited items
            for (TLAttribute attribute : vwa.getAttributes()) {
                if (attribute.getType() instanceof TLValueWithAttributes) {
                    findInheritedAttributes((TLValueWithAttributes) attribute.getType(),
                            includeDuplicateNames, attributeList, visitedVWAs);
                }
            }

            // Recurse into the parent type to find inherited items
            if (vwa.getParentType() instanceof TLValueWithAttributes) {
                findInheritedAttributes((TLValueWithAttributes) vwa.getParentType(),
                        includeDuplicateNames, attributeList, visitedVWAs);
            }
        }
    }

    /**
     * Returns the list of indicators that were declared by the given VWA or inherited from other
     * VWA's when assigned as the parent type of the given one. Indicators are guranteed to be in
     * the correct order of their declaration in the sequencing of the inheritance hierarchy.
     * 
     * @param vwa
     *            the value-with-attributes for which to retrieve inherited indicators
     * @return List<TLIndicator>
     */
    public static List<TLIndicator> getInheritedIndicators(TLValueWithAttributes vwa) {
        List<TLIndicator> indicatorList = new ArrayList<TLIndicator>();

        findInheritedIndicators(vwa, indicatorList, new HashSet<TLValueWithAttributes>());
        return indicatorList;
    }

    /**
     * Recursive method that constructs the list of all VWA indicators, including those indicators
     * inherited from other VWA's.
     * 
     * @param vwa
     *            the value-with-attributes for which to retrieve inherited indicators
     * @param indicatorList
     *            the list of indicators being constructed
     * @param visitedVWAs
     *            the collection of VWA's that have already been visited (used to prevent infinite
     *            loops)
     */
    private static void findInheritedIndicators(TLValueWithAttributes vwa,
            List<TLIndicator> indicatorList, Collection<TLValueWithAttributes> visitedVWAs) {
        if (!visitedVWAs.contains(vwa)) {

            visitedVWAs.add(vwa);
            indicatorList.addAll(vwa.getIndicators());

            for (TLAttribute attribute : vwa.getAttributes()) {
                if (attribute.getType() instanceof TLValueWithAttributes) {
                    findInheritedIndicators((TLValueWithAttributes) attribute.getType(),
                            indicatorList, visitedVWAs);
                }
            }
            if (vwa.getParentType() instanceof TLValueWithAttributes) {
                findInheritedIndicators((TLValueWithAttributes) vwa.getParentType(), indicatorList,
                        visitedVWAs);
            }
        }
    }

    /**
     * Returns the list of attributes that were declared by the given facet or inherited from other
     * facets, including higher-level facets from the same owner. Attributes are guranteed to be in
     * the correct order of their declaration in the sequencing of the inheritance hierarchy.
     * 
     * @param facet
     *            the facet for which to retrieve inherited attributes
     * @return List<TLAttribute>
     */
    public static List<TLAttribute> getInheritedAttributes(TLFacet facet) {
        List<TLFacet> localFacetHierarchy = FacetCodegenUtils.getLocalFacetHierarchy(facet);
        List<TLAttribute> attributeList = new ArrayList<TLAttribute>();

        for (TLFacet aFacet : localFacetHierarchy) {
            attributeList.addAll(getInheritedFacetAttributes(aFacet));
        }
        return attributeList;
    }

    /**
     * Returns the list of attributes that were declared by the given facet or inherited from facets
     * of the same type. Attributes are guranteed to be in the correct order of their declaration in
     * the sequencing of the inheritance hierarchy.
     * 
     * @param facet
     *            the facet for which to retrieve inherited attributes
     * @return List<TLAttribute>
     */
    public static List<TLAttribute> getInheritedFacetAttributes(TLFacet facet) {
        Collection<TLFacetOwner> visitedOwners = new HashSet<TLFacetOwner>();
        List<TLAttribute> attributeList = new ArrayList<TLAttribute>();
        TLFacetOwner facetOwner = facet.getOwningEntity();

        while (facetOwner != null) {
            if (visitedOwners.contains(facetOwner)) {
                break;
            }
            TLFacet aFacet = FacetCodegenUtils.getFacetOfType(facetOwner, facet.getFacetType(),
                    facet.getContext(), facet.getLabel());

            if (aFacet != null) {
                List<TLAttribute> localAttributes = new ArrayList<TLAttribute>(
                        aFacet.getAttributes());

                // We are traversing upward in the inheritance hierarchy, so we must pre-pend
                // attributes onto
                // the list in the reverse order of their declarations in order to preserve the
                // intende order
                // of occurrance.
                Collections.reverse(localAttributes);

                for (TLAttribute attribute : localAttributes) {
                    attributeList.add(0, attribute);
                }
            }
            visitedOwners.add(facetOwner);
            facetOwner = FacetCodegenUtils.getFacetOwnerExtension(facetOwner);
        }
        return attributeList;
    }

    /**
     * Returns the list of indicators that were declared by the given facet or inherited from other
     * facets, including higher-level facets from the same owner. Indicators are guranteed to be in
     * the correct order of their declaration in the sequencing of the inheritance hierarchy.
     * 
     * @param facet
     *            the facet for which to retrieve inherited indicators
     * @return List<TLIndicator>
     */
    public static List<TLIndicator> getInheritedIndicators(TLFacet facet) {
        List<TLFacet> localFacetHierarchy = FacetCodegenUtils.getLocalFacetHierarchy(facet);
        List<TLIndicator> indicatorList = new ArrayList<TLIndicator>();

        for (TLFacet aFacet : localFacetHierarchy) {
            indicatorList.addAll(getInheritedFacetIndicators(aFacet));
        }
        return indicatorList;
    }

    /**
     * Returns the list of indicators that were declared by the given facet or inherited from facets
     * of the same type. Indicators are guranteed to be in the correct order of their declaration in
     * the sequencing of the inheritance hierarchy.
     * 
     * @param facet
     *            the facet for which to retrieve inherited indicators
     * @return List<TLIndicator>
     */
    public static List<TLIndicator> getInheritedFacetIndicators(TLFacet facet) {
        Collection<TLFacetOwner> visitedOwners = new HashSet<TLFacetOwner>();
        List<TLIndicator> indicatorList = new ArrayList<TLIndicator>();
        TLFacetOwner facetOwner = facet.getOwningEntity();

        while (facetOwner != null) {
            if (visitedOwners.contains(facetOwner)) {
                break;
            }
            TLFacet aFacet = FacetCodegenUtils.getFacetOfType(facetOwner, facet.getFacetType(),
                    facet.getContext(), facet.getLabel());

            if (aFacet != null) {
                List<TLIndicator> localIndicators = new ArrayList<TLIndicator>(
                        aFacet.getIndicators());

                // We are traversing upward in the inheritance hierarchy, so we must pre-pend
                // indicators onto
                // the list in the reverse order of their declarations in order to preserve the
                // intende order
                // of occurrance.
                Collections.reverse(localIndicators);

                for (TLIndicator indicator : localIndicators) {
                    indicatorList.add(0, indicator);
                }
            }
            visitedOwners.add(facetOwner);
            facetOwner = FacetCodegenUtils.getFacetOwnerExtension(facetOwner);
        }
        return indicatorList;
    }

    /**
     * Returns the list of properties that were declared by the given facet or inherited from other
     * facets, including higher-level facets from the same owner. Properties are guranteed to be in
     * the correct order of their declaration in the sequencing of the inheritance hierarchy.
     * 
     * @param facet
     *            the facet for which to retrieve inherited properties
     * @return List<TLProperty>
     */
    public static List<TLProperty> getInheritedProperties(TLFacet facet) {
        List<TLFacet> localFacetHierarchy = FacetCodegenUtils.getLocalFacetHierarchy(facet);
        List<TLProperty> propertyList = new ArrayList<TLProperty>();

        for (TLFacet aFacet : localFacetHierarchy) {
            propertyList.addAll(getInheritedFacetProperties(aFacet));
        }
        return propertyList;
    }

    /**
     * Returns the list of properties that were declared by the given facet or inherited from facets
     * of the same type. Properties are guranteed to be in the correct order of their declaration in
     * the sequencing of the inheritance hierarchy.
     * 
     * @param facet
     *            the facet for which to retrieve inherited properties
     * @return List<TLProperty>
     */
    public static List<TLProperty> getInheritedFacetProperties(TLFacet facet) {
        Collection<TLFacetOwner> visitedOwners = new HashSet<TLFacetOwner>();
        Set<NamedEntity> inheritanceRoots = new HashSet<NamedEntity>();
        List<TLProperty> propertyList = new ArrayList<TLProperty>();
        TLFacetOwner facetOwner = facet.getOwningEntity();

        while (facetOwner != null) {
            if (visitedOwners.contains(facetOwner)) {
                break;
            }
            TLFacet aFacet = FacetCodegenUtils.getFacetOfType(facetOwner, facet.getFacetType(),
                    facet.getContext(), facet.getLabel());

            if (aFacet != null) {
                List<TLProperty> localProperties = new ArrayList<TLProperty>(aFacet.getElements());

                // We are traversing upward in the inheritance hierarchy, so we must pre-pend
                // properties onto
                // the list in the reverse order of their declarations in order to preserve the
                // intende order
                // of occurrance.
                Collections.reverse(localProperties);

                for (TLProperty property : localProperties) {
                    TLPropertyType propertyType = resolvePropertyType(property.getPropertyOwner(),
                            property.getType());
                    NamedEntity inheritanceRoot = getInheritanceRoot(propertyType);

                    // Properties whose types are members of an inheritance hierarchy should be
                    // skipped
                    // if they were eclipsed by lower-level properties of the owner's hierarchy
                    if ((inheritanceRoot == null) || !inheritanceRoots.contains(inheritanceRoot)) {
                        if (inheritanceRoot != null) {
                            inheritanceRoots.add(inheritanceRoot);
                        }
                        propertyList.add(0, property);
                    }
                }
            }
            visitedOwners.add(facetOwner);
            facetOwner = FacetCodegenUtils.getFacetOwnerExtension(facetOwner);
        }
        return propertyList;
    }

    /**
     * Returns the list of <code>TLRole</code> entities that are defined and inherited by the given
     * core object.
     * 
     * @param coreObject
     *            the core object for which to return roles
     * @return List<TLRole>
     */
    public static List<TLRole> getInheritedRoles(TLCoreObject coreObject) {
        List<TLCoreObject> coreObjects = new ArrayList<TLCoreObject>();
        List<String> roleNames = new ArrayList<String>();
        List<TLRole> roles = new ArrayList<TLRole>();
        TLFacetOwner core = coreObject;

        while (core != null) {
            if (core instanceof TLCoreObject) {
                coreObjects.add(0, (TLCoreObject) core);
            }
            core = FacetCodegenUtils.getFacetOwnerExtension(core);
        }

        for (TLCoreObject c : coreObjects) {
            for (TLRole role : c.getRoleEnumeration().getRoles()) {
                if (!roleNames.contains(role.getName())) {
                    roles.add(role);
                    roleNames.add(role.getName());
                }
            }
        }
        return roles;
    }

    /**
     * Analyzes the given property instance to determine the root of its inheritance hierarchy. This
     * information is typically used to determine whether two properties from different levels of a
     * containing entity's hierarchy should eclipse one another during code generation. If the given
     * property type is not capable of being a member of an inheritance hierarchy (e.g. a simple
     * type or VWA), this method will return null.
     * 
     * @param propertyType
     *            the property type to analyze
     * @return NamedEntity
     */
    public static NamedEntity getInheritanceRoot(TLPropertyType propertyType) {
        NamedEntity inheritanceRoot = null;

        if (isFacetPropertyType(propertyType)) {
            TLFacet facet = null;
            TLAlias alias = null;

            // Identify the query facet and (if applicable) the facet's alias
            if (propertyType instanceof TLAlias) {
                alias = (TLAlias) propertyType;
                propertyType = (TLPropertyType) alias.getOwningEntity();
                facet = (TLFacet) propertyType;
                inheritanceRoot = alias;
            } else {
                inheritanceRoot = facet = (TLFacet) propertyType;
            }

            // Traverse upward in the inheritance root, looking for the highest-level query facet
            // that matches the one passed to this method
            TLFacetOwner parentEntity = FacetCodegenUtils.getFacetOwnerExtension(facet
                    .getOwningEntity());

            while (parentEntity != null) {
                TLFacet baseQueryFacet = FacetCodegenUtils.getFacetOfType(parentEntity,
                        facet.getFacetType(), facet.getContext(), facet.getLabel());

                if (baseQueryFacet != null) {
                    inheritanceRoot = (alias == null) ? baseQueryFacet : baseQueryFacet
                            .getAlias(alias.getName());
                }
                parentEntity = FacetCodegenUtils.getFacetOwnerExtension(parentEntity);
            }

        } else { // non-facet property types
            TLFacetOwner facetOwner = null;
            TLAlias ownerAlias = null;

            // Identify the facet owner and (if applicable) the facet owner's alias
            if (propertyType instanceof TLAlias) {
                TLAlias alias = (TLAlias) propertyType;

                if (alias.getOwningEntity() instanceof TLFacetOwner) {
                    facetOwner = (TLFacetOwner) alias.getOwningEntity();
                    ownerAlias = alias;
                }
                inheritanceRoot = ownerAlias;
            } else {
                if (propertyType instanceof TLFacetOwner) {
                    facetOwner = (TLFacetOwner) propertyType;
                }
                inheritanceRoot = facetOwner;
            }

            // If we are not at the root of the inheritance hierarchy, traverse upward until we get
            // there
            if (facetOwner != null) {
                TLFacetOwner parentEntity = FacetCodegenUtils.getFacetOwnerExtension(facetOwner);

                while (parentEntity != null) {
                    facetOwner = parentEntity;
                    inheritanceRoot = (ownerAlias == null) ? facetOwner
                            : ((TLAliasOwner) facetOwner).getAlias(ownerAlias.getName());
                    parentEntity = FacetCodegenUtils.getFacetOwnerExtension(parentEntity);
                }
            }
        }
        return inheritanceRoot;
    }

    /**
     * Returns true if the given property is a reference to a complex type, or is assigned as a
     * simple type of "IDREF" or "IDREFS".
     * 
     * @param property
     *            the model property to analyze
     * @return boolean
     */
    public static boolean isReferenceProperty(TLProperty property) {
        boolean result = false;

        if (property.isReference()) {
            result = true;

        } else {
            TLPropertyType propertyType = property.getType();

            if ((propertyType != null)
                    && XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(propertyType.getNamespace())) {
                result = propertyType.getLocalName().equals("IDREF")
                        || propertyType.getLocalName().equals("IDREFS");
            }
        }
        return result;
    }

    /**
     * Returns true if the property type passed into this method is either a facet or an alias for a
     * facet.
     * 
     * @param propertyType
     *            the property type to analyze
     * @return boolean
     */
    private static boolean isFacetPropertyType(TLPropertyType propertyType) {
        TLFacet facet = null;

        if (propertyType instanceof TLAlias) {
            propertyType = (TLPropertyType) ((TLAlias) propertyType).getOwningEntity();
        }
        if (propertyType instanceof TLFacet) {
            facet = (TLFacet) propertyType;
        }
        return (facet != null);
    }

    /**
     * Resolves the assigned property type into the actual property type that should be used for
     * code generation. This is typically the same type, but it differs if a business or core object
     * is referenced. In those cases, the resolved facet will be at the top of the substitution
     * group hierarchy for the referenced core or business object.
     * 
     * <p>
     * If the resolved type is a facet that does not have any defined content, this method will also
     * attempt to locate an alternate facet to use for the property's XSD element type.
     * 
     * @param owner
     *            the owning entity of the property being generated
     * @param assignedType
     *            the assigned type for the property
     * @return TLPropertyType
     */
    public static TLPropertyType resolvePropertyType(TLPropertyOwner owner,
            TLPropertyType assignedType) {
        TLPropertyType resolvedType;

        // If the assigned type is a non-simple list facet, use it's item facet as the assigned type
        if (assignedType instanceof TLListFacet) {
            TLAbstractFacet itemFacet = ((TLListFacet) assignedType).getItemFacet();

            if (!(itemFacet instanceof TLSimpleFacet)) {
                switch (itemFacet.getFacetType()) {
                    case SUMMARY:
                        assignedType = (TLPropertyType) itemFacet.getOwningEntity();
                        break;
                    case DETAIL:
                        assignedType = itemFacet;
                        break;
                }
            }
        }

        if (assignedType instanceof TLAbstractFacet) {
            // If the rendered property type resolves to a TLFacet, we need to make sure that
            // facet will be rendered in the XML schema output. If not, we need to find an
            // alternate facet from the same facet owner that will be rendered.
            resolvedType = findNonEmptyFacet(owner, (TLAbstractFacet) assignedType);

        } else {
            resolvedType = assignedType;
        }
        return resolvedType;
    }

    /**
     * If the given facet is considered "empty" (i.e. there are no generated elements), one of the
     * non-empty sibling facets from its owner should be returned. The priority of which sibling is
     * returned can be customized by sub-classes. By default, this method simply returns the
     * 'referencedFacet' that is passed.
     * 
     * @param originatingFacet
     *            the originating facet that owns the property making the reference
     * @param referencedFacet
     *            the facet being referenced (also the source facet used to lookup this delegate
     *            instance)
     * @return TLAbstractFacet
     */
    public static TLAbstractFacet findNonEmptyFacet(TLPropertyOwner originatingFacet,
            TLAbstractFacet referencedFacet) {
        FacetCodegenDelegateFactory factory = new FacetCodegenDelegateFactory(null);
        TLAbstractFacet result = referencedFacet;

        if ((referencedFacet != null) && !factory.getDelegate(referencedFacet).hasContent()) {
            TLAbstractFacet[] alternateFacets = getAlternateFacets(originatingFacet,
                    referencedFacet);

            for (TLAbstractFacet alternateFacet : alternateFacets) {
                if (factory.getDelegate(alternateFacet).hasContent()) {
                    result = alternateFacet;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * When a referenced facet is considered "empty", this method will provide a prioritized list of
     * the alternate sibling facets that should be considered for code generation.
     * 
     * @param referencedFacet
     *            the facet being referenced (also the source facet used to lookup this delegate
     *            instance)
     * @return TLAbstractFacet[]
     */
    private static TLAbstractFacet[] getAlternateFacets(TLPropertyOwner originatingFacet,
            TLAbstractFacet referencedFacet) {
        TLAbstractFacet[] results = new TLAbstractFacet[0];

        if (referencedFacet instanceof TLListFacet) {
            results = getAlternateFacets(originatingFacet,
                    (TLCoreObject) referencedFacet.getOwningEntity(), (TLListFacet) referencedFacet);

        } else if (referencedFacet instanceof TLFacet) {
            TLFacet origFacet = (TLFacet) referencedFacet;

            if (origFacet.getOwningEntity() instanceof TLBusinessObject) {
                if (referencedFacet.getOwningEntity() instanceof TLBusinessObject) {
                    results = getAlternateFacets((TLBusinessObject) origFacet.getOwningEntity(),
                            origFacet, (TLBusinessObject) referencedFacet.getOwningEntity(),
                            referencedFacet);

                } else if (referencedFacet.getOwningEntity() instanceof TLCoreObject) {
                    results = getAlternateFacets((TLBusinessObject) origFacet.getOwningEntity(),
                            origFacet, (TLCoreObject) referencedFacet.getOwningEntity(),
                            referencedFacet);
                }
            } else if (origFacet.getOwningEntity() instanceof TLCoreObject) {
                if (referencedFacet.getOwningEntity() instanceof TLBusinessObject) {
                    results = getAlternateFacets((TLCoreObject) origFacet.getOwningEntity(),
                            origFacet, (TLBusinessObject) referencedFacet.getOwningEntity(),
                            referencedFacet);

                } else if (referencedFacet.getOwningEntity() instanceof TLCoreObject) {
                    results = getAlternateFacets((TLCoreObject) origFacet.getOwningEntity(),
                            origFacet, (TLCoreObject) referencedFacet.getOwningEntity(),
                            referencedFacet);
                }
            } else if (origFacet.getOwningEntity() instanceof TLExtensionPointFacet) {
                if (referencedFacet.getOwningEntity() instanceof TLBusinessObject) {
                    results = getAlternateFacets(
                            (TLExtensionPointFacet) origFacet.getOwningEntity(), origFacet,
                            (TLBusinessObject) referencedFacet.getOwningEntity(), referencedFacet);

                } else if (referencedFacet.getOwningEntity() instanceof TLCoreObject) {
                    results = getAlternateFacets(
                            (TLExtensionPointFacet) origFacet.getOwningEntity(), origFacet,
                            (TLCoreObject) referencedFacet.getOwningEntity(), referencedFacet);
                }
            }
        }
        return results;
    }

    /**
     * Simple dispatch method used to decompose the problem from the primary 'getAlternateFacets()'
     * method.
     */
    @SuppressWarnings("unused")
    private static TLAbstractFacet[] getAlternateFacets(TLPropertyOwner originatingFacet,
            TLCoreObject referencedOwner, TLListFacet referencedFacet) {
        TLAbstractFacet[] results = new TLAbstractFacet[0];

        // The summary-list facet is the alternate for the info-list, and vice-versa
        if (referencedFacet.getFacetType() == TLFacetType.SUMMARY) {
            results = new TLAbstractFacet[] { referencedOwner.getDetailListFacet() };
        } else { // the info-list facet is referenced
            results = new TLAbstractFacet[] { referencedOwner.getSummaryListFacet() };
        }
        return results;
    }

    /**
     * Simple dispatch method used to decompose the problem from the primary 'getAlternateFacets()'
     * method.
     */
    @SuppressWarnings("unused")
    private static TLAbstractFacet[] getAlternateFacets(TLBusinessObject originatingOwner,
            TLFacet originatingFacet, TLBusinessObject referencedOwner,
            TLAbstractFacet referencedFacet) {
        TLAbstractFacet[] results = new TLAbstractFacet[0];

        switch (originatingFacet.getFacetType()) {
            case ID:
                switch (referencedFacet.getFacetType()) {
                    case ID:
                        // ID facets are required - no alternates required because it should never
                        // be "empty"
                        break;
                    case SUMMARY:
                        results = new TLAbstractFacet[] { referencedOwner.getIdFacet(),
                                referencedOwner.getDetailFacet() };
                        break;
                    case DETAIL:
                    case CUSTOM:
                        results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(),
                                referencedOwner.getIdFacet() };
                        break;
                }
                break;
            case SUMMARY:
            case CUSTOM:
            case QUERY:
                switch (referencedFacet.getFacetType()) {
                    case ID:
                        // ID facets are required - no alternates required because it should never
                        // be "empty"
                        break;
                    case SUMMARY:
                        results = new TLAbstractFacet[] { referencedOwner.getDetailFacet(),
                                referencedOwner.getIdFacet() };
                        break;
                    case DETAIL:
                        results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(),
                                referencedOwner.getIdFacet() };
                        break;
                    case CUSTOM:
                        results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(),
                                referencedOwner.getIdFacet() };
                        break;
                }
                break;
            case DETAIL:
                switch (referencedFacet.getFacetType()) {
                    case ID:
                        // ID facets are required - no alternates required because it should never
                        // be "empty"
                        break;
                    case SUMMARY:
                        results = new TLAbstractFacet[] { referencedOwner.getDetailFacet(),
                                referencedOwner.getIdFacet() };
                        break;
                    case DETAIL:
                        results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(),
                                referencedOwner.getIdFacet() };
                        break;
                    case CUSTOM:
                        results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(),
                                referencedOwner.getIdFacet() };
                        break;
                }
                break;
        }
        return results;
    }

    /**
     * Simple dispatch method used to decompose the problem from the primary 'getAlternateFacets()'
     * method.
     */
    @SuppressWarnings("unused")
    private static TLAbstractFacet[] getAlternateFacets(TLBusinessObject originatingOwner,
            TLFacet originatingFacet, TLCoreObject referencedOwner, TLAbstractFacet referencedFacet) {
        TLAbstractFacet[] results = new TLAbstractFacet[0];

        switch (originatingFacet.getFacetType()) {
            case ID:
                switch (referencedFacet.getFacetType()) {
                    case SIMPLE:
                        results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(),
                                referencedOwner.getDetailFacet() };
                        break;
                    case SUMMARY:
                        results = new TLAbstractFacet[] { referencedOwner.getSimpleFacet(),
                                referencedOwner.getDetailFacet() };
                        break;
                    case DETAIL:
                        results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(),
                                referencedOwner.getSimpleFacet() };
                        break;
                }
                break;
            case SUMMARY:
                switch (referencedFacet.getFacetType()) {
                    case SIMPLE:
                        results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(),
                                referencedOwner.getDetailFacet() };
                        break;
                    case SUMMARY:
                        results = new TLAbstractFacet[] { referencedOwner.getDetailFacet(),
                                referencedOwner.getSimpleFacet() };
                        break;
                    case DETAIL:
                        results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(),
                                referencedOwner.getSimpleFacet() };
                        break;
                }
                break;
            case DETAIL:
            case CUSTOM:
            case QUERY:
                switch (referencedFacet.getFacetType()) {
                    case SIMPLE:
                        results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(),
                                referencedOwner.getDetailFacet() };
                        break;
                    case SUMMARY:
                        results = new TLAbstractFacet[] { referencedOwner.getDetailFacet(),
                                referencedOwner.getSimpleFacet() };
                        break;
                    case DETAIL:
                        results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(),
                                referencedOwner.getSimpleFacet() };
                        break;
                }
                break;
        }
        return results;
    }

    /**
     * Simple dispatch method used to decompose the problem from the primary 'getAlternateFacets()'
     * method.
     */
    @SuppressWarnings("unused")
    private static TLAbstractFacet[] getAlternateFacets(TLCoreObject originatingOwner,
            TLFacet originatingFacet, TLBusinessObject referencedOwner,
            TLAbstractFacet referencedFacet) {
        return new TLAbstractFacet[0]; // Trivial Case: Core objects can only reference the ID facet
                                       // of a business object, so there are no alternates
    }

    /**
     * Simple dispatch method used to decompose the problem from the primary 'getAlternateFacets()'
     * method.
     */
    @SuppressWarnings("unused")
    private static TLAbstractFacet[] getAlternateFacets(TLCoreObject originatingOwner,
            TLFacet originatingFacet, TLCoreObject referencedOwner, TLAbstractFacet referencedFacet) {
        TLAbstractFacet[] results = new TLAbstractFacet[0];

        switch (referencedFacet.getFacetType()) {
            case SIMPLE:
                results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(),
                        referencedOwner.getDetailFacet() };
                break;
            case SUMMARY:
                results = new TLAbstractFacet[] { referencedOwner.getDetailFacet(),
                        referencedOwner.getSimpleFacet() };
                break;
            case DETAIL:
                results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(),
                        referencedOwner.getSimpleFacet() };
                break;
        }
        return results;
    }

    /**
     * Simple dispatch method used to decompose the problem from the primary 'getAlternateFacets()'
     * method.
     */
    @SuppressWarnings("unused")
    private static TLAbstractFacet[] getAlternateFacets(TLExtensionPointFacet originatingOwner,
            TLFacet originatingFacet, TLBusinessObject referencedOwner,
            TLAbstractFacet referencedFacet) {
        TLAbstractFacet[] results = new TLAbstractFacet[0];

        switch (referencedFacet.getFacetType()) {
            case ID:
                // ID facets are required - no alternates required because it should never be
                // "empty"
                break;
            case SUMMARY:
                results = new TLAbstractFacet[] { referencedOwner.getDetailFacet(),
                        referencedOwner.getIdFacet() };
                break;
            case DETAIL:
                results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(),
                        referencedOwner.getIdFacet() };
                break;
            case CUSTOM:
                results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(),
                        referencedOwner.getIdFacet() };
                break;
        }
        return results;
    }

    /**
     * Simple dispatch method used to decompose the problem from the primary 'getAlternateFacets()'
     * method.
     */
    @SuppressWarnings("unused")
    private static TLAbstractFacet[] getAlternateFacets(TLExtensionPointFacet originatingOwner,
            TLFacet originatingFacet, TLCoreObject referencedOwner, TLAbstractFacet referencedFacet) {
        TLAbstractFacet[] results = new TLAbstractFacet[0];

        switch (referencedFacet.getFacetType()) {
            case SIMPLE:
                results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(),
                        referencedOwner.getDetailFacet() };
                break;
            case SUMMARY:
                results = new TLAbstractFacet[] { referencedOwner.getDetailFacet(),
                        referencedOwner.getSimpleFacet() };
                break;
            case DETAIL:
                results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(),
                        referencedOwner.getSimpleFacet() };
                break;
        }
        return results;
    }

}
