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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.Versioned;

/**
 * Shared static methods used during the code generation for <code>TLProperty</code> elements.
 * 
 * @author S. Livezey
 */
public class PropertyCodegenUtils {
	
	/**
	 * Private constructor to prevent instantiation.
	 */
	private PropertyCodegenUtils() {
	}
	
	/**
	 * If the 'repeat' value of a property is greater than this threshold value, the XSD element definition will be
	 * created with a 'maxOccurs' value of "unbounded".
	 */
	public static final int MAX_OCCURS_UNBOUNDED_THRESHOLD = 5000;
	
	/**
	 * Returns true if a global element declaration is to be generated for the given entity.
	 * 
	 * @param entity the entity to analyze
	 * @return boolean
	 */
	public static boolean hasGlobalElement(NamedEntity entity) {
		boolean result;
		
		if (entity != null) {
			if ((entity instanceof TLCoreObject) || (entity instanceof TLRole)) {
				// Core objects and roles have global elements, even though they also implement
				// TLAttributeType
				result = true;
			} else if (entity instanceof TLActionFacet) {
				result = (ResourceCodegenUtils.getPayloadType( (TLActionFacet) entity ) == entity);
				// Action facets only define global elements if they are not simple
				// references to a core, choice, or business object.
				
			} else {
				result = !(entity instanceof TLAttributeType) && !(entity instanceof TLValueWithAttributes)
						&& !(entity instanceof TLOpenEnumeration) && !(entity instanceof TLRoleEnumeration)
						&& !(entity instanceof TLListFacet);
			}
		} else {
			result = false;
		}
		return result;
	}
	
	/**
	 * Returns the default name that will be assigned in a generated schema for elements of the assigned type. If the
	 * given property type does not have a default element name, this method will return null.
	 * 
	 * @param propertyType the assigned type of the model property
	 * @param isReferenceProperty indicates whether the type is assigned by value or reference
	 * @return QName
	 */
	public static QName getDefaultSchemaElementName(NamedEntity propertyType, boolean isReferenceProperty) {
		boolean isContextualFacet = (propertyType instanceof TLContextualFacet);
		TLListFacet listFacet = null;
		QName elementName = null;
		
		// Special case to process non-simple list facets even though they do not have a
		// globally-defined element name
		if (propertyType instanceof TLListFacet) {
			listFacet = (TLListFacet) propertyType;
			
		} else if (propertyType instanceof TLAlias) {
			TLAlias alias = (TLAlias) propertyType;
			TLAliasOwner aliasOwner = alias.getOwningEntity();
			
			if (aliasOwner instanceof TLListFacet) {
				listFacet = (TLListFacet) aliasOwner;
			}
			isContextualFacet = (aliasOwner instanceof TLContextualFacet);
		}
		if ((listFacet != null) && (listFacet.getFacetType() == TLFacetType.SIMPLE)) {
			listFacet = null; // Do not process simple list facets
		}
		
		// Determine the correct method of calculating the element's default name
		if ((listFacet != null) || hasGlobalElement( propertyType )) {
			elementName = getDefaultElementName( propertyType, isReferenceProperty, isContextualFacet );
		}
		return elementName;
	}

	/**
	 * Returns the default XML element name for the given property type.
	 * 
	 * @param propertyType  the property type for which to return the default XML element name.
	 * @param isReferenceProperty  flag indicating whether the property is a reference
	 * @param isContextualFacet  flag indicating whether the given property type is a contextual facet
	 * @return QName
	 */
	private static QName getDefaultElementName(NamedEntity propertyType, boolean isReferenceProperty,
			boolean isContextualFacet) {
		QName elementName = null;
		
		if (XsdCodegenUtils.isSimpleCoreObject( propertyType ) || isContextualFacet) {
			// Special cases for simple cores that do not declare a substitution group element,
			// and contextual facets which will never be rendered as substitution groups when
			// referenced directly.
			elementName = XsdCodegenUtils.getGlobalElementName( propertyType );
		}
		if (elementName == null) {
			elementName = getComplexObjectElementName( propertyType, isReferenceProperty );
		}
		if (elementName == null) {
			// Default handling for all element references that were not covered by the previous
			// conditions.
			elementName = XsdCodegenUtils.getGlobalElementName( propertyType );
		}
		
		// Assign the "Ref" suffix for element references
		if ((elementName != null) && isReferenceProperty) {
			elementName = new QName( elementName.getNamespaceURI(), elementName.getLocalPart() + "Ref" );
		}
		return elementName;
	}

	/**
	 * Returns the XML element name for a property of the given type.  This method presumes that the property
	 * type has already been determined to be a complex type with a managed name (or an alias of one).
	 * 
	 * @param propertyType  the property type for which to return an element name
	 * @param isReferenceProperty  flag indicating whether the property name returned should be for a reference property
	 * @return QName
	 */
	private static QName getComplexObjectElementName(NamedEntity propertyType, boolean isReferenceProperty) {
		QName elementName = null;
		
		if (!isReferenceProperty) {
			// If the property type is a non-simple core or business object, this method call
			// will return the QName of the substitution group element (or the substitutable summary
			// element).
			elementName = XsdCodegenUtils.getSubstitutionGroupElementName( propertyType );
			
		} else {
			if (propertyType instanceof TLAlias) {
				elementName = getAliasElementName( (TLAlias) propertyType );
				
			} else {
				TLFacet summaryFacet = null;
				
				if (propertyType instanceof TLCoreObject) {
					summaryFacet = ((TLCoreObject) propertyType).getSummaryFacet();
					
				} else if (propertyType instanceof TLBusinessObject) {
					summaryFacet = ((TLBusinessObject) propertyType).getSummaryFacet();
				}
				if (summaryFacet != null) {
					elementName = XsdCodegenUtils.getSubstitutableElementName( summaryFacet );
				}
			}
		}
		return elementName;
	}

