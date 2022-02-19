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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.validate.impl.CompositeValidator;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory used to access all validator implementation classes.
 * 
 * @author S. Livezey
 */
public class ValidatorFactory {

    public static final String COMPILE_RULE_SET_ID = "defaultCompileValidationRules";
    public static final String SAVE_RULE_SET_ID = "defaultSaveValidationRules";
    public static final String ASSEMBLY_RULE_SET_ID = "assemblyValidationRules";

    private static final Logger log = LogManager.getLogger( ValidatorFactory.class );

    private ValidationRuleSet ruleSet;
    private ValidationContext context;

    /**
     * Private constructor (use static methods to create factory instances).
     */
    private ValidatorFactory() {}

    /**
     * Returns the an instance of the <code>ValidatorFactory</code> that will apply the rule set with the specified
     * application context ID.
     * 
     * @param ruleSetId the bean ID of the validation rule set from the application context
     * @param context the validation context to use for all validators produced by the factory
     * @return ValidatorFactory
     */
    public static ValidatorFactory getInstance(String ruleSetId, ValidationContext context) {
        ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();
        ValidationRuleSet ruleSet = (ValidationRuleSet) appContext.getBean( ruleSetId );
        ValidatorFactory factory = new ValidatorFactory();

        factory.setRuleSet( ruleSet );
        factory.setContext( context );
        return factory;
    }

    /**
     * Returns the rule set to be applied by this factory.
     * 
     * @return ValidationRuleSet
     */
    public ValidationRuleSet getRuleSet() {
        return ruleSet;
    }

    /**
     * Assigns the rule set to be applied by this factory.
     * 
     * @param ruleSet the set of validation mappings to be applied by this factory instance
     */
    public void setRuleSet(ValidationRuleSet ruleSet) {
        this.ruleSet = ruleSet;
    }

    /**
     * Assigns the validation context for this factory instance.
     * 
     * @param context the validation context to assign
     */
    public void setContext(ValidationContext context) {
        this.context = context;
    }

    /**
     * Returns a validator capable of checking instances of the specified target class.
     * 
     * @param <T> the validation target type
     * @param targetClass the type of object for which to return a validator
     * @return Validator&lt;T&gt;
     */
    public <T extends Validatable> Validator<T> getValidatorForClass(Class<T> targetClass) {
        List<Class<Validator<T>>> validatorClasses =
            (ruleSet == null) ? new ArrayList<>() : ruleSet.getValidatorClasses( targetClass );
        Validator<T> validator = null;

        if (validatorClasses != null) {
            if (validatorClasses.size() == 1) {
                validator = newValidator( (Class<Validator<T>>) validatorClasses.get( 0 ) );

            } else if (validatorClasses.size() > 1) {
                CompositeValidator<T> cValidator = new CompositeValidator<>();

                for (Class<Validator<T>> validatorClass : validatorClasses) {
                    cValidator.addValidator( newValidator( validatorClass ) );
                }
                validator = cValidator;
            }
        }
        return validator;
    }

    /**
     * Creates a new instance of the specified validator class. If the class cannot be instantiated, an error will be
     * logged and this method will return null.
     * 
     * @param <T> the type of validator to be created
     * @param validatorClass the class of the validator to create
     * @return Validator&lt;T&gt;
     */
    private <T extends Validatable> Validator<T> newValidator(Class<Validator<T>> validatorClass) {
        Validator<T> validator = null;

        try {
            if (validatorClass != null) {
                Validator<T> newValidator = validatorClass.getConstructor().newInstance();

                newValidator.setValidatorFactory( this );
                newValidator.setValidationContext( context );
                validator = newValidator;
            }
        } catch (Exception e) {
            log.error( "Unable to instantiate validator of type: " + validatorClass.getName(), e );
        }
        return validator;
    }

    /**
     * Returns a validator capable of checking the given target object.
     * 
     * @param <T> the validation target type
     * @param target the target object to be validated
     * @return Validator&lt;T&gt;
     */
    @SuppressWarnings("unchecked")
    public <T extends Validatable> Validator<T> getValidatorForTarget(T target) {
        Validator<T> validator = null;

        if (target != null) {
            validator = (Validator<T>) getValidatorForClass( target.getClass() );
        }
        return validator;
    }

}
