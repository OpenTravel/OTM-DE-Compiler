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
import java.util.Collection;
import java.util.List;

import org.opentravel.schemacompiler.validate.Validatable;
import org.opentravel.schemacompiler.validate.ValidationContext;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.ValidatorFactory;

/**
 * Validator implementation that can combine and apply multiple validator implementations to a
 * single <code>Validatable</code> object.
 * 
 * @param <T>
 *            the type of the object to be validated
 * @author S. Livezey
 */
public class CompositeValidator<T extends Validatable> implements Validator<T> {

    private List<Validator<T>> validators = new ArrayList<>();
    private ValidationContext context;
    private ValidatorFactory factory;

    /**
     * Default constructor.
     */
    public CompositeValidator() {
    }

    /**
     * Constructor that initializes this composite validator with a single nested validator
     * instance.
     * 
     * @param validator
     *            the validator to add to this composite
     */
    public CompositeValidator(Validator<T> validator) {
        addValidator(validator);
    }

    /**
     * Constructor that initializes this composite validator with the given collection of nested
     * validators.
     * 
     * @param validators
     *            the list of validators to add to this composite
     */
    public CompositeValidator(Collection<Validator<T>> validators) {
        if (validators != null) {
            for (Validator<T> validator : validators) {
                addValidator(validator);
            }
        }
    }

    /**
     * Adds the given validator to the current list maintained by this composite.
     * 
     * @param validator
     *            the validator to add to this composite
     */
    public void addValidator(Validator<T> validator) {
        if (validator != null) {
            validators.add(validator);
            validator.setValidationContext(context);
            validator.setValidatorFactory(factory);
        }
    }

    /**
     * Removes the given validator from the current list maintained by this composite.
     * 
     * @param validator
     *            the validator to remove from this composite
     */
    public void removeValidator(Validator<T> validator) {
        if (validator != null) {
            validators.remove(validator);
            validator.setValidationContext(null);
            validator.setValidatorFactory(null);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validator#setValidationContext(org.opentravel.schemacompiler.validate.ValidationContext)
     */
    @Override
    public void setValidationContext(ValidationContext context) {
        this.context = context;

        for (Validator<T> validator : validators) {
            validator.setValidationContext(context);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validator#getValidatorFactory()
     */
    @Override
    public ValidatorFactory getValidatorFactory() {
        return factory;
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validator#setValidatorFactory(org.opentravel.schemacompiler.validate.ValidatorFactory)
     */
    @Override
    public void setValidatorFactory(ValidatorFactory factory) {
        this.factory = factory;

        for (Validator<T> validator : validators) {
            validator.setValidatorFactory(factory);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validator#validate(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    public ValidationFindings validate(T target) {
        ValidationFindings findings = new ValidationFindings();

        for (Validator<T> validator : validators) {
            findings.addAll(validator.validate(target));
        }
        return findings;
    }

}