	/**
	 * Returns the XML element name for the given alias.
	 * 
	 * @param alias  the alias for which to return an element name
	 * @return QName
	 */
	private static QName getAliasElementName(TLAlias alias) {
		TLAlias summaryAlias = null;
		QName elementName = null;
		
		if ((alias.getOwningEntity() instanceof TLCoreObject)
				|| (alias.getOwningEntity() instanceof TLBusinessObject)) {
			summaryAlias = AliasCodegenUtils.getFacetAlias( alias, TLFacetType.SUMMARY );
		}
		if (summaryAlias != null) {
			elementName = XsdCodegenUtils.getSubstitutableElementName( summaryAlias );
		}
		return elementName;
	}
	
	/**
	 * Performs a similar name calculation as the <code>getDefaultSchemaElementName()</code> with one exception. If the
	 * name of the schema element ends in "SubGrp", the suffix will be removed from the local part of the name that is
	 * returned by this method.
	 * 
	 * @param propertyType the assigned type of the model property
	 * @param isReferenceProperty indicates whether the type is assigned by value or reference
	 * @return QName
	 */
	public static QName getDefaultXmlElementName(NamedEntity propertyType, boolean isReferenceProperty) {
		QName elementName = getDefaultSchemaElementName( propertyType, isReferenceProperty );
		
		if ((elementName != null) && elementName.getLocalPart().endsWith( "SubGrp" )) {
			String localName = elementName.getLocalPart();
			elementName = new QName( elementName.getNamespaceURI(), localName.substring( 0, localName.length() - 6 ) );
		}
		return elementName;
	}
	
	/**
	 * Returns the list of attributes that were declared by the given VWA or inherited from other VWA's when assigned as
	 * the parent type of the given one. Attributes are guranteed to be in the correct order of their declaration in the
	 * sequencing of the inheritance hierarchy.
	 * 
	 * <p>
	 * NOTE: Attributes with duplicate names are NOT included in the results from this method
	 * 
	 * @param vwa the value-with-attributes for which to retrieve inherited attributes
	 * @return List<TLAttribute>
	 */
	public static List<TLAttribute> getInheritedAttributes(TLValueWithAttributes vwa) {
		return getInheritedAttributes( vwa, false );
	}
	
	/**
	 * Returns the list of attributes that were declared by the given VWA or inherited from other VWA's when assigned as
	 * the parent type of the given one. Attributes are guranteed to be in the correct order of their declaration in the
	 * sequencing of the inheritance hierarchy.
	 * 
	 * @param vwa the value-with-attributes for which to retrieve inherited attributes
	 * @param includeDuplicateNames flag indicating whether attributes with duplicate names should be included in the
	 *            results
	 * @return List<TLAttribute>
	 */
	public static List<TLAttribute> getInheritedAttributes(TLValueWithAttributes vwa, boolean includeDuplicateNames) {
		List<TLAttribute> attributeList = new ArrayList<>();
		
		findInheritedAttributes( vwa, includeDuplicateNames, attributeList, new HashSet<TLValueWithAttributes>() );
		return attributeList;
	}
	
	/**
	 * Recursive method that constructs the list of all VWA attributes, including those attributes inherited from other
	 * VWA's.
	 * 
	 * @param vwa the value-with-attributes for which to retrieve inherited attributes
	 * @param includeDuplicateNames flag indicating whether attributes with duplicate names should be included in the results
	 * @param attributeList the list of attributes being constructed
	 * @param visitedVWAs the collection of VWA's that have already been visited (used to prevent infinite loops)
	 */
	private static void findInheritedAttributes(TLValueWithAttributes vwa, boolean includeDuplicateNames,
			List<TLAttribute> attributeList, Collection<TLValueWithAttributes> visitedVWAs) {
		if (!visitedVWAs.contains( vwa )) {
			
			visitedVWAs.add( vwa );
			addDeclaredAttributes( vwa, includeDuplicateNames, attributeList );
			
			// Recurse into VWA attributes to find inherited items
			for (TLAttribute attribute : vwa.getAttributes()) {
				if (attribute.getType() instanceof TLValueWithAttributes) {
					findInheritedAttributes( (TLValueWithAttributes) attribute.getType(), includeDuplicateNames,
							attributeList, visitedVWAs );
				}
			}
			
			// Recurse into the parent type to find inherited items
			if (vwa.getParentType() instanceof TLValueWithAttributes) {
				findInheritedAttributes( (TLValueWithAttributes) vwa.getParentType(), includeDuplicateNames,
						attributeList, visitedVWAs );
			}
		}
	}

