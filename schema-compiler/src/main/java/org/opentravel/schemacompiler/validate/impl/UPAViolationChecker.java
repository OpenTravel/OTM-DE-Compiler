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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLIndicatorOwner;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.model.TLPropertyType;

/**
 * Checks a facet or VWA for any elements (or indicators rendered as elements) with names that
 * will cause UPA violations in the resulting schemas.  Note that only violations related to
 * substitution groups will be reported by this checker since simple (non-substitution group)
 * UPA violations will be caught by the <code>DuplicateNameChecker</code>.
 * 
 * @author S. Livezey
 */
public class UPAViolationChecker {
	
	private static IdentityResolver<TLModelElement> fieldNameResolver = new FacetMemberIdentityResolver();
	private static FacetCodegenDelegateFactory facetDelegateFactory = new FacetCodegenDelegateFactory( null );
	
	private List<TLModelElement> upaViolationItems = new ArrayList<>();
	
	/**
	 * Constructor that initializes the checker based on the field members of the
	 * <code>TLAttributeOwner</code> provided.
	 * 
	 * @param fieldOwner  the owner of the field names to be analyzed
	 */
	public UPAViolationChecker(TLAttributeOwner fieldOwner) {
		this( (TLModelElement) fieldOwner );
	}
	
	/**
	 * Constructor that initializes the checker based on the field members of the
	 * <code>TLPropertyOwner</code> provided.
	 * 
	 * @param fieldOwner  the owner of the field names to be analyzed
	 */
	public UPAViolationChecker(TLPropertyOwner fieldOwner) {
		this( (TLModelElement) fieldOwner );
	}
	
	/**
	 * Constructor that initializes the checker based on the field members of the
	 * <code>TLIndicatorOwner</code> provided.
	 * 
	 * @param fieldOwner  the owner of the field names to be analyzed
	 */
	public UPAViolationChecker(TLIndicatorOwner fieldOwner) {
		this( (TLModelElement) fieldOwner );
	}
	
	/**
	 * Private constructor that calls the correct initialization method based on
	 * the entity type of the given field owner.
	 * 
	 * @param fieldOwner  the owner of the field names to be analyzed
	 */
	private UPAViolationChecker(TLModelElement fieldOwner) {
		List<TLModelElement> elementSequence = getElementSequence( fieldOwner );
		
		if (elementSequence.size() <= 1) {
			return;
		}
		
		for (int checkIdx = 1; checkIdx < elementSequence.size(); checkIdx++) {
			TLModelElement checkElement = elementSequence.get( checkIdx );
			boolean hasSubstitutionGroup = (checkElement instanceof TLProperty)
					&& isSubstitutionGroupElement( (TLProperty) checkElement );
			Set<QName> checkElementNames = new HashSet<>();
			Set<QName> precedingNames = new HashSet<>();
			
			// Identify the possible names for the check element
			if (checkElement instanceof TLIndicator) {
				checkElementNames.addAll( getPossibleElementNames( (TLIndicator) checkElement ) );
				
			} else if (checkElement instanceof TLProperty) {
				checkElementNames.addAll( getPossibleElementNames( (TLProperty) checkElement ) );
			}
			
			// Collect a list of possible preceding names by starting with the previous
			// element and working our way back towards the beginning of the list.
			hasSubstitutionGroup = hasPrecedingElementConflict( elementSequence, precedingNames, checkIdx,
					hasSubstitutionGroup );
			
			// Only report errors if we have encountered at least one substitution group
			// element during this check; non-substition errors will be reported as duplicate
			// names rather than UPA violations.
			if (hasSubstitutionGroup) {
				// If there is any overlap between the check element name(s) and the possible
				// preceding names, then we have a UPA violation to report.
				for (QName checkName : checkElementNames) {
					if (precedingNames.contains( checkName )) {
						upaViolationItems.add( checkElement );
						break;
					}
				}
			}
		}
	}

