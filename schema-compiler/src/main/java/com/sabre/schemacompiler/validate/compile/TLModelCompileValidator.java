/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.compile;

import com.sabre.schemacompiler.model.LibraryElement;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLClosedEnumeration;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLListFacet;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.model.TLService;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.validate.Validatable;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.Validator;
import com.sabre.schemacompiler.validate.ValidatorFactory;
import com.sabre.schemacompiler.validate.impl.TLModelValidationContext;
import com.sabre.schemacompiler.validate.impl.TLModelValidator;
import com.sabre.schemacompiler.visitor.DependencyNavigator;
import com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter;

/**
 * Static utility methods used for the validation of <code>TLModel</code> elements prior
 * to compilation / code generation.
 * 
 * @author S. Livezey
 */
public class TLModelCompileValidator {
	
	/**
	 * Utility method that validates all elements of the given model using the default rule set for
	 * library compilation.
	 * 
	 * @param model  the model whose members should be validated
	 * @return ValidationFindings
	 */
	public static ValidationFindings validateModel(TLModel model) {
		return TLModelValidator.validateModel(model, ValidatorFactory.COMPILE_RULE_SET_ID);
	}
	
	/**
	 * Utility method that validates the given model element using the specified rule set from the
	 * application context file.  If the object has not yet been assigned to a model, some validation
	 * tasks may not function properly.
	 * 
	 * <p>NOTE: This method performs validation checks for the given model element, as well as all of
	 * its dependencies.  For example, in a model where <code>Payment</code> contains an element reference
	 * to <code>PaymentCard</code>, a call to validate <code>Payment</code> may result in findings for
	 * <code>PaymentCard</code> since it is a dependency of the original model element.
	 * 
	 * @param modelElement  the model element to validate
	 * @return TLModelElement
	 */
	public static ValidationFindings validateModelElement(TLModelElement modelElement) {
		return validateModelElement(modelElement, true);
	}
	
	/**
	 * Utility method that validates the given model element using the specified rule set from the
	 * application context file.  If the object has not yet been assigned to a model, some validation
	 * tasks may not function properly.
	 * 
	 * @param modelElement  the model element to validate
	 * @param validateDependencies  flag indicating whether dependencies of the given model element should
	 *								also be validated (only applies to named entity model elements)
	 * @return TLModelElement
	 */
	public static ValidationFindings validateModelElement(TLModelElement modelElement, boolean validateDependencies) {
		ValidationFindings findings;
		
		if (validateDependencies && (modelElement instanceof NamedEntity)) {
			ValidationDependencyVisitor visitor = new ValidationDependencyVisitor(modelElement.getOwningModel());
			
			DependencyNavigator.navigate((NamedEntity) modelElement, visitor);
			findings = visitor.getFindings();
			
		} else {
			findings = TLModelValidator.validateModelElement(modelElement, ValidatorFactory.COMPILE_RULE_SET_ID);
		}
		return findings;
	}
	
	/**
	 * Visitor that performs validation checks for the assigned model element and all of its dependencies
	 * to determine whether any errors exist prior to producing example output.
	 *
	 * @author S. Livezey
	 */
	private static class ValidationDependencyVisitor extends ModelElementVisitorAdapter {
		
		private ValidatorFactory factory;
		private ValidationFindings findings = new ValidationFindings();
		
		/**
		 * Constructor that provides a reference to the model that owns all elements to be validated.
		 * 
		 * @param model  the model instance to be validated
		 */
		public ValidationDependencyVisitor(TLModel model) {
			factory = ValidatorFactory.getInstance( ValidatorFactory.COMPILE_RULE_SET_ID,
					new TLModelValidationContext(model) );
		}
		
		/**
		 * Returns the validation findings that were discovered during dependency navigation.
		 * 
		 * @return ValidationFindings
		 */
		public ValidationFindings getFindings() {
			return findings;
		}
		
		/**
		 * Validates the entity and reports any validation findings that are discovered.
		 * 
		 * @param entity  the model element to be validated
		 */
		private void validateEntity(Validatable entity) {
			if (entity instanceof LibraryElement) {
				LibraryElement libElement = (LibraryElement) entity;
				
				// Only validate members of user-defined libraries
				if (libElement.getOwningLibrary() instanceof TLLibrary) {
					Validator<Validatable> validator = factory.getValidatorForTarget(entity);
					
					if (validator != null) {
						findings.addAll( validator.validate(entity) );
					}
				}
			}
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(com.sabre.schemacompiler.model.TLSimple)
		 */
		@Override
		public boolean visitSimple(TLSimple simple) {
			validateEntity(simple);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(com.sabre.schemacompiler.model.TLValueWithAttributes)
		 */
		@Override
		public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
			validateEntity(valueWithAttributes);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitClosedEnumeration(com.sabre.schemacompiler.model.TLClosedEnumeration)
		 */
		@Override
		public boolean visitClosedEnumeration(TLClosedEnumeration enumeration) {
			validateEntity(enumeration);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitCoreObject(com.sabre.schemacompiler.model.TLCoreObject)
		 */
		@Override
		public boolean visitCoreObject(TLCoreObject coreObject) {
			validateEntity(coreObject);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitBusinessObject(com.sabre.schemacompiler.model.TLBusinessObject)
		 */
		@Override
		public boolean visitBusinessObject(TLBusinessObject businessObject) {
			validateEntity(businessObject);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitService(com.sabre.schemacompiler.model.TLService)
		 */
		@Override
		public boolean visitService(TLService service) {
			validateEntity(service);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitOperation(com.sabre.schemacompiler.model.TLOperation)
		 */
		@Override
		public boolean visitOperation(TLOperation operation) {
			validateEntity(operation);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtensionPointFacet(com.sabre.schemacompiler.model.TLExtensionPointFacet)
		 */
		@Override
		public boolean visitExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
			validateEntity(extensionPointFacet);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitFacet(com.sabre.schemacompiler.model.TLFacet)
		 */
		@Override
		public boolean visitFacet(TLFacet facet) {
			validateEntity(facet);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(com.sabre.schemacompiler.model.TLSimpleFacet)
		 */
		@Override
		public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
			validateEntity(simpleFacet);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitListFacet(com.sabre.schemacompiler.model.TLListFacet)
		 */
		@Override
		public boolean visitListFacet(TLListFacet listFacet) {
			validateEntity(listFacet);
			return true;
		}

		/**
		 * @see com.sabre.schemacompiler.visitor.ModelElementVisitorAdapter#visitAlias(com.sabre.schemacompiler.model.TLAlias)
		 */
		@Override
		public boolean visitAlias(TLAlias alias) {
			validateEntity(alias);
			return true;
		}
		
	}
	
}
