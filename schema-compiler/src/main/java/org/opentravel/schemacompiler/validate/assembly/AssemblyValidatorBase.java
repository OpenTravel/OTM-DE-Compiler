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

package org.opentravel.schemacompiler.validate.assembly;

import org.opentravel.schemacompiler.validate.Validatable;
import org.opentravel.schemacompiler.validate.ValidationContext;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.ValidatorFactory;
import org.opentravel.schemacompiler.validate.impl.TLValidatorBase;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

/**
 * Abstract base class for all <code>ServiceAssembly</code> validators.
 */
public abstract class AssemblyValidatorBase<T extends Validatable> implements Validator<T> {
	
	protected static VersionScheme versionScheme;
	
    private AssemblyValidationContext context;
    private ValidatorFactory validatorFactory;

	/**
	 * @see org.opentravel.schemacompiler.validate.Validator#setValidationContext(org.opentravel.schemacompiler.validate.ValidationContext)
	 */
	@Override
	public void setValidationContext(ValidationContext context) {
        if (context instanceof AssemblyValidationContext) {
            this.context = (AssemblyValidationContext) context;

        } else if (context == null) {
            throw new NullPointerException("The assembly validation context is requied.");

        } else {
            throw new IllegalArgumentException(
                    "The validation context must be an instance of AssemblyValidationContext.");
        }
	}

	/**
	 * @see org.opentravel.schemacompiler.validate.Validator#getValidatorFactory()
	 */
	@Override
	public ValidatorFactory getValidatorFactory() {
        return validatorFactory;
	}

	/**
	 * @see org.opentravel.schemacompiler.validate.Validator#setValidatorFactory(org.opentravel.schemacompiler.validate.ValidatorFactory)
	 */
	@Override
	public void setValidatorFactory(ValidatorFactory factory) {
		this.validatorFactory = factory;
	}
	
	/**
	 * Returns the context for the current validation operation.
	 * 
	 * @return AssemblyValidationContext
	 */
	protected AssemblyValidationContext getValidationContext() {
		return context;
	}

    /**
     * Creates a new validation builder instance for the given target object.
     * 
     * @param targetObject  the target object to be validated
     * @return AssemblyValidationBuilder
     */
    protected AssemblyValidationBuilder newValidationBuilder(T targetObject) {
        return new AssemblyValidationBuilder( TLValidatorBase.TLMODEL_PREFIX ).setTargetObject(targetObject);
    }
    
	/**
	 * Initialize the default version scheme.
	 */
	static {
		try {
			VersionSchemeFactory factory = VersionSchemeFactory.getInstance();
			versionScheme = factory.getVersionScheme( factory.getDefaultVersionScheme() );
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError( t );
		}
	}
	
}