	/**
	 * Returns the element sequence to be checked for UPA violations for the given field owner.
	 * 
	 * @param fieldOwner  the field owner for which to return the element sequence
	 * @return List<TLModelElement>
	 */
	private List<TLModelElement> getElementSequence(TLModelElement fieldOwner) {
		List<TLModelElement> elementSequence;
		if (fieldOwner == null) {
			throw new IllegalArgumentException("The field owner entity cannot be null.");
		}
		
		if (fieldOwner instanceof TLFacet) {
			elementSequence = PropertyCodegenUtils.getElementSequence( (TLFacet) fieldOwner );
			
		} else if (fieldOwner instanceof TLExtensionPointFacet) {
			elementSequence = ValidatorUtils.getMembers( (TLExtensionPointFacet) fieldOwner );
			
		} else { // instanceof TLValueWithAttributes
			elementSequence = new ArrayList<>(); // no elements, so no possible UPA violations
		}
		return elementSequence;
	}

	/**
	 * Returns true if the current element under inspection has a conflict with the preceding one.
	 * 
	 * @param elementSequence  the overall sequence of elements being checked
	 * @param precedingNames  the list of preceding names of the current element
	 * @param checkIdx  the current index in the overall element sequence that is being checked for UPA errors
	 * @param hasSubstitutionGroup  indicates if the current element defines a substitution group
	 * @return boolean
	 */
	private boolean hasPrecedingElementConflict(List<TLModelElement> elementSequence, Set<QName> precedingNames,
			int checkIdx, boolean hasSubstitutionGroup) {
		for (int i = checkIdx - 1; i >= 0; i--) {
			TLModelElement precedingElement = elementSequence.get( i );
			
			if (precedingElement instanceof TLIndicator) {
				precedingNames.addAll( getPossibleElementNames( (TLIndicator) precedingElement ) );
				
			} else if (precedingElement instanceof TLProperty) {
				TLProperty element = (TLProperty) precedingElement;
				
				precedingNames.addAll( getPossibleElementNames( element ) );
				hasSubstitutionGroup = isSubstitutionGroupElement( element );
				
				// If this element is mandatory, we can stop searching since prededing
				// elements cannot create a UPA violation
				//
				// NOTE: Indicators are always optional, so we will never break out of the
				//       loop unless we encounter a required TLProperty
				if (element.isMandatory()) {
					break;
				}
			}
		}
		return hasSubstitutionGroup;
	}
	
	/**
	 * Returns true if the name of the given indicator causes a UPA violation.
	 * 
	 * @param indicator  the indicator to check
	 * @return boolean
	 */
	public boolean isUPAViolation(TLIndicator indicator) {
		return isUPAViolation( (TLModelElement) indicator );
	}
	
	/**
	 * Returns true if the name of the given indicator causes a UPA violation.
	 * 
	 * @param element  the element to check
	 * @return boolean
	 */
	public boolean isUPAViolation(TLProperty element) {
		return isUPAViolation( (TLModelElement) element );
	}
	
