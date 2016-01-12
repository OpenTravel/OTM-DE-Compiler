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

import org.opentravel.schemacompiler.codegen.impl.AbstractCodegenTransformer;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.json.JsonSchemaCodegenUtils;

/**
 * Base class for all <code>ObjectTransformer</code> implementations that are part of the Swagger
 * code generation subsystem.
 * 
 * @param <S>  the source type of the object transformation
 * @param <T>  the target type of the object transformation
 */
public abstract class AbstractSwaggerCodegenTransformer<S, T> extends AbstractCodegenTransformer<S, T> {
	
	protected JsonSchemaCodegenUtils jsonUtils;
	
	/**
	 * @see org.opentravel.schemacompiler.transform.util.BaseTransformer#setContext(org.opentravel.schemacompiler.transform.ObjectTransformerContext)
	 */
	@Override
	public void setContext(CodeGenerationTransformerContext context) {
		super.setContext(context);
		jsonUtils = new JsonSchemaCodegenUtils( context );
	}

}
