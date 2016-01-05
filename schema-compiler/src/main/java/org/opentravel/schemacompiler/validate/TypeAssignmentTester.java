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
package org.opentravel.schemacompiler.validate;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.validate.compile.TLAttributeCompileValidator;
import org.opentravel.schemacompiler.validate.compile.TLPropertyCompileValidator;
import org.opentravel.schemacompiler.validate.compile.TLValueWithAttributesCompileValidator;
import org.opentravel.schemacompiler.validate.impl.TLModelValidationContext;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Provides utility methods to test the assignments of model objects before those assignments
 * are actually made.  These tests will not return the reason for an invalid assignment, only
 * whether or not a potential assignment is legal.
 * 
 * @author S. Livezey
 */
public class TypeAssignmentTester {
	
	private static final String ATTRIBUTE_ERROR_PREFIX    = "org.opentravel.schemacompiler.TLAttribute.type.";
	private static final String ELEMENT_ERROR_PREFIX      = "org.opentravel.schemacompiler.TLProperty.type.";
	private static final String SIMPLE_ERROR_PREFIX       = "org.opentravel.schemacompiler.TLSimple.parentType.";
	private static final String SIMPLE_FACET_ERROR_PREFIX = "org.opentravel.schemacompiler.TLSimpleFacet.simpleType.";
	private static final String VWA_ERROR_PREFIX          = "org.opentravel.schemacompiler.TLValueWithAttributes.parentType.";
	
	private static Collection<String> typeAssignmentErrorKeys = Arrays.asList( new String[] {
			// Attribute error codes
			ATTRIBUTE_ERROR_PREFIX + TLValidationBuilder.INVALID_NAMED_ENTITY_REFERENCE,
			ATTRIBUTE_ERROR_PREFIX + TLAttributeCompileValidator.ERROR_NON_SIMPLE_CORE_AS_ATTRIBUTE,
			ATTRIBUTE_ERROR_PREFIX + TLAttributeCompileValidator.ERROR_ILLEGAL_LIST_FACET_REFERENCE,
			ATTRIBUTE_ERROR_PREFIX + TLAttributeCompileValidator.ERROR_ILLEGAL_VWA_ATTRIBUTE,
			ATTRIBUTE_ERROR_PREFIX + TLAttributeCompileValidator.ERROR_ILLEGAL_OPEN_ENUM_ATTRIBUTE,
			
			// Element error codes
			ELEMENT_ERROR_PREFIX + TLValidationBuilder.INVALID_NAMED_ENTITY_REFERENCE,
			ELEMENT_ERROR_PREFIX + TLPropertyCompileValidator.ERROR_ILLEGAL_LIST_FACET_REFERENCE,
			
			// Simple type error codes
			SIMPLE_ERROR_PREFIX + TLValidationBuilder.INVALID_NAMED_ENTITY_REFERENCE,
			
			// Simple facet error codes
			SIMPLE_FACET_ERROR_PREFIX + TLValidationBuilder.INVALID_NAMED_ENTITY_REFERENCE,
			
			// VWA error codes
			VWA_ERROR_PREFIX + TLValidationBuilder.INVALID_NAMED_ENTITY_REFERENCE,
			VWA_ERROR_PREFIX + TLValueWithAttributesCompileValidator.ERROR_ILLEGAL_EXTENSION_ATTRIBUTE,
	});
	
	private TLModel model;
	private String lastCompilerExtension;
	private ValidatorFactory validatorFactory;
	private Validator<TLAttribute> attributeValidator;
	private Validator<TLProperty> elementValidator;
	private Validator<TLSimple> simpleValidator;
	private Validator<TLSimpleFacet> simpleFacetValidator;
	private Validator<TLValueWithAttributes> vwaValidator;
	
	/**
	 * Constructor that establishes the model context for all validation checks.\
	 * 
	 * @param model  the model to which all validated elements will belong
	 */
	public TypeAssignmentTester(TLModel model) {
		this.model = model;
	}
	
	/**
	 * Returns true if the given candidate is a valid type assignment for the attribute.
	 * 
	 * @param attribute  the attribute to which the candidate type may be assigned
	 * @param candidateType  the candidate type assignment for the attribute
	 * @return boolean
	 */
	public boolean isValidAssignment(TLAttribute attribute, NamedEntity candidateType) {
		boolean result;
		
		if (candidateType instanceof TLAttributeType) {
			TLAttribute testAttribute = new TLAttribute();
			
			testAttribute.setName("test");
			testAttribute.setOwner(attribute.getOwner());
			testAttribute.setType((TLAttributeType) candidateType);
			
			initValidators();
			result = isLegalAssignment( attributeValidator.validate( testAttribute ) );
			
		} else {
			result = false;
		}
		return result;
	}
	
	/**
	 * Returns true if the given candidate is a valid type assignment for the element.
	 * 
	 * @param element  the element to which the candidate type may be assigned
	 * @param candidateType  the candidate type assignment for the element
	 * @return boolean
	 */
	public boolean isValidAssignment(TLProperty element, NamedEntity candidateType) {
		boolean result;
		
		if (candidateType instanceof TLPropertyType) {
			TLProperty testElement = new TLProperty();
			
			testElement.setName("test");
			testElement.setOwner(element.getOwner());
			testElement.setType((TLPropertyType) candidateType);
			
			initValidators();
			result = isLegalAssignment( elementValidator.validate( testElement ) );
			
		} else {
			result = false;
		}
		return result;
	}
	