	/**
	 * Adds declared attributes from the given VWA to the list provided.
	 * 
	 * @param vwa  the VWA from which to add declared attributes
	 * @param includeDuplicateNames  flag indicating whether attributes with duplicate names should be included in the results
	 * @param attributeList  the list to which declared attributes will be added
	 */
	private static void addDeclaredAttributes(TLValueWithAttributes vwa, boolean includeDuplicateNames,
			List<TLAttribute> attributeList) {
		for (TLAttribute attribute : vwa.getAttributes()) {
			boolean canAdd = true;
			
			if (!includeDuplicateNames) {
				String attributeName = attribute.getName();
				
				for (TLAttribute existingAttribute : attributeList) {
					if ((attributeName != null) && attributeName.equals( existingAttribute.getName() )) {
						canAdd = false;
						break;
					}
				}
			}
			if (canAdd) {
				attributeList.add( attribute );
			}
		}
	}
	
	/**
	 * Returns the list of indicators that were declared by the given VWA or inherited from other VWA's when assigned as
	 * the parent type of the given one. Indicators are guranteed to be in the correct order of their declaration in the
	 * sequencing of the inheritance hierarchy.
	 * 
	 * @param vwa the value-with-attributes for which to retrieve inherited indicators
	 * @return List<TLIndicator>
	 */
	public static List<TLIndicator> getInheritedIndicators(TLValueWithAttributes vwa) {
		List<TLIndicator> indicatorList = new ArrayList<>();
		
		findInheritedIndicators( vwa, indicatorList, new HashSet<TLValueWithAttributes>() );
		return indicatorList;
	}
	
	/**
	 * Recursive method that constructs the list of all VWA indicators, including those indicators inherited from other
	 * VWA's.
	 * 
	 * @param vwa the value-with-attributes for which to retrieve inherited indicators
	 * @param indicatorList the list of indicators being constructed
	 * @param visitedVWAs the collection of VWA's that have already been visited (used to prevent infinite loops)
	 */
	private static void findInheritedIndicators(TLValueWithAttributes vwa, List<TLIndicator> indicatorList,
			Collection<TLValueWithAttributes> visitedVWAs) {
		if (!visitedVWAs.contains( vwa )) {
			
			visitedVWAs.add( vwa );
			indicatorList.addAll( vwa.getIndicators() );
			
			for (TLAttribute attribute : vwa.getAttributes()) {
				if (attribute.getType() instanceof TLValueWithAttributes) {
					findInheritedIndicators( (TLValueWithAttributes) attribute.getType(), indicatorList, visitedVWAs );
				}
			}
			if (vwa.getParentType() instanceof TLValueWithAttributes) {
				findInheritedIndicators( (TLValueWithAttributes) vwa.getParentType(), indicatorList, visitedVWAs );
			}
		}
	}
	
	/**
	 * Returns the list of attributes that were declared by the given facet or inherited from other facets, including
	 * higher-level facets from the same owner. Attributes are guranteed to be in the correct order of their declaration
	 * in the sequencing of the inheritance hierarchy.
	 * 
	 * @param facet the facet for which to retrieve inherited attributes
	 * @return List<TLAttribute>
	 */
	public static List<TLAttribute> getInheritedAttributes(TLFacet facet) {
		List<TLFacet> localFacetHierarchy = FacetCodegenUtils.getLocalFacetHierarchy( facet );
		List<TLAttribute> attributeList = new ArrayList<>();
		
		for (TLFacet aFacet : localFacetHierarchy) {
			attributeList.addAll( getInheritedFacetAttributes( aFacet ) );
		}
		return attributeList;
	}
	
	/**
	 * Returns the list of attributes that were declared by the given facet or inherited from facets of the same type.
	 * Attributes are guranteed to be in the correct order of their declaration in the sequencing of the inheritance
	 * hierarchy.
	 * 
	 * @param facet the facet for which to retrieve inherited attributes
	 * @return List<TLAttribute>
	 */
	public static List<TLAttribute> getInheritedFacetAttributes(TLFacet facet) {
		Collection<TLFacetOwner> visitedOwners = new HashSet<>();
		List<TLAttribute> attributeList = new ArrayList<>();
		TLFacetOwner facetOwner = facet.getOwningEntity();
		TLFacet aFacet = facet;
		
		while (facetOwner != null) {
			if (visitedOwners.contains( facetOwner )) {
				break;
			}
			String facetName = FacetCodegenUtils.getFacetName( facet );
			
			if (aFacet != null) {
				addDeclaredAttributes( aFacet, attributeList );
			}
			visitedOwners.add( facetOwner );
			facetOwner = FacetCodegenUtils.getFacetOwnerExtension( facetOwner );
			aFacet = (facetOwner == null) ? null
					: FacetCodegenUtils.getFacetOfType( facetOwner, facet.getFacetType(), facetName );
		}
		return attributeList;
	}

