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

import java.util.List;

import org.opentravel.schemacompiler.codegen.impl.AbstractCodegenTransformer;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.codegen.json.JsonSchemaCodegenUtils;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentationOwner;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.springframework.context.ApplicationContext;

/**
 * Base class for all <code>ObjectTransformer</code> implementations that are part of the Swagger
 * code generation subsystem.
 * 
 * @param <S>  the source type of the object transformation
 * @param <T>  the target type of the object transformation
 */
public abstract class AbstractSwaggerCodegenTransformer<S, T> extends AbstractCodegenTransformer<S, T> {
	
	protected CodeGenerationSwaggerBindings swaggerBindings;
	protected JsonSchemaCodegenUtils jsonUtils;
	
    /**
     * Default constructor.
     */
    public AbstractSwaggerCodegenTransformer() {
        ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();

        if (appContext.containsBean( SchemaCompilerApplicationContext.CODE_GENERATION_SWAGGER_BINDINGS )) {
        	swaggerBindings = (CodeGenerationSwaggerBindings) appContext
                    .getBean( SchemaCompilerApplicationContext.CODE_GENERATION_SWAGGER_BINDINGS );
        }
    }

	/**
	 * @see org.opentravel.schemacompiler.transform.util.BaseTransformer#setContext(org.opentravel.schemacompiler.transform.ObjectTransformerContext)
	 */
	@Override
	public void setContext(CodeGenerationTransformerContext context) {
		super.setContext(context);
		jsonUtils = new JsonSchemaCodegenUtils( context );
	}
	
	/**
	 * Transforms the OTM documentation for the given owner and assigns it to the
	 * target JSON schema provided.
	 * 
	 * @param docOwner  the OTM documentation owner
	 * @param targetSchema  the target JSON schema that will receive the documentation
	 */
	protected void transformDocumentation(TLDocumentationOwner docOwner, JsonDocumentationOwner targetSchema) {
		TLDocumentation doc = DocumentationFinder.getDocumentation( docOwner );
		
		if (doc != null) {
	        ObjectTransformer<TLDocumentation, JsonDocumentation, CodeGenerationTransformerContext> transformer =
	        		getTransformerFactory().getTransformer(doc, JsonDocumentation.class);
			
	        targetSchema.setDocumentation( transformer.transform( doc ) );
		}
	}
	
    /**
     * Returns true if the give list of MIME types contains at least one of the supported types.
     * 
     * @param mimeTypes  the list of MIME types to check
     * @param supportedTypes  the array of supported MIME types
     * @return boolean
     */
    protected boolean containsSupportedType(List<TLMimeType> mimeTypes, TLMimeType... supportedTypes) {
    	boolean supported = false;
    	
    	for (TLMimeType supportedType : supportedTypes) {
    		supported |= mimeTypes.contains( supportedType );
    	}
    	return supported;
    }
    
}