	/**
	 * Returns true if the name of the given indicator or element causes a UPA
	 * violation.
	 * 
	 * @param indicator  the indicator to check
	 * @return boolean
	 */
	private boolean isUPAViolation(TLModelElement element) {
		boolean result = false;
		
		for (TLModelElement upaItem : upaViolationItems) {
			if (element == upaItem) { // Avoid checking with .equals() method
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * Returns the list of all possible element names for the given <code>TLIndicator</code>.
	 * 
	 * @param indicator  the indicator for which to return the collection of names
	 * @return Set<QName>
	 */
	private Set<QName> getPossibleElementNames(TLIndicator indicator) {
		Set<QName> elementNames = new HashSet<>();
		
		if ((indicator != null) && (indicator.getOwningLibrary() != null)) {
			elementNames.add( new QName( indicator.getOwningLibrary().getNamespace(),
					fieldNameResolver.getIdentity( indicator ) ) );
		}
		return elementNames;
	}
	
	/**
	 * Returns the list of all possible element names for the given <code>TLProperty</code>.
	 * 
	 * @param element  the element for which to return the collection of names
	 * @return Set<QName>
	 */
	private Set<QName> getPossibleElementNames(TLProperty element) {
		Set<QName> elementNames = new HashSet<>();
		
		if ((element != null) && (element.getOwningLibrary() != null)) {
			if (isSubstitutionGroupElement( element )) {
				addSubstitutionGroupNames( element, elementNames );
				
			} else {
				String fieldName = fieldNameResolver.getIdentity( element );
				
				if (fieldName != null) {
					elementNames.add( new QName(
							element.getOwningLibrary().getNamespace(), fieldName ) );
				}
			}
		}
		return elementNames;
	}

	/**
	 * Adds element names for the given element's substitution group.
	 * 
	 * @param element  the element for which to return substitution group names
	 * @param elementNames  the set of element names being populated
	 */
	private void addSubstitutionGroupNames(TLProperty element, Set<QName> elementNames) {
		TLPropertyType elementType = element.getType();
		TLAlias ownerAlias = null;
		
		if (elementType instanceof TLAlias) {
			ownerAlias = (TLAlias) elementType;
			elementType = (TLPropertyType) ownerAlias.getOwningEntity();
		}
		
		if (elementType instanceof TLCoreObject) {
			TLCoreObject core = (TLCoreObject) elementType;
			
			addElementName( core.getSummaryFacet(), ownerAlias, elementNames );
			addElementName( core.getDetailFacet(), ownerAlias, elementNames );
			
		} else { // instanceof TLBusinessObject
			TLBusinessObject bo = (TLBusinessObject) elementType;
			
			addElementName( bo.getIdFacet(), ownerAlias, elementNames );
			addElementName( bo.getSummaryFacet(), ownerAlias, elementNames );
			addElementName( bo.getDetailFacet(), ownerAlias, elementNames );
			
			for (TLFacet customFacet : bo.getCustomFacets()) {
				addElementName( customFacet, ownerAlias, elementNames );
			}
		}
	}
	
	/**
	 * If the given facet contains (or inherits) any content its name will be added to the list
	 * provided.  If an owner alias is specified, the element name for the corresponding facet alias
	 * will be added.
	 * 
	 * @param facet  the facet for which to return a name
	 * @param ownerAlias  the owner alias (optional) that specifies which facet alias to return
	 * @param elementNames  the collection of names to which the facet name should be added
	 */
	private void addElementName(TLFacet facet, TLAlias ownerAlias, Set<QName> elementNames) {
		QName elementName = null;
		
		if (facetDelegateFactory.getDelegate( facet ).hasContent()) {
			if (ownerAlias != null) {
				TLAlias facetAlias = AliasCodegenUtils.getFacetAlias( ownerAlias, facet.getFacetType() );
				
				if (facetAlias != null) {
					elementName = XsdCodegenUtils.getSubstitutableElementName( facetAlias );
				}
				
			} else {
				elementName = XsdCodegenUtils.getSubstitutableElementName( facet );
			}
		}
		
		if (elementName != null) {
			elementNames.add( elementName );
		}
	}
	
	/**
	 * Returns true if the type of the given property represents the head of a substitution
	 * group (i.e. business objects, cores, and their aliases).
	 * 
	 * @param element  the element to analyze
	 * @return boolean
	 */
	private boolean isSubstitutionGroupElement(TLProperty element) {
		TLPropertyType fieldType = (element == null) ? null : element.getType();
		boolean result = false;
		
		if (fieldType instanceof TLAlias) {
			fieldType = (TLPropertyType) ((TLAlias) fieldType).getOwningEntity();
		}
		if ((fieldType instanceof TLCoreObject) || (fieldType instanceof TLBusinessObject)) {
			result = true;
		}
		return result;
	}
	
}
