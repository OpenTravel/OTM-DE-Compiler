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

import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.validate.Validatable;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.ValidatorFactory;
import org.opentravel.schemacompiler.validate.impl.TLModelValidationContext;
import org.opentravel.schemacompiler.validate.impl.TLModelValidator;
import org.opentravel.schemacompiler.visitor.DependencyNavigator;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;

/**
 * Static utility methods used for the validation of <code>TLModel</code> elements prior to
 * compilation / code generation.
 * 
 * @author S. Livezey
 */
public class TLModelCompileValidator {
	
	/**
	 * Private constructor to prevent instantiation.
	 */
	private TLModelCompileValidator() {}
	
    /**
     * Utility method that validates all elements of the given model using the default rule set for
     * library compilation.
     * 
     * @param model
     *            the model whose members should be validated
     * @return ValidationFindings
     */
    public static ValidationFindings validateModel(TLModel model) {
        return TLModelValidator.validateModel(model, ValidatorFactory.COMPILE_RULE_SET_ID);
    }

    /**
     * Utility method that validates the given model element using the specified rule set from the
     * application context file. If the object has not yet been assigned to a model, some validation
     * tasks may not function properly.
     * 
     * <p>
     * NOTE: This method performs validation checks for the given model element, as well as all of
     * its dependencies. For EXAMPLE, in a model where <code>Payment</code> contains an element
     * reference to <code>PaymentCard</code>, a call to validate <code>Payment</code> may result in
     * findings for <code>PaymentCard</code> since it is a dependency of the original model element.
     * 
     * @param modelElement
     *            the model element to validate
     * @return TLModelElement
     */
    public static ValidationFindings validateModelElement(TLModelElement modelElement) {
        return validateModelElement(modelElement, true);
    }

    /**
     * Utility method that validates the given model element using the specified rule set from the
     * application context file. If the object has not yet been assigned to a model, some validation
     * tasks may not function properly.
     * 
     * @param modelElement
     *            the model element to validate
     * @param validateDependencies
     *            flag indicating whether dependencies of the given model element should also be
     *            validated (only applies to named entity model elements)
     * @return TLModelElement
     */
    public static ValidationFindings validateModelElement(TLModelElement modelElement,
            boolean validateDependencies) {
        ValidationFindings findings;

        if (validateDependencies && (modelElement instanceof NamedEntity)) {
            ValidationDependencyVisitor visitor = new ValidationDependencyVisitor(
                    modelElement.getOwningModel());

            DependencyNavigator.navigate((NamedEntity) modelElement, visitor);
            findings = visitor.getFindings();

        } else {
            findings = TLModelValidator.validateModelElement(modelElement,
                    ValidatorFactory.COMPILE_RULE_SET_ID);
        }
        return findings;
    }

    /**
     * Visitor that performs validation checks for the assigned model element and all of its
     * dependencies to determine whether any errors exist prior to producing EXAMPLE output.
     * 
     * @author S. Livezey
     */
    private static class ValidationDependencyVisitor extends ModelElementVisitorAdapter {

        private ValidatorFactory factory;
        private ValidationFindings findings = new ValidationFindings();

