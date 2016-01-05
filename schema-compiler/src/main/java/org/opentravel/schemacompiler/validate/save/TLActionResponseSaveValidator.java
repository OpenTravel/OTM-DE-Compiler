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
package org.opentravel.schemacompiler.validate.save;

import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLActionResponseBaseValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLActionResponse</code> class.
 * 
 * @author S. Livezey
 */
public class TLActionResponseSaveValidator extends TLActionResponseBaseValidator {

	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLActionResponse target) {
        TLValidationBuilder builder = newValidationBuilder(target);
        
        return builder.getFindings();
	}

}
