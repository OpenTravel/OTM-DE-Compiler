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

import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
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
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Checks a facet or VWA for any attributes, elements, or indicators with duplicate names. Note that these checks DO NOT
 * include elements that are members of a substitution group since those items will be validated by the
 * <code>UPAViolationChecker</code>.
 * 
 * @author S. Livezey
 */
public class DuplicateFieldChecker {

    private static IdentityResolver<TLModelElement> fieldNameResolver = new FacetMemberIdentityResolver();

    private Set<String> duplicateFieldNames = new HashSet<>();

    /**
     * Constructor that initializes the checker based on the field members of the <code>TLAttributeOwner</code>
     * provided.
     * 
     * @param fieldOwner the owner of the field names to be analyzed
     */
    public DuplicateFieldChecker(TLAttributeOwner fieldOwner) {
        this( (TLModelElement) fieldOwner );
    }

    /**
     * Constructor that initializes the checker based on the field members of the <code>TLPropertyOwner</code> provided.
     * 
     * @param fieldOwner the owner of the field names to be analyzed
     */
    public DuplicateFieldChecker(TLPropertyOwner fieldOwner) {
        this( (TLModelElement) fieldOwner );
    }

    /**
     * Constructor that initializes the checker based on the field members of the <code>TLIndicatorOwner</code>
     * provided.
     * 
     * @param fieldOwner the owner of the field names to be analyzed
     */
    public DuplicateFieldChecker(TLIndicatorOwner fieldOwner) {
        this( (TLModelElement) fieldOwner );
    }

    /**
     * Private constructor that calls the correct initialization method based on the entity type of the given field
     * owner.
     * 
     * @param fieldOwner the owner of the field names to be analyzed
     */
    private DuplicateFieldChecker(TLModelElement fieldOwner) {
        Map<String,Integer> fieldCounts = new HashMap<>();
        List<TLModelElement> memberFields;

        if (fieldOwner == null) {
            throw new IllegalArgumentException( "The field owner entity cannot be null." );
        }

        if (fieldOwner instanceof TLFacet) {
            memberFields = ValidatorUtils.getInheritedMembers( (TLFacet) fieldOwner );

        } else if (fieldOwner instanceof TLValueWithAttributes) {
            memberFields = ValidatorUtils.getInheritedMembers( (TLValueWithAttributes) fieldOwner );

        } else {
            memberFields = ValidatorUtils.getMembers( (TLExtensionPointFacet) fieldOwner );
        }

        countFieldNameOccurrances( memberFields, fieldCounts );

        // Populate the list of duplicate names based on the occurrance count of each field name
        for (Entry<String,Integer> entry : fieldCounts.entrySet()) {
            String fieldName = entry.getKey();

            if (entry.getValue() > 1) {
                duplicateFieldNames.add( fieldName );
            }
        }
    }

    /**
     * Collect the number of occurrances of each field name.
     * 
     * @param memberFields the list of member fields to analyze
     * @param fieldCounts the map associating each field name with the total number of occurrances
     */
    private void countFieldNameOccurrances(List<TLModelElement> memberFields, Map<String,Integer> fieldCounts) {
        for (TLModelElement field : memberFields) {
            String fieldName;

            if (field instanceof TLProperty) {
                // Skip all elements that represent the root of a substitution group (business objects,
                // cores, and their aliases). These will be analyzed by the UPAViolationChecker.
                if (isSubstitutionGroupElement( (TLProperty) field )) {
                    continue;
                }
                fieldName = fieldNameResolver.getIdentity( field );

            } else {
                fieldName = fieldNameResolver.getIdentity( field );
            }

            // Update the occurrance count for this field name
            if (fieldName != null) {
                if (fieldCounts.containsKey( fieldName )) {
                    fieldCounts.put( fieldName, fieldCounts.get( fieldName ) + 1 );

                } else {
                    fieldCounts.put( fieldName, 1 );
                }
            }
        }
    }

    /**
     * Returns true if the name of the given attribute is a duplicate within its owning facet or VWA.
     * 
     * @param attribute the attribute to check
     * @return boolean
     */
    public boolean isDuplicateName(TLAttribute attribute) {
        return duplicateFieldNames.contains( fieldNameResolver.getIdentity( attribute ) );
    }

    /**
     * Returns true if the name of the given element is a duplicate within its owning facet.
     * 
     * @param element the element to check
     * @return boolean
     */
    public boolean isDuplicateName(TLProperty element) {
        boolean result = false;

        if (!isSubstitutionGroupElement( element )) {
            result = duplicateFieldNames.contains( fieldNameResolver.getIdentity( element ) );
        }
        return result;
    }

    /**
     * Returns true if the name of the given indicator is a duplicate within its owning facet or VWA.
     * 
     * @param indicator the indicator to check
     * @return boolean
     */
    public boolean isDuplicateName(TLIndicator indicator) {
        return duplicateFieldNames.contains( fieldNameResolver.getIdentity( indicator ) );
    }

    /**
     * Returns true if the type of the given property represents the head of a substitution group (i.e. business
     * objects, cores, and their aliases).
     * 
     * @param element the element to analyze
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
