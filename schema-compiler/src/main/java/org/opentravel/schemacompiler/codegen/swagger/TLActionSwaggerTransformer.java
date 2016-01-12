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
package org.opentravel.schemacompiler.codegen.swagger;

import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerOperation;
import org.opentravel.schemacompiler.model.TLAction;

/**
 * Performs the translation from <code>TLAction</code> objects to the Swagger model
 * objects used to produce the output.
 */
public class TLActionSwaggerTransformer extends AbstractSwaggerCodegenTransformer<TLAction,SwaggerOperation> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public SwaggerOperation transform(TLAction source) {
		return null;
	}
	
}
