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
package org.opentravel.schemacompiler.validate.compile;

import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationBuilder;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLPropertyBaseValidator;
import org.opentravel.schemacompiler.validate.impl.CircularReferenceChecker;
import org.opentravel.schemacompiler.validate.impl.DuplicateFieldChecker;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.UPAViolationChecker;
import org.opentravel.schemacompiler.validate.impl.ValidatorUtils;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.Versioned;

/**
 * Validator for the <code>TLProperty</code> class.
 * 
 * @author S. Livezey
 */
public class TLPropertyCompileValidator extends TLPropertyBaseValidator {

    public static final String ERROR_ILLEGAL_LIST_FACET_REFERENCE = "ILLEGAL_LIST_FACET_REFERENCE";
    public static final String WARNING_LIST_FACET_REPEAT_IGNORED = "LIST_FACET_REPEAT_IGNORED";
    public static final String ERROR_EMPTY_FACET_REFERENCED = "EMPTY_FACET_REFERENCED";
    public static final String ERROR_ELEMENT_REF_NAME_MISMATCH = "ELEMENT_REF_NAME_MISMATCH";
    public static final String ERROR_UPA_VIOLATION = "UPA_VIOLATION";
    public static final String ERROR_ILLEGAL_REFERENCE = "ILLEGAL_REFERENCE";
    public static final String ERROR_ILLEGAL_CIRCULAR_REFERENCE = "ILLEGAL_CIRCULAR_REFERENCE";
    public static final String ERROR_ILLEGAL_REQUIRED_ELEMENT = "ILLEGAL_REQUIRED_ELEMENT";
    public static final String WARNING_BOOLEAN_TYPE_REFERENCE = "BOOLEAN_TYPE_REFERENCE";
    public static final String WARNING_UNNECESSARY_EXAMPLE = "UNNECESSARY_EXAMPLE";
    public static final String WARNING_LEGACY_IDREF = "LEGACY_IDREF";
    public static final String WARNING_INVALID_REFERENCE_NAME = "INVALID_REFERENCE_NAME";
    public static final String WARNING_ILLEGAL_BUSINESS_OBJECT_ID = "ILLEGAL_BUSINESS_OBJECT_ID";
    public static final String WARNING_ILLEGAL_CORE_OBJECT_ID = "ILLEGAL_CORE_OBJECT_ID";
    public static final String WARNING_ILLEGAL_CHOICE_OBJECT_ID = "ILLEGAL_CHOICE_OBJECT_ID";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLProperty target) {
        TLValidationBuilder builder = newValidationBuilder(target);
        TLPropertyType propertyType = target.getType();

        builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
                .assertPatternMatch(NAME_XML_PATTERN);

        builder.setEntityReferenceProperty("type", propertyType, target.getTypeName())
                .setFindingType(FindingType.ERROR)
                .assertNotNull()
                .assertValidEntityReference(TLSimple.class, TLSimpleFacet.class, TLClosedEnumeration.class, 
                        TLOpenEnumeration.class, TLRoleEnumeration.class, TLValueWithAttributes.class,
                        TLFacet.class, TLListFacet.class, TLCoreObject.class, TLChoiceObject.class,
                        TLBusinessObject.class, TLAlias.class, TLRoleEnumeration.class, XSDSimpleType.class,
                        XSDComplexType.class, XSDElement.class)
                .setFindingType(FindingType.WARNING).assertNotDeprecated().assertNotObsolete();

		validateIDFieldLocation(target, builder);
		
		if ((propertyType != null) && target.isReference() && !hasID(propertyType)) {
			builder.addFinding(FindingType.ERROR, "type", ERROR_ILLEGAL_REFERENCE, target.getTypeName());
		}
		
		checkEmptyValueType(target, target.getType(), "type", builder);
		
		if (CircularReferenceChecker.hasCircularReference(target)) {
			builder.addFinding(FindingType.ERROR, "type", ERROR_ILLEGAL_CIRCULAR_REFERENCE, target.getTypeName());
		}
		
		builder.setProperty("equivalents", target.getEquivalents()).setFindingType(FindingType.ERROR).assertNotNull()
			.assertContainsNoNullElements();
		
		// Check for duplicate names of this element
		if (target.getName() != null) {
			DuplicateFieldChecker dupChecker = getDuplicateFieldChecker(target);
			
			if (dupChecker.isDuplicateName(target)) {
				builder.addFinding(FindingType.ERROR, "name", ValidationBuilder.ERROR_DUPLICATE_ELEMENT,
						target.getName());
			}
		}
		
		// Check for UPA violations
		if (target.getName() != null) {
			UPAViolationChecker upaChecker = getUPAViolationChecker(target);
			
			if (upaChecker.isUPAViolation(target)) {
				builder.addFinding(FindingType.ERROR, "name", ERROR_UPA_VIOLATION, target.getName());
			}
		}
		
		validateMinorVersionProperty(target, builder, propertyType);
		
		// A warning will be issued for boolean properties (should be indicators)
		if (ValidatorUtils.isBooleanType(propertyType)) {
			builder.addFinding(FindingType.WARNING, "type", WARNING_BOOLEAN_TYPE_REFERENCE);
		}
		
		validatePropertyName(target, builder, propertyType);
		validateEmptyFacetReference(propertyType, builder);
		
		// Warn if a deprecated XSD date/time type is being referenced
		TLPropertyOwner elementOwner = target.getOwner();
		AbstractLibrary owningLibrary = (elementOwner == null) ? null : elementOwner.getOwningLibrary();
		
		validateDeprecatedDateTimeUsage(propertyType, owningLibrary, builder);
		validateListFacetProperty(target, builder, propertyType);
		
		// Issue a warning if one or more examples are provided for a complex
		// property type
		if (!(propertyType instanceof TLAttributeType) && target.getExamples().isEmpty()) {
			builder.addFinding(FindingType.WARNING, "examples", WARNING_UNNECESSARY_EXAMPLE, target.getTypeName());
		}
		
		return builder.getFindings();
    }

	/**
	 * Issue a warning if an empty facet is referenced.
	 * 
	 * @param propertyType  the resolved type of the property being validated
	 * @param builder  the validation builder where errors/warnings should be reported
	 */
	private void validateEmptyFacetReference(TLPropertyType propertyType, TLValidationBuilder builder) {
		if (propertyType instanceof TLAbstractFacet) {
			TLAbstractFacet referencedFacet = (TLAbstractFacet) propertyType;
			FacetCodegenDelegate<TLAbstractFacet> facetDelegate = new FacetCodegenDelegateFactory(null)
				.getDelegate(referencedFacet);
			boolean hasContent = (facetDelegate == null) ? referencedFacet.declaresContent()
					: facetDelegate.hasContent();
			
			if (!hasContent) {
				builder.addFinding(FindingType.WARNING, "type", ERROR_EMPTY_FACET_REFERENCED,
						referencedFacet.getFacetType().getIdentityName(),
						referencedFacet.getOwningEntity().getLocalName());
			}
		}
	}

	/**
	 * List facets can only be referenced if the core object defines one or more roles.
	 * 
	 * @param target  the target property being validated
	 * @param builder  the validation builder where errors/warnings should be reported
	 * @param propertyType  the resolved type of the property being validated
	 */
	private void validateListFacetProperty(TLProperty target, TLValidationBuilder builder,
			TLPropertyType propertyType) {
		boolean isListFacet = (propertyType instanceof TLListFacet) || ((propertyType instanceof TLAlias)
				&& (((TLAlias) propertyType).getOwningEntity() instanceof TLListFacet));
		
		if (isListFacet) {
			TLListFacet listFacet = (propertyType instanceof TLListFacet) ? (TLListFacet) propertyType
					: (TLListFacet) ((TLAlias) propertyType).getOwningEntity();
			TLCoreObject referencedCore = (TLCoreObject) listFacet.getOwningEntity();
			int roleCount = referencedCore.getRoleEnumeration().getRoles().size();
			
			if ((roleCount == 0) && !(listFacet.getItemFacet() instanceof TLSimpleFacet)) {
				builder.addFinding(FindingType.ERROR, "type", ERROR_ILLEGAL_LIST_FACET_REFERENCE,
						referencedCore.getName());
			}
			if ((target.getRepeat() != 0) && (target.getRepeat() != roleCount)) {
				String repeatValue = (target.getRepeat() < 0) ? "*" : (target.getRepeat() + "");
				builder.addFinding(FindingType.WARNING, "repeat", WARNING_LIST_FACET_REPEAT_IGNORED, repeatValue);
			}
		}
	}

	/**
	 * Issue a warning for properties that reference complex types where the property name
	 * and the referenced type's name do not match (the only exception occurs when the referenced
	 * property belongs to a built-in library since it cannot be edited to add aliases).
	 * 
	 * @param target  the target property being validated
	 * @param builder  the validation builder where errors/warnings should be reported
	 * @param propertyType  the resolved type of the property being validated
	 */
	private void validatePropertyName(TLProperty target, TLValidationBuilder builder, TLPropertyType propertyType) {
		if (propertyType != null) {
			TLPropertyType resolvedPropertyType = (target.getOwner() == null) ? propertyType
					: PropertyCodegenUtils.resolvePropertyType(propertyType);
			
			if (PropertyCodegenUtils.hasGlobalElement(resolvedPropertyType)) {
				validateManagedPropertyName(target, builder, resolvedPropertyType);
				
			} else {
				if (target.isReference() && (target.getName() != null) && !target.getName().endsWith("Ref")) {
					builder.addFinding(FindingType.WARNING, "name", WARNING_INVALID_REFERENCE_NAME,
							target.getName() + "Ref");
				}
			}
		}
	}

	/**
	 * Issue a warning for properties that reference complex types where the property name
	 * and the resolved type's name do not match.
	 * 
	 * @param target  the target property being validated
	 * @param builder  the validation builder where errors/warnings should be reported
	 * @param resolvedPropertyType  the resolved type of the property being validated
	 */
	private void validateManagedPropertyName(TLProperty target, TLValidationBuilder builder,
			TLPropertyType resolvedPropertyType) {
		QName referencedQName = PropertyCodegenUtils.getDefaultXmlElementName(resolvedPropertyType,
				target.isReference());
		String referencedLocalName = (referencedQName == null) ? null : referencedQName.getLocalPart();
		String propertyLocalName = target.getName();
		boolean referencedNameMatch = (propertyLocalName != null) && !propertyLocalName.equals(referencedLocalName);
		
		if (referencedNameMatch) {
			if (!target.isReference()) {
				builder.addFinding(FindingType.WARNING, "name", ERROR_ELEMENT_REF_NAME_MISMATCH,
						referencedLocalName);
			} else {
				builder.addFinding(FindingType.WARNING, "name", WARNING_INVALID_REFERENCE_NAME,
						referencedLocalName);
			}
		}
	}

	/**
	 * Verify that properties of minor version extension are optional (with one exception - see
	 * inline comments).
	 * 
	 * @param target  the target property being validated
	 * @param builder  the validation builder where errors/warnings should be reported
	 * @param propertyType  the resolved type of the property being validated
	 */
	private void validateMinorVersionProperty(TLProperty target, TLValidationBuilder builder,
			TLPropertyType propertyType) {
		if (target.isMandatory() && isVersionExtension(getVersionedOwner(target))) {
			TLProperty eclipsedProperty = getEclipsedProperty(target);
			boolean isSpecialCase = false;
			
			// Mandatory elements are allowed on minor version extensions if the
			// only change was to a later version of the element's type.
			if ((eclipsedProperty != null) && (eclipsedProperty.getType() instanceof Versioned)) {
				try {
					Versioned priorVersionType = (Versioned) eclipsedProperty.getType();
					List<Versioned> laterMinorVersions = new MinorVersionHelper()
						.getLaterMinorVersions(priorVersionType);
					
					isSpecialCase = (propertyType instanceof Versioned)
							&& laterMinorVersions.contains((Versioned) propertyType);
					
				} catch (VersionSchemeException e) {
					// No error - assume not a special case and move on
				}
			}
			
			if (!isSpecialCase) {
				builder.addFinding(FindingType.ERROR, "mandatory", ERROR_ILLEGAL_REQUIRED_ELEMENT);
			}
		}
	}

	/**
	 * For xsd:ID elements, make sure they are contained in the top-level facet if the
	 * owner is a core or business object.
	 * 
	 * @param target  the target property being validated
	 * @param builder  the validation builder where errors/warnings should be reported
	 */
	private void validateIDFieldLocation(TLProperty target, TLValidationBuilder builder) {
		if (ValidatorUtils.isXsdID(target.getType()) && (target.getOwner() instanceof TLFacet)) {
			TLFacet facet = (TLFacet) target.getOwner();
			TLFacetOwner facetOwner = facet.getOwningEntity();
			
			if (facetOwner instanceof TLBusinessObject) {
				if ((facet.getFacetType() != TLFacetType.ID) && (facet.getFacetType() != TLFacetType.QUERY)) {
					builder.addFinding(FindingType.WARNING, "type", WARNING_ILLEGAL_BUSINESS_OBJECT_ID);
				}
			} else if (facetOwner instanceof TLCoreObject) {
				if (facet.getFacetType() != TLFacetType.SUMMARY) {
					builder.addFinding(FindingType.WARNING, "type", WARNING_ILLEGAL_CORE_OBJECT_ID);
				}
			} else if ((facetOwner instanceof TLChoiceObject) && (facet.getFacetType() != TLFacetType.SHARED)) {
				builder.addFinding(FindingType.WARNING, "type", WARNING_ILLEGAL_CHOICE_OBJECT_ID);
			}
		}
	}

    /**
     * Returns a <code>DuplicateFieldChecker</code> that can be used to identify duplicate
     * field names within the elements of the declaring facet.
     * 
     * @param target  the target property being validated
     * @return DuplicateFieldChecker
     */
    private DuplicateFieldChecker getDuplicateFieldChecker(TLProperty target) {
        TLPropertyOwner propertyOwner = target.getOwner();
        String cacheKey = propertyOwner.getNamespace() + ":" + propertyOwner.getLocalName() + ":dupChecker";
        DuplicateFieldChecker checker = (DuplicateFieldChecker) getContextCacheEntry( cacheKey );

        if (checker == null) {
        	checker = new DuplicateFieldChecker( propertyOwner );
            setContextCacheEntry( cacheKey, checker );
        }
        return checker;
    }

    /**
     * Returns a <code>UPAViolationChecker</code> that can be used to identify UPA violations that occur
     * with preceding elements of the declaring facet.
     * 
     * @param target  the target property being validated
     * @return UPAViolationChecker
     */
    private UPAViolationChecker getUPAViolationChecker(TLProperty target) {
        TLPropertyOwner propertyOwner = target.getOwner();
        String cacheKey = propertyOwner.getNamespace() + ":" + propertyOwner.getLocalName() + ":upaChecker";
        UPAViolationChecker checker = (UPAViolationChecker) getContextCacheEntry( cacheKey );

        if (checker == null) {
        	checker = new UPAViolationChecker( propertyOwner );
            setContextCacheEntry( cacheKey, checker );
        }
        return checker;
    }

    /**
     * Returns true if the given named entity publishes at least one ID value as an attribute or
     * element.
     * 
     * @param entity
     *            the named entity to analyze
     * @return boolean
     */
    protected static boolean hasID(NamedEntity entity) {
        boolean result = false;

        if (entity instanceof TLValueWithAttributes) {
            TLValueWithAttributes vwa = (TLValueWithAttributes) entity;

            for (TLAttribute attribute : PropertyCodegenUtils.getInheritedAttributes(vwa)) {
                if (isIDType(attribute.getType())) {
                    result = true;
                    break;
                }
            }

        } else { // non-VWA entity
            TLFacet entityFacet = null;

            if (entity instanceof TLAlias) {
                entity = ((TLAlias) entity).getOwningEntity();
            }

            if (entity instanceof TLFacet) {
                entityFacet = (TLFacet) entity;

            } else if (entity instanceof TLChoiceObject) {
                entityFacet = ((TLChoiceObject) entity).getSharedFacet();

            } else if (entity instanceof TLCoreObject) {
                entityFacet = ((TLCoreObject) entity).getSummaryFacet();

            } else if (entity instanceof TLBusinessObject) {
                entityFacet = ((TLBusinessObject) entity).getSummaryFacet();
            }

            result = hasIDField(entityFacet);
        }
        return result;
    }

	/**
	 * Returns true if the given facet has an attribute or element that is an
	 * <code>xsd:ID</code>.
	 * 
	 * @param facet  the facet to check for legacy schema ID fields
	 * @return boolean
	 */
	private static boolean hasIDField(TLFacet facet) {
		boolean result = false;
		
		if (facet != null) {
		    for (TLAttribute attribute : PropertyCodegenUtils.getInheritedAttributes(facet)) {
		        if (isIDType(attribute.getType())) {
		            result = true;
		            break;
		        }
		    }
		    if (!result) {
		        for (TLProperty property : PropertyCodegenUtils.getInheritedProperties(facet)) {
		            if (isIDType(property.getType())) {
		                result = true;
		                break;
		            }
		        }
		    }
		}
		return result;
	}

    /**
     * Returns true if the given type reference is 'xsd:ID'.
     * 
     * @param type
     *            the type reference to analyze
     * @return boolean
     */
    private static boolean isIDType(NamedEntity type) {
        return (type != null) && type.getNamespace().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                && type.getLocalName().equals("ID");
    }

    /**
     * Returns the <code>Versioned</code> owner of the target property.
     * 
     * @param target
     *            the target property being validated
     * @return Versioned
     */
    private Versioned getVersionedOwner(TLProperty target) {
        Versioned owner = null;

        if (target.getOwner() instanceof TLFacet) {
            TLFacetOwner facetOwner = ((TLFacet) target.getOwner()).getOwningEntity();

            if (facetOwner instanceof Versioned) {
                owner = (Versioned) facetOwner;
            }
        }
        return owner;
    }
    
    /**
     * Returns the property that is eclipsed by the given target element.  If the property's
     * owner is not a version extension or the target property does not eclipse an inherited
     * element, this method will return null.
     * 
     * @param target  the target element for which to return the eclipsed property
     * @return TLProperty
     */
    private TLProperty getEclipsedProperty(TLProperty target) {
    	TLProperty eclipsedProperty = null;
    	
    	if (target.getOwner() instanceof TLFacet) {
    		TLFacet targetFacet = (TLFacet) target.getOwner();
			String facetName = (targetFacet instanceof TLContextualFacet) ?
					((TLContextualFacet) targetFacet).getName() : null;
    		TLFacetOwner targetOwner = targetFacet.getOwningEntity();
    		TLFacetOwner extendedOwner = FacetCodegenUtils.getFacetOwnerExtension( targetOwner );
    		
    		while ((eclipsedProperty == null) && (extendedOwner != null)) {
        		TLFacet priorVersionFacet = FacetCodegenUtils.getFacetOfType(
        				extendedOwner, targetFacet.getFacetType(), facetName );
        		
        		if (priorVersionFacet != null) {
        			eclipsedProperty = priorVersionFacet.getElement( target.getName() );
        		}
        		extendedOwner = FacetCodegenUtils.getFacetOwnerExtension( extendedOwner );
    		}
    	}
    	return eclipsedProperty;
    }
    
}