	/**
	 * Adds declared attributes from the give facet if they meet the required conditions.
	 * 
	 * @param facet  the facet from which to obtain declared attributes
	 * @param attributeList  the list to which the locally declared attributes will be added
	 */
	private static void addDeclaredAttributes(TLFacet facet, List<TLAttribute> attributeList) {
		Map<String,Set<NamedEntity>> inheritanceRoots = new HashMap<>();
		List<TLAttribute> localAttributes = new ArrayList<>( facet.getAttributes() );
		
		// We are traversing upward in the inheritance hierarchy, so we must pre-pend
		// attributes onto the list in the reverse order of their declarations in order
		// to preserve the intended order of occurrance.
		Collections.reverse( localAttributes );
		
		for (TLAttribute attribute : localAttributes) {
			TLPropertyType attrType = attribute.getType();
			NamedEntity inheritanceRoot = (attrType == null) ? null : getInheritanceRoot( attrType );
			Set<NamedEntity> attrInheritanceRoots = inheritanceRoots.get( attribute.getName() );
			
			if (attrInheritanceRoots == null) {
				attrInheritanceRoots = new HashSet<>();
				inheritanceRoots.put( attribute.getName(), attrInheritanceRoots );
			}
			
			// Properties whose types are members of an inheritance hierarchy of same-name
			// inherited properties should be skipped if they were eclipsed by lower-level
			// properties of the owner's hierarchy
			if ((inheritanceRoot == null) || !attrInheritanceRoots.contains( inheritanceRoot )) {
				if (inheritanceRoot != null) {
					attrInheritanceRoots.add( inheritanceRoot );
				}
				attributeList.add( 0, attribute );
			}
		}
	}
	
	/**
	 * Returns the list of indicators that were declared by the given facet or inherited from other facets, including
	 * higher-level facets from the same owner. Indicators are guranteed to be in the correct order of their declaration
	 * in the sequencing of the inheritance hierarchy.
	 * 
	 * @param facet the facet for which to retrieve inherited indicators
	 * @return List<TLIndicator>
	 */
	public static List<TLIndicator> getInheritedIndicators(TLFacet facet) {
		List<TLFacet> localFacetHierarchy = FacetCodegenUtils.getLocalFacetHierarchy( facet );
		List<TLIndicator> indicatorList = new ArrayList<>();
		
		for (TLFacet aFacet : localFacetHierarchy) {
			indicatorList.addAll( getInheritedFacetIndicators( aFacet ) );
		}
		return indicatorList;
	}
	
	/**
	 * Returns the list of indicators that were declared by the given facet or inherited from facets of the same type.
	 * Indicators are guranteed to be in the correct order of their declaration in the sequencing of the inheritance
	 * hierarchy.
	 * 
	 * @param facet the facet for which to retrieve inherited indicators
	 * @return List<TLIndicator>
	 */
	public static List<TLIndicator> getInheritedFacetIndicators(TLFacet facet) {
		Collection<TLFacetOwner> visitedOwners = new HashSet<>();
		List<TLIndicator> indicatorList = new ArrayList<>();
		TLFacetOwner facetOwner = facet.getOwningEntity();
		TLFacet aFacet = facet;
		
		while (facetOwner != null) {
			if (visitedOwners.contains( facetOwner )) {
				break;
			}
			String facetName = FacetCodegenUtils.getFacetName( facet );
			
			if (aFacet != null) {
				List<TLIndicator> localIndicators = new ArrayList<>( aFacet.getIndicators() );
				
				// We are traversing upward in the inheritance hierarchy, so we must pre-pend
				// indicators onto the list in the reverse order of their declarations in order
				// to preserve the intended order of occurrance.
				Collections.reverse( localIndicators );
				
				for (TLIndicator indicator : localIndicators) {
					indicatorList.add( 0, indicator );
				}
			}
			visitedOwners.add( facetOwner );
			facetOwner = FacetCodegenUtils.getFacetOwnerExtension( facetOwner );
			aFacet = (facetOwner == null) ? null
					: FacetCodegenUtils.getFacetOfType( facetOwner, facet.getFacetType(), facetName );
		}
		return indicatorList;
	}
	
	/**
	 * Returns the list of properties that were declared by the given facet or inherited from other facets, including
	 * higher-level facets from the same owner. Properties are guranteed to be in the correct order of their declaration
	 * in the sequencing of the inheritance hierarchy.
	 * 
	 * @param facet the facet for which to retrieve inherited properties
	 * @return List<TLProperty>
	 */
	public static List<TLProperty> getInheritedProperties(TLFacet facet) {
		List<TLFacet> localFacetHierarchy = FacetCodegenUtils.getLocalFacetHierarchy( facet );
		List<TLProperty> propertyList = new ArrayList<>();
		
		for (TLFacet aFacet : localFacetHierarchy) {
			propertyList.addAll( getInheritedFacetProperties( aFacet ) );
		}
		return propertyList;
	}
	
	/**
	 * Returns the list of properties that were declared by the given facet or inherited from facets of the same type.
	 * Properties are guranteed to be in the correct order of their declaration in the sequencing of the inheritance
	 * hierarchy.
	 * 
	 * @param facet the facet for which to retrieve inherited properties
	 * @return List<TLProperty>
	 */
	public static List<TLProperty> getInheritedFacetProperties(TLFacet facet) {
		Collection<TLFacetOwner> visitedOwners = new HashSet<>();
		List<TLProperty> propertyList = new ArrayList<>();
		TLFacetOwner facetOwner = facet.getOwningEntity();
		TLFacet aFacet = facet;
		
		while (facetOwner != null) {
			if (visitedOwners.contains( facetOwner )) {
				break;
			}
			String facetName = FacetCodegenUtils.getFacetName( facet );
			
			if (aFacet != null) {
				addLocalProperties( aFacet, propertyList );
			}
			visitedOwners.add( facetOwner );
			facetOwner = FacetCodegenUtils.getFacetOwnerExtension( facetOwner );
			aFacet = (facetOwner == null) ? null
					: FacetCodegenUtils.getFacetOfType( facetOwner, facet.getFacetType(), facetName );
		}
		return propertyList;
	}