	/**
	 * Returns true if the given candidate is a valid base type assignment for the simple type.
	 * 
	 * @param simpleType  the simple type to which the candidate base type may be assigned
	 * @param candidateBaseType  the candidate base type assignment for the simple
	 * @return boolean
	 */
	public boolean isValidAssignment(TLSimple simpleType, NamedEntity candidateBaseType) {
		boolean result;
		
		if (candidateBaseType instanceof TLAttributeType) {
			TLSimple testSimple = new TLSimple();
			
			testSimple.setName("test");
			testSimple.setOwningLibrary(simpleType.getOwningLibrary());
			testSimple.setParentType((TLAttributeType) candidateBaseType);
			
			initValidators();
			result = isLegalAssignment( simpleValidator.validate( testSimple ) );
			
		} else {
			result = false;
		}
		return result;
	}
	
	/**
	 * Returns true if the given candidate is a valid type assignment for the simple facet.
	 * 
	 * @param element  the simple facet to which the candidate type may be assigned
	 * @param candidateType  the candidate type assignment for the simple facet
	 * @return boolean
	 */
	public boolean isValidAssignment(TLSimpleFacet simpleFacet, NamedEntity candidateType) {
		boolean result;
		
		if (candidateType instanceof TLAttributeType) {
			TLSimpleFacet testFacet = new TLSimpleFacet();
			
			testFacet.setFacetType(TLFacetType.SIMPLE);
			testFacet.setOwningEntity(simpleFacet.getOwningEntity());
			testFacet.setSimpleType((TLAttributeType) candidateType);
			
			initValidators();
			result = isLegalAssignment( simpleFacetValidator.validate( testFacet ) );
			
		} else {
			result = false;
		}
		return result;
	}
	
	/**
	 * Returns true if the given candidate is a valid base type assignment for the VWA.
	 * 
	 * @param element  the VWA to which the candidate base type may be assigned
	 * @param candidateBaseType  the candidate base type assignment for the VWA
	 * @return boolean
	 */
	public boolean isValidAssignment(TLValueWithAttributes vwa, NamedEntity candidateBaseType) {
		boolean result;
		
		if (candidateBaseType instanceof TLAttributeType) {
			TLValueWithAttributes testVWA = new TLValueWithAttributes();
			
			testVWA.setName("test");
			testVWA.setOwningLibrary(vwa.getOwningLibrary());
			testVWA.setParentType((TLAttributeType) candidateBaseType);
			
			initValidators();
			result = isLegalAssignment( vwaValidator.validate( testVWA ) );
			
		} else {
			result = false;
		}
		return result;
	}
	
	/**
	 * Returns true if none of the findings provided are flagged as illegal type assignments
	 * for elements or attributes.
	 * 
	 * @param findings  the validation findings to check
	 * @return boolean
	 */
	private boolean isLegalAssignment(ValidationFindings findings) {
		boolean isLegal;
		
		if ((findings == null) || !findings.hasFinding(FindingType.ERROR)) {
			isLegal = true;
			
		} else {
			Set<String> errorKeys = new HashSet<>();
			
			for (ValidationFinding finding : findings.getFindingsAsList(FindingType.ERROR)) {
				errorKeys.add( finding.getMessageKey() );
			}
			errorKeys.retainAll( typeAssignmentErrorKeys );
			isLegal = errorKeys.isEmpty();
		}
		return isLegal;
	}
	
	/**
	 * Initializes the validators to be used for all checks performed by this class.
	 */
	private synchronized void initValidators() {
		ValidatorFactory factory = getFactory();
		
		if (factory != this.validatorFactory) {
			this.validatorFactory = factory;
			attributeValidator = factory.getValidatorForClass( TLAttribute.class );
			elementValidator = factory.getValidatorForClass( TLProperty.class );
			simpleValidator = factory.getValidatorForClass( TLSimple.class );
			simpleFacetValidator = factory.getValidatorForClass( TLSimpleFacet.class );
			vwaValidator = factory.getValidatorForClass( TLValueWithAttributes.class );
		}
	}
	
	/**
	 * Returns a pre-configured instance of the <code>ValidatorFactory</code>.
	 * 
	 * @return ValidatorFactory
	 */
	private synchronized ValidatorFactory getFactory() {
		ValidatorFactory factory = this.validatorFactory;
		boolean initFactory = false;
		
		if (factory == null) {
			initFactory = true;
			
		} else {
			String activeCompilerExtension = CompilerExtensionRegistry.getActiveExtension();
			
			if (!activeCompilerExtension.equals(lastCompilerExtension)) {
				lastCompilerExtension = activeCompilerExtension;
				initFactory = true;
			}
		}
		
		if (initFactory) {
			factory = ValidatorFactory.getInstance(ValidatorFactory.COMPILE_RULE_SET_ID,
                    new TLModelValidationContext(model));
		}
		return factory;
	}
	
}
