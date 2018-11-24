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

import org.opentravel.schemacompiler.validate.ValidationBuilder;

/**
 * Simple extension of <code>ValidationBuilder</code> that is compatible with service
 * assemblies.
 */
public class AssemblyValidationBuilder extends ValidationBuilder<AssemblyValidationBuilder> {

	/**
	 * Constructor that supplies the message prefix for all error/warning message keys.
	 * 
	 * @param context  the context for the current validation operation
	 */
	public AssemblyValidationBuilder(String messagePrefix) {
		super( messagePrefix );
	}

	/**
	 * @see org.opentravel.schemacompiler.validate.ValidationBuilder#getThis()
	 */
	@Override
	protected AssemblyValidationBuilder getThis() {
		return this;
	}
	
}