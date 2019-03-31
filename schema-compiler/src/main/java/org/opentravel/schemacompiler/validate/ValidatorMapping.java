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
 * Configuration element used to associate a target <code>Validatable</code> class with a <code>Validator</code>
 * implementation class.
 * 
 * @param <T> the validatable type to which this mapping applies
 * @author S. Livezey
 */
public class ValidatorMapping<T extends Validatable> {

    private Class<T> targetClass;
    private Class<Validator<T>> validatorClass;

    /**
     * Returns the target class for the validation mapping.
     * 
     * @return Class&lt;T&gt;
     */
    public Class<T> getTargetClass() {
        return targetClass;
    }

    /**
     * Assigns the target class for the validation mapping.
     * 
     * @param targetClass the target class to assign
     */
    public void setTargetClass(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * Returns the validator class for the validation mapping.
     * 
     * @return Class&lt;Validator&lt;T&gt;&gt;
     */
    public Class<Validator<T>> getValidatorClass() {
        return validatorClass;
    }

    /**
     * Assigns the validator class for the validation mapping.
     * 
     * @param validatorClass the validator class to assign
     */
    public void setValidatorClass(Class<Validator<T>> validatorClass) {
        this.validatorClass = validatorClass;
    }

}
