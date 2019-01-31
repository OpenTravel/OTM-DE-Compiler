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

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
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
import org.opentravel.schemacompiler.validate.base.TLAttributeBaseValidator;
import org.opentravel.schemacompiler.validate.impl.DuplicateFieldChecker;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.ValidatorUtils;
import org.opentravel.schemacompiler.version.Versioned;

/**
 * Validator for the <code>TLAttribute</code> class.
 * 
 * @author S. Livezey
 */
public class TLAttributeCompileValidator extends TLAttributeBaseValidator {
	
	public static final String ERROR_NON_SIMPLE_CORE_AS_ATTRIBUTE = "NON_SIMPLE_CORE_AS_ATTRIBUTE";
	public static final String ERROR_ILLEGAL_LIST_FACET_REFERENCE = "ILLEGAL_LIST_FACET_REFERENCE";
    public static final String WARNING_LIST_FACET_REPEAT_IGNORED = "LIST_FACET_REPEAT_IGNORED";
	public static final String ERROR_EMPTY_FACET_REFERENCED = "EMPTY_FACET_REFERENCED";
	public static final String ERROR_ILLEGAL_VWA_ATTRIBUTE = "ILLEGAL_VWA_ATTRIBUTE";
	public static final String ERROR_ILLEGAL_OPEN_ENUM_ATTRIBUTE = "ILLEGAL_OPEN_ENUM_ATTRIBUTE";
	public static final String ERROR_ILLEGAL_REQUIRED_ATTRIBUTE = "ILLEGAL_REQUIRED_ATTRIBUTE";
	public static final String ERROR_ILLEGAL_REFERENCE = "ILLEGAL_REFERENCE";
	public static final String WARNING_INVALID_REFERENCE_NAME = "INVALID_REFERENCE_NAME";
	public static final String WARNING_BOOLEAN_TYPE_REFERENCE = "BOOLEAN_TYPE_REFERENCE";
	public static final String WARNING_LEGACY_IDREF = "LEGACY_IDREF";
	public static final String WARNING_ILLEGAL_BUSINESS_OBJECT_ID = "ILLEGAL_BUSINESS_OBJECT_ID";
	public static final String WARNING_ILLEGAL_CORE_OBJECT_ID = "ILLEGAL_CORE_OBJECT_ID";
	
	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLAttribute target) {
		TLValidationBuilder builder = newValidationBuilder( target );
		
		builder.setProperty( "name", target.getName() ).setFindingType( FindingType.ERROR )
			.assertPatternMatch( NAME_XML_PATTERN );
		
		if (target.isReference() && (target.getName() != null) && !target.getName().endsWith( "Ref" )) {
			builder.addFinding( FindingType.WARNING, "name", WARNING_INVALID_REFERENCE_NAME, target.getName() + "Ref" );
		}
		
		// Check for duplicate names of this attribute
		if (target.getName() != null) {
			DuplicateFieldChecker dupChecker = getDuplicateFieldChecker( target );
			
			if (dupChecker.isDuplicateName( target )) {
				builder.addFinding( FindingType.ERROR, "name", ValidationBuilder.ERROR_DUPLICATE_ELEMENT,
						target.getName() );
			}
		}
		
		builder.setProperty( "equivalents", target.getEquivalents() ).setFindingType( FindingType.ERROR )
			.assertNotNull().assertContainsNoNullElements();
		
		// Verify that properties of minor version extension are optional
		if (target.isMandatory() && isVersionExtension( getVersionedOwner( target ) )) {
			builder.addFinding( FindingType.ERROR, "mandatory", ERROR_ILLEGAL_REQUIRED_ATTRIBUTE );
		}
		
		// Validate the characteristics of the attribute type
		builder.setEntityReferenceProperty( "type", target.getType(), target.getTypeName() )
			.setFindingType( FindingType.ERROR ).assertNotNull().setFindingType( FindingType.WARNING )
			.assertNotDeprecated().assertNotObsolete();
		
		if (target.isReference()) {
			validateReferenceType( target, builder );
			
		} else {
			validateStandardType( target, builder );
		}
		
		// Issue a warning if an empty facet is referenced (applies to standard
		// attributes and references)
		if (target.getType() instanceof TLAbstractFacet) {
			TLAbstractFacet referencedFacet = (TLAbstractFacet) target.getType();
			
			if (!referencedFacet.declaresContent()) {
				builder.addFinding( FindingType.WARNING, "type", ERROR_EMPTY_FACET_REFERENCED,
						referencedFacet.getFacetType().getIdentityName(),
						referencedFacet.getOwningEntity().getLocalName() );
			}
		}
		
		validateListFacetTypeAssignment( target, builder );
		
		return builder.getFindings();
	}

	/**
	 * List facets can only be referenced if the core object defines one or more
	 * roles (applies to both standard attributes and references).
	 * 
	 * @param target  the target atribute being validated
	 * @param builder  the validation builder where errors and warnings will be reported
	 */
	private void validateListFacetTypeAssignment(TLAttribute target, TLValidationBuilder builder) {
		if (target.getType() instanceof TLListFacet) {
			TLListFacet listFacet = (target.getType() instanceof TLListFacet) ? (TLListFacet) target.getType()
					: (TLListFacet) ((TLAlias) target.getType()).getOwningEntity();
			TLCoreObject referencedCore = (TLCoreObject) ((TLListFacet) target.getType()).getOwningEntity();
			int roleCount = referencedCore.getRoleEnumeration().getRoles().size();
			
			if ((roleCount == 0) && !(listFacet.getItemFacet() instanceof TLSimpleFacet)) {
				builder.addFinding( FindingType.ERROR, "type", ERROR_ILLEGAL_LIST_FACET_REFERENCE,
						referencedCore.getName() );
			}
            if ((target.getReferenceRepeat() != 0) && (target.getReferenceRepeat() != roleCount)) {
                String repeatValue = (target.getReferenceRepeat() < 0) ? "*" : (target.getReferenceRepeat() + "");
                builder.addFinding( FindingType.WARNING, "repeat", WARNING_LIST_FACET_REPEAT_IGNORED, repeatValue );
            }
		}
	}
	
	/**
	 * Performs attribute type validation for non-reference attributes.
	 * 
	 * @param target the target attribute being validated
	 * @param builder the validation builder where all findings should be reported
	 */
	private void validateStandardType(TLAttribute target, TLValidationBuilder builder) {
		TLPropertyType attributeType = target.getType();
		
		builder.setEntityReferenceProperty( "type", attributeType, target.getTypeName() ).assertValidEntityReference(
				TLClosedEnumeration.class, TLOpenEnumeration.class, TLRoleEnumeration.class,
				TLValueWithAttributes.class, TLSimple.class, TLCoreObject.class, TLSimpleFacet.class, TLListFacet.class,
				XSDSimpleType.class );
		
		// VWA and open enumeration attributes are only allowed when the
		// attribute's owner is a VWA
		if (!(target.getOwner() instanceof TLValueWithAttributes)) {
			if (attributeType instanceof TLValueWithAttributes) {
				builder.addFinding( FindingType.ERROR, "type", ERROR_ILLEGAL_VWA_ATTRIBUTE );
			} else if (attributeType instanceof TLOpenEnumeration) {
				builder.addFinding( FindingType.ERROR, "type", ERROR_ILLEGAL_OPEN_ENUM_ATTRIBUTE );
			}
		}
		
		checkEmptyValueType( target, attributeType, "type", builder );
		
		// A warning will be issued for boolean attributes (should be indicators)
		if (ValidatorUtils.isBooleanType( target.getType() )) {
			builder.addFinding( FindingType.WARNING, "type", WARNING_BOOLEAN_TYPE_REFERENCE );
		}
		
		// Cores are only allowed as attribute types if they publish a simple facet
		if (attributeType instanceof TLCoreObject) {
			TLCoreObject coreObject = (TLCoreObject) target.getType();
			
			if (!coreObject.getSimpleFacet().declaresContent()) {
				builder.addFinding( FindingType.ERROR, "type", ERROR_NON_SIMPLE_CORE_AS_ATTRIBUTE,
						coreObject.getLocalName() );
			}
		}
		
		validateXsdIdTypeAssignment( target, builder, attributeType );
		
		// The reference-repeat value is meaningless for non-reference
		// attributes
		builder.setProperty( "referenceRepeat", target.getReferenceRepeat() ).setFindingType( FindingType.WARNING )
			.assertEquals( 0 );
		
		// Warn if a deprecated XSD date/time type is being referenced
		TLAttributeOwner attrOwner = target.getOwner();
		AbstractLibrary owningLibrary = (attrOwner == null) ? null : attrOwner.getOwningLibrary();
		
		validateDeprecatedDateTimeUsage( attributeType, owningLibrary, builder );
	}

	/**
	 * If the given attribute's type assignment is an xsd:ID, make sure it is contained
	 * in the top-level facet if the owner is a core or business object.
	 * 
	 * @param target the target attribute being validated
	 * @param builder the validation builder where all findings should be reported
	 * @param attributeType  the attribute's resolved type
	 */
	private void validateXsdIdTypeAssignment(TLAttribute target, TLValidationBuilder builder,
			TLPropertyType attributeType) {
		if (ValidatorUtils.isXsdID( attributeType ) && (target.getOwner() instanceof TLFacet)) {
			TLFacet facet = (TLFacet) target.getOwner();
			TLFacetOwner facetOwner = facet.getOwningEntity();
			
			if (facetOwner instanceof TLBusinessObject) {
				if ((facet.getFacetType() != TLFacetType.ID) && (facet.getFacetType() != TLFacetType.QUERY)) {
					builder.addFinding( FindingType.WARNING, "type", WARNING_ILLEGAL_BUSINESS_OBJECT_ID );
				}
			} else if ((facetOwner instanceof TLCoreObject) && (facet.getFacetType() != TLFacetType.SUMMARY)) {
				builder.addFinding( FindingType.WARNING, "type", WARNING_ILLEGAL_CORE_OBJECT_ID );
			}
		}
	}
	
	/**
	 * Performs attribute type validation for non-reference attributes.
	 * 
	 * @param target the target attribute being validated
	 * @param builder the validation builder where all findings should be reported
	 */
	private void validateReferenceType(TLAttribute target, TLValidationBuilder builder) {
		TLPropertyType attributeType = target.getType();
		
		builder.setEntityReferenceProperty( "refType", target.getType(), target.getTypeName() )
			.setFindingType( FindingType.ERROR ).assertValidEntityReference( TLValueWithAttributes.class, TLFacet.class,
					TLListFacet.class, TLCoreObject.class, TLChoiceObject.class, TLBusinessObject.class, TLAlias.class,
					XSDComplexType.class, XSDElement.class );
		
		if ((attributeType != null) && target.isReference() && !TLPropertyCompileValidator.hasID( attributeType )) {
			builder.addFinding( FindingType.ERROR, "refType", ERROR_ILLEGAL_REFERENCE, target.getTypeName() );
		}
		
	}
	
	/**
	 * Returns a <code>DuplicateFieldChecker</code> that can be used to identify duplicate field names within the
	 * attributes of the declaring facet or VWA.
	 * 
	 * @param target the target attribute being validated
	 * @return DuplicateFieldChecker
	 */
	private DuplicateFieldChecker getDuplicateFieldChecker(TLAttribute target) {
		TLAttributeOwner attrOwner = target.getOwner();
		String cacheKey = attrOwner.getNamespace() + ":" + attrOwner.getLocalName() + ":dupChecker";
		DuplicateFieldChecker checker = (DuplicateFieldChecker) getContextCacheEntry( cacheKey );
		
		if (checker == null) {
			checker = new DuplicateFieldChecker( attrOwner );
			setContextCacheEntry( cacheKey, checker );
		}
		return checker;
	}
	
	/**
	 * Returns the <code>Versioned</code> owner of the target attribute.
	 * 
	 * @param target the target attribute being validated
	 * @return Versioned
	 */
	private Versioned getVersionedOwner(TLAttribute target) {
		Versioned owner = null;
		
		if (target.getOwner() instanceof TLFacet) {
			TLFacetOwner facetOwner = ((TLFacet) target.getOwner()).getOwningEntity();
			
			if (facetOwner instanceof Versioned) {
				owner = (Versioned) facetOwner;
			}
		} else if (target.getOwner() instanceof TLValueWithAttributes) {
			owner = (Versioned) target.getOwner();
		}
		return owner;
	}
	
}