        /**
         * Constructor that provides a reference to the model that owns all elements to be
         * validated.
         * 
         * @param model
         *            the model instance to be validated
         */
        public ValidationDependencyVisitor(TLModel model) {
            factory = ValidatorFactory.getInstance(ValidatorFactory.COMPILE_RULE_SET_ID,
                    new TLModelValidationContext(model));
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
         * @param entity
         *            the model element to be validated
         */
        private void validateEntity(Validatable entity) {
            if (entity instanceof LibraryElement) {
                LibraryElement libElement = (LibraryElement) entity;

                // Only validate members of user-defined libraries
                if (libElement.getOwningLibrary() instanceof TLLibrary) {
                    Validator<Validatable> validator = factory.getValidatorForTarget(entity);

                    if (validator != null) {
                        findings.addAll(validator.validate(entity));
                    }
                }
            }
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
         */
        @Override
        public boolean visitSimple(TLSimple simple) {
            validateEntity(simple);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
         */
        @Override
        public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
            validateEntity(valueWithAttributes);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitClosedEnumeration(org.opentravel.schemacompiler.model.TLClosedEnumeration)
         */
        @Override
        public boolean visitClosedEnumeration(TLClosedEnumeration enumeration) {
            validateEntity(enumeration);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitCoreObject(org.opentravel.schemacompiler.model.TLCoreObject)
         */
        @Override
        public boolean visitCoreObject(TLCoreObject coreObject) {
            validateEntity(coreObject);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitBusinessObject(org.opentravel.schemacompiler.model.TLBusinessObject)
         */
        @Override
        public boolean visitBusinessObject(TLBusinessObject businessObject) {
            validateEntity(businessObject);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitService(org.opentravel.schemacompiler.model.TLService)
         */
        @Override
        public boolean visitService(TLService service) {
            validateEntity(service);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitOperation(org.opentravel.schemacompiler.model.TLOperation)
         */
        @Override
        public boolean visitOperation(TLOperation operation) {
            validateEntity(operation);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtensionPointFacet(org.opentravel.schemacompiler.model.TLExtensionPointFacet)
         */
        @Override
        public boolean visitExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
            validateEntity(extensionPointFacet);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitFacet(org.opentravel.schemacompiler.model.TLFacet)
         */
        @Override
        public boolean visitFacet(TLFacet facet) {
            validateEntity(facet);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
         */
        @Override
        public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
            validateEntity(simpleFacet);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitListFacet(org.opentravel.schemacompiler.model.TLListFacet)
         */
        @Override
        public boolean visitListFacet(TLListFacet listFacet) {
            validateEntity(listFacet);
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAlias(org.opentravel.schemacompiler.model.TLAlias)
         */
        @Override
        public boolean visitAlias(TLAlias alias) {
            validateEntity(alias);
            return true;
        }

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitChoiceObject(org.opentravel.schemacompiler.model.TLChoiceObject)
		 */
		@Override
		public boolean visitChoiceObject(TLChoiceObject choiceObject) {
            validateEntity(choiceObject);
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResource(org.opentravel.schemacompiler.model.TLResource)
		 */
		@Override
		public boolean visitResource(TLResource resource) {
            validateEntity(resource);
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResourceParentRef(org.opentravel.schemacompiler.model.TLResourceParentRef)
		 */
		@Override
		public boolean visitResourceParentRef(TLResourceParentRef parentRef) {
            validateEntity(parentRef);
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParamGroup(org.opentravel.schemacompiler.model.TLParamGroup)
		 */
		@Override
		public boolean visitParamGroup(TLParamGroup paramGroup) {
            validateEntity(paramGroup);
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParameter(org.opentravel.schemacompiler.model.TLParameter)
		 */
		@Override
		public boolean visitParameter(TLParameter parameter) {
            validateEntity(parameter);
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAction(org.opentravel.schemacompiler.model.TLAction)
		 */
		@Override
		public boolean visitAction(TLAction action) {
            validateEntity(action);
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionRequest(org.opentravel.schemacompiler.model.TLActionRequest)
		 */
		@Override
		public boolean visitActionRequest(TLActionRequest actionRequest) {
            validateEntity(actionRequest);
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionResponse(org.opentravel.schemacompiler.model.TLActionResponse)
		 */
		@Override
		public boolean visitActionResponse(TLActionResponse actionResponse) {
            validateEntity(actionResponse);
            return true;
		}

		/**
		 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionFacet(org.opentravel.schemacompiler.model.TLActionFacet)
		 */
		@Override
		public boolean visitActionFacet(TLActionFacet facet) {
            validateEntity(facet);
            return true;
		}

    }

}
