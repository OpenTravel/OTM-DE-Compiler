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

/**
 * Interface for components used to perform validation checks and report findings.
 * 
 * @param <T>
 *            the type of the object to be validated
 * @author S. Livezey
 */
public interface Validator<T extends Validatable> {

    /**
     * Assigns the context that can provide relevant information or resources that extend beyond the
     * object being validated.
     * 
     * @param context
     *            the validation context to assign
     */
    public void setValidationContext(ValidationContext context);

    /**
     * Returns the factory that created this validator instance.
     * 
     * @return ValidatorFactory
     */
    public ValidatorFactory getValidatorFactory();

    /**
     * Assings a reference to the factory used to create this validator instance.
     * 
     * @param factory
     *            the factory that created this validator instance
     */
    public void setValidatorFactory(ValidatorFactory factory);

    /**
     * Performs all necessary validation checks and returns any findings that were discovered. If no
     * findings were reported, null is a valid return value from this method.
     * 
     * @param target
     *            the target object to be validated
     * @return ValidationFindings
     */
    public ValidationFindings validate(T target);

}