	/**
	 * Adds the local properties from the given facet to the list of inherited
	 * properties being constructed.
	 * 
	 * @param facet  the facet from which to collect local properties
	 * @param propertyList  the list of inherited properties being constructed
	 */
	private static void addLocalProperties(TLFacet facet, List<TLProperty> propertyList) {
		Set<NamedEntity> complexInheritanceRoots = new HashSet<>();
		Map<String,Set<NamedEntity>> simpleInheritanceRoots = new HashMap<>();
		List<TLProperty> localProperties = new ArrayList<>( facet.getElements() );
		
		// We are traversing upward in the inheritance hierarchy, so we must pre-pend
		// properties onto the list in the reverse order of their declarations in order
		// to preserve the intended order of occurrance.
		Collections.reverse( localProperties );
		
		for (TLProperty property : localProperties) {
			if (property.isReference()) {
				propertyList.add( 0, property );
				
			} else {
				addLocalProperty( property, propertyList, complexInheritanceRoots, simpleInheritanceRoots );
			}
		}
	}

	/**
	 * Adds the given property to the list if it meets all of the criteria
	 * for being considered a local property that is not eclipsed by lower-level
	 * substitution group properties.
	 * 
	 * @param property  the property to be added (if it meets the required conditions)
	 * @param propertyList  the list of inherited properties being constructed
	 * @param complexInheritanceRoots  collection of complex substitution group properties discovered so far
	 * @param simpleInheritanceRoots  map of simple inheritance root properties discovered so far
	 */
	private static void addLocalProperty(TLProperty property, List<TLProperty> propertyList,
			Set<NamedEntity> complexInheritanceRoots, Map<String,Set<NamedEntity>> simpleInheritanceRoots) {
		TLPropertyType propertyType = resolvePropertyType( property.getType() );
		
		if (PropertyCodegenUtils.hasGlobalElement( propertyType )) {
			NamedEntity inheritanceRoot = getInheritanceRoot( propertyType );
			
			// Properties whose types are members of an inheritance hierarchy should be
			// skipped if they were eclipsed by lower-level properties of the owner's
			// hierarchy
			if ((inheritanceRoot == null) || !complexInheritanceRoots.contains( inheritanceRoot )) {
				if (inheritanceRoot != null) {
					complexInheritanceRoots.add( inheritanceRoot );
				}
				propertyList.add( 0, property );
			}
			
		} else if (propertyType != null) {
			NamedEntity inheritanceRoot = getInheritanceRoot( propertyType );
			Set<NamedEntity> propertyInheritanceRoots;
			
			simpleInheritanceRoots.computeIfAbsent(  property.getName(),
					n -> simpleInheritanceRoots.put( n, new HashSet<>() ) );
			propertyInheritanceRoots = simpleInheritanceRoots.get( property.getName() );
			
			// Properties whose types are members of an inheritance hierarchy of same-name
			// inherited properties should be skipped if they were eclipsed by lower-level
			// properties of the owner's hierarchy
			if ((inheritanceRoot == null) || !propertyInheritanceRoots.contains( inheritanceRoot )) {
				if (inheritanceRoot != null) {
					propertyInheritanceRoots.add( inheritanceRoot );
				}
				propertyList.add( 0, property );
			}
		}
	}
	
	/**
	 * Returns the list of <code>TLRole</code> entities that are defined and inherited by the given core object.
	 * 
	 * @param coreObject the core object for which to return roles
	 * @return List<TLRole>
	 */
	public static List<TLRole> getInheritedRoles(TLCoreObject coreObject) {
		List<TLCoreObject> coreObjects = new ArrayList<>();
		List<String> roleNames = new ArrayList<>();
		List<TLRole> roles = new ArrayList<>();
		TLFacetOwner core = coreObject;
		
		while (core != null) {
			if (core instanceof TLCoreObject) {
				coreObjects.add( 0, (TLCoreObject) core );
			}
			core = FacetCodegenUtils.getFacetOwnerExtension( core );
		}
		
		for (TLCoreObject c : coreObjects) {
			for (TLRole role : c.getRoleEnumeration().getRoles()) {
				if (!roleNames.contains( role.getName() )) {
					roles.add( role );
					roleNames.add( role.getName() );
				}
			}
		}
		return roles;
	}
	
	/**
	 * Analyzes the given property instance to determine the root of its inheritance hierarchy. This information is
	 * typically used to determine whether two properties from different levels of a containing entity's hierarchy
	 * should eclipse one another during code generation. If the given property type is not capable of being a member of
	 * an inheritance hierarchy (e.g. a simple type or VWA), this method will return null.
	 * 
	 * @param propertyType the property type to analyze
	 * @return NamedEntity
	 */
	public static NamedEntity getInheritanceRoot(TLPropertyType propertyType) {
		NamedEntity inheritanceRoot = null;
		
		if (isFacetPropertyType( propertyType )) {
			inheritanceRoot = getFacetTypeInheritanceRoot( propertyType );
			
		} else { // non-facet property types
			inheritanceRoot = getNonFacetTypeInheritanceRoot( propertyType );
		}
		return inheritanceRoot;
	}

	/**
	 * Returns the inheritance root fo the given property type.  This method presumes that the
	 * property type has already been determined to be a facet or facet alias.
	 * 
	 * @param propertyType  the property type for which to return the inheritance root
	 * @return NamedEntity
	 */
	private static NamedEntity getFacetTypeInheritanceRoot(TLPropertyType propertyType) {
		NamedEntity inheritanceRoot;
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
		TLFacetOwner parentEntity = FacetCodegenUtils.getFacetOwnerExtension( facet.getOwningEntity() );
		
		while (parentEntity != null) {
			String facetName = FacetCodegenUtils.getFacetName( facet );
			TLFacet baseQueryFacet = FacetCodegenUtils.getFacetOfType( parentEntity, facet.getFacetType(), facetName );
			
			if (baseQueryFacet != null) {
				inheritanceRoot = (alias == null) ? baseQueryFacet : baseQueryFacet.getAlias( alias.getName() );
			}
			parentEntity = FacetCodegenUtils.getFacetOwnerExtension( parentEntity );
		}
		return inheritanceRoot;
	}
	
	/**
	 * Returns the inheritance root fo the given property type.  This method presumes that the
	 * property type has already been determined NOT to be a facet or facet alias.
	 * 
	 * @param propertyType  the property type for which to return the inheritance root
	 * @return NamedEntity
	 */
	private static NamedEntity getNonFacetTypeInheritanceRoot(TLPropertyType propertyType) {
		NamedEntity inheritanceRoot;
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
			TLFacetOwner parentEntity = FacetCodegenUtils.getFacetOwnerExtension( facetOwner );
			
			while (parentEntity != null) {
				facetOwner = parentEntity;
				inheritanceRoot = (ownerAlias == null) ? facetOwner
						: ((TLAliasOwner) facetOwner).getAlias( ownerAlias.getName() );
				parentEntity = FacetCodegenUtils.getFacetOwnerExtension( parentEntity );
			}
			
		} else if (propertyType instanceof Versioned) {
			inheritanceRoot = getEarliestPriorVersion( (Versioned) propertyType );
		}
		return inheritanceRoot;
	}

	/**
	 * Returns the earliest minor version extension of the given property type.
	 * 
	 * @param propertyType  the property type for which to return the earliest minor version extension
	 * @return NamedEntity
	 */
	private static NamedEntity getEarliestPriorVersion(Versioned propertyType) {
		NamedEntity inheritanceRoot = null;
		try {
			MinorVersionHelper versionHelper = new MinorVersionHelper();
			Versioned currentVersion = propertyType;
			Versioned priorVersion = versionHelper.getVersionExtension( currentVersion );
			
			while (priorVersion != null) {
				currentVersion = priorVersion;
				priorVersion = versionHelper.getVersionExtension( currentVersion );
			}
			inheritanceRoot = currentVersion;
			
		} catch (VersionSchemeException e) {
			// Ignore and return null for the inheritance root
		}
		return inheritanceRoot;
	}

	/**
	 * Returns an ordered list of <code>TLProperty</code> and <code>TLIndicator</code> elements in the order they will
	 * occur in a schema-valid XML document. Only those indicators that are published as elements will be included in
	 * the resulting list.
	 * 
	 * @param facet the facet for which to return the sequence of XML elements
	 * @return List<TLModelElement>
	 */
	public static List<TLModelElement> getElementSequence(TLFacet facet) {
		List<TLFacet> localFacetHierarchy = FacetCodegenUtils.getLocalFacetHierarchy( facet );
		List<TLModelElement> elementList = new ArrayList<>();
		
		for (TLFacet aFacet : localFacetHierarchy) {
			elementList.addAll( getInheritedFacetProperties( aFacet ) );
			
			for (TLIndicator indicator : getInheritedFacetIndicators( aFacet )) {
				if (indicator.isPublishAsElement()) {
					elementList.add( indicator );
				}
			}
		}
		return elementList;
	}
	
	/**
	 * Returns true if the property type passed into this method is either a facet or an alias for a facet.
	 * 
	 * @param propertyType the property type to analyze
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
	 * Resolves the assigned property type into the actual property type that should be used for code generation. This
	 * is typically the same type, but it may differ if a list facet or a facet with no declared or inherited content is
	 * referenced.
	 * 
	 * @param assignedType the assigned type for the property
	 * @return TLPropertyType
	 */
	public static TLPropertyType resolvePropertyType(TLPropertyType assignedType) {
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
					default:
						break;
				}
			}
		}
		
		if (assignedType instanceof TLAbstractFacet) {
			// If the rendered property type resolves to a TLFacet, we need to make sure that
			// facet will be rendered in the XML schema output. If not, we need to find an
			// alternate facet from the same facet owner that will be rendered.
			resolvedType = findNonEmptyFacet( (TLAbstractFacet) assignedType );
			
		} else {
			resolvedType = assignedType;
		}
		return resolvedType;
	}
	
	/**
	 * If the given facet is considered "empty" (i.e. there are no generated elements), one of the non-empty sibling
	 * facets from its owner should be returned. The priority of which sibling is returned can be customized by
	 * sub-classes. By default, this method simply returns the 'referencedFacet' that is passed.
	 * 
	 * @param originatingFacet the originating facet that owns the property making the reference
	 * @param referencedFacet the facet being referenced (also the source facet used to lookup this delegate instance)
	 * @return TLAbstractFacet
	 */
	public static TLAbstractFacet findNonEmptyFacet(TLAbstractFacet referencedFacet) {
		FacetCodegenDelegateFactory factory = new FacetCodegenDelegateFactory( null );
		FacetCodegenDelegate<TLAbstractFacet> facetDelegate = factory.getDelegate( referencedFacet );
		TLAbstractFacet result = referencedFacet;
		
		if ((referencedFacet != null) && (facetDelegate != null) && !facetDelegate.hasContent()) {
			TLAbstractFacet[] alternateFacets = getAlternateFacets( referencedFacet );
			
			for (TLAbstractFacet alternateFacet : alternateFacets) {
				if (factory.getDelegate( alternateFacet ).hasContent()) {
					result = alternateFacet;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * When a referenced facet is considered "empty", this method will provide a prioritized list of the alternate
	 * sibling facets that should be considered for code generation.
	 * 
	 * @param referencedFacet the facet being referenced (also the source facet used to lookup this delegate instance)
	 * @return TLAbstractFacet[]
	 */
	protected static TLAbstractFacet[] getAlternateFacets(TLAbstractFacet referencedFacet) {
		TLAbstractFacet[] results = new TLAbstractFacet[0];
		
		if (referencedFacet instanceof TLListFacet) {
			results = getAlternateFacets( (TLCoreObject) referencedFacet.getOwningEntity(),
					(TLListFacet) referencedFacet );
			
		} else if (referencedFacet instanceof TLFacet) {
			results = getAlternateFacets( (TLFacet) referencedFacet, referencedFacet );
		}
		return results;
	}

	/**
	 * Returns alternate facets based upon the type of the owning entity.
	 * 
	 * @param facet  the facet for which to return valid alternatives
	 * @param referencedFacet  the referenced facet from the original call
	 * @return TLAbstractFacet[]
	 */
	protected static TLAbstractFacet[] getAlternateFacets(TLFacet facet, TLAbstractFacet referencedFacet) {
		TLAbstractFacet[] results = new TLAbstractFacet[0];
		
		if (facet.getOwningEntity() instanceof TLBusinessObject) {
			if (referencedFacet.getOwningEntity() instanceof TLBusinessObject) {
				results = getAlternateFacets( facet, (TLBusinessObject) referencedFacet.getOwningEntity(),
						referencedFacet );
				
			} else if (referencedFacet.getOwningEntity() instanceof TLCoreObject) {
				results = getAlternateFacets( facet, (TLCoreObject) referencedFacet.getOwningEntity(),
						referencedFacet );
			}
		} else if (facet.getOwningEntity() instanceof TLCoreObject) {
			if (referencedFacet.getOwningEntity() instanceof TLBusinessObject) {
				// Trivial Case: Core objects can only reference the ID facet
				// of a business object, so there are no alternates
				results = new TLAbstractFacet[0];
				
			} else if (referencedFacet.getOwningEntity() instanceof TLCoreObject) {
				results = getAlternateFacets( (TLCoreObject) referencedFacet.getOwningEntity(), referencedFacet );
			}
		} else if (facet.getOwningEntity() instanceof TLExtensionPointFacet) {
			if (referencedFacet.getOwningEntity() instanceof TLBusinessObject) {
				results = getAlternateFacets( (TLBusinessObject) referencedFacet.getOwningEntity(),
						referencedFacet );
				
			} else if (referencedFacet.getOwningEntity() instanceof TLCoreObject) {
				results = getAlternateFacets( (TLCoreObject) referencedFacet.getOwningEntity(), referencedFacet );
			}
		}
		return results;
	}
	
	/**
	 * Simple dispatch method used to decompose the problem from the primary 'getAlternateFacets()' method.
	 */
	protected static TLAbstractFacet[] getAlternateFacets(TLCoreObject referencedOwner, TLListFacet referencedFacet) {
		TLAbstractFacet[] results;
		
		// The summary-list facet is the alternate for the info-list, and vice-versa
		if (referencedFacet.getFacetType() == TLFacetType.SUMMARY) {
			results = new TLAbstractFacet[] { referencedOwner.getDetailListFacet() };
		} else { // the info-list facet is referenced
			results = new TLAbstractFacet[] { referencedOwner.getSummaryListFacet() };
		}
		return results;
	}
	
	/**
	 * Simple dispatch method used to decompose the problem from the primary 'getAlternateFacets()' method.
	 */
	protected static TLAbstractFacet[] getAlternateFacets(TLFacet originatingFacet, TLBusinessObject referencedOwner,
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
					default:
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
					default:
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
					default:
						break;
				}
				break;
			default:
				break;
		}
		return results;
	}
	
	/**
	 * Simple dispatch method used to decompose the problem from the primary 'getAlternateFacets()' method.
	 */
	protected static TLAbstractFacet[] getAlternateFacets(TLFacet originatingFacet, TLCoreObject referencedOwner,
			TLAbstractFacet referencedFacet) {
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
					default:
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
					default:
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
					default:
						break;
				}
				break;
			default:
				break;
		}
		return results;
	}
	
	/**
	 * Simple dispatch method used to decompose the problem from the primary 'getAlternateFacets()' method.
	 */
	protected static TLAbstractFacet[] getAlternateFacets(TLCoreObject referencedOwner, TLAbstractFacet referencedFacet) {
		TLAbstractFacet[] results = new TLAbstractFacet[0];
		
		switch (referencedFacet.getFacetType()) {
			case SIMPLE:
				results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(), referencedOwner.getDetailFacet() };
				break;
			case SUMMARY:
				results = new TLAbstractFacet[] { referencedOwner.getDetailFacet(), referencedOwner.getSimpleFacet() };
				break;
			case DETAIL:
				results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(), referencedOwner.getSimpleFacet() };
				break;
			default:
				break;
		}
		return results;
	}
	
	/**
	 * Simple dispatch method used to decompose the problem from the primary 'getAlternateFacets()' method.
	 */
	protected static TLAbstractFacet[] getAlternateFacets(TLBusinessObject referencedOwner,
			TLAbstractFacet referencedFacet) {
		TLAbstractFacet[] results = new TLAbstractFacet[0];
		
		switch (referencedFacet.getFacetType()) {
			case ID:
				// ID facets are required - no alternates required because it should never be
				// "empty"
				break;
			case SUMMARY:
				results = new TLAbstractFacet[] { referencedOwner.getDetailFacet(), referencedOwner.getIdFacet() };
				break;
			case DETAIL:
				results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(), referencedOwner.getIdFacet() };
				break;
			case CUSTOM:
				results = new TLAbstractFacet[] { referencedOwner.getSummaryFacet(), referencedOwner.getIdFacet() };
				break;
			default:
				break;
		}
		return results;
	}
	
	/**
	 * Returns the type of the attribute. In most cases, this is a simple call to 'attr.getType()'. In the case of VWA
	 * attribute types, however, we must search the VWA hierarchy to retrieve the simple base type.
	 * 
	 * @param attribute the attribute for which to return the type
	 * @return TLPropertyType
	 */
	public static TLPropertyType getAttributeType(TLAttribute attribute) {
		TLPropertyType attributeType = attribute.getType();
		
		while (attributeType instanceof TLValueWithAttributes) {
			attributeType = ((TLValueWithAttributes) attributeType).getParentType();
			
			if (attributeType == null) {
				attributeType = findEmptyStringType( attribute.getOwningModel() );
			}
		}
		return attributeType;
	}
	
	/**
	 * Scans the given model and returns the model entity used to represent the empty element type. If no such entity is
	 * defined, this method will return null.
	 * 
	 * @param model the model to search
	 * @return TLAttributeType
	 */
	public static TLAttributeType findEmptyStringType(TLModel model) {
		SchemaDependency emptySD = SchemaDependency.getEmptyElement();
		TLAttributeType emptyAttribute = null;
		
		for (AbstractLibrary library : model.getAllLibraries()) {
			if ((library.getNamespace() != null)
					&& library.getNamespace().equals( emptySD.getSchemaDeclaration().getNamespace() )) {
				NamedEntity member = library.getNamedMember( emptySD.getLocalName() );
				
				if (member instanceof TLAttributeType) {
					emptyAttribute = (TLAttributeType) member;
				}
			}
		}
		return emptyAttribute;
	}
	
	/**
	 * Returns true if the given named entity is a reference to the empty-string schema dependency.
	 * 
	 * @param entity the entity to analyze
	 * @return boolean
	 */
	public static boolean isEmptyStringType(NamedEntity entity) {
		QName emptyElementType = SchemaDependency.getEmptyElement().toQName();
		
		return emptyElementType.getNamespaceURI().equals( entity.getNamespace() )
				&& emptyElementType.getLocalPart().equals( entity.getLocalName() );
	}
	
	/**
	 * Identifies the 'maxOccurs' value for the generated element, typically this is defined by the 'repeat' attribute
	 * of the <code>TLPropertyElement</code>.
	 * 
	 * <p>
	 * Special Case: Properties that reference core object list facets as their type should assign the maxOccurs
	 * attribute to the number of roles in the core object.
	 * 
	 * @param source the model property being rendered
	 * @return String
	 */
	public static String getMaxOccurs(TLProperty source) {
		TLPropertyType facetType = source.getType();
		TLListFacet listFacet = null;
		String maxOccurs = null;
		
		// Check for special case with core object list facets
		if (facetType instanceof TLListFacet) {
			listFacet = (TLListFacet) facetType;
			
		} else if (facetType instanceof TLAlias) {
			TLAlias alias = (TLAlias) facetType;
			TLAliasOwner aliasOwner = alias.getOwningEntity();
			
			if (aliasOwner instanceof TLListFacet) {
				listFacet = (TLListFacet) aliasOwner;
			}
		}
		if (listFacet != null) {
			TLFacetOwner facetOwner = listFacet.getOwningEntity();
			
			if (facetOwner instanceof TLCoreObject) {
				TLCoreObject core = (TLCoreObject) facetOwner;
				
				if (!core.getRoleEnumeration().getRoles().isEmpty()) {
					maxOccurs = core.getRoleEnumeration().getRoles().size() + "";
				}
			}
			listFacet.getOwningEntity();
		}
		
		// Normal processing for maxOccurs if the special case was not present
		if (maxOccurs == null) {
			if ((source.getRepeat() < 0) || (source.getRepeat() > MAX_OCCURS_UNBOUNDED_THRESHOLD)) {
				maxOccurs = "unbounded";
				
			} else if (source.getRepeat() > 0) {
				maxOccurs = source.getRepeat() + "";
			}
		}
		return maxOccurs;
	}
	
}
