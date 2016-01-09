/**
s * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
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
package org.opentravel.schemacompiler.codegen.json;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.AbstractCodegenTransformer;
import org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.codegen.json.model.JsonLibraryInfo;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.util.SchemaCompilerInfo;
import org.opentravel.schemacompiler.util.URLUtils;

/**
 * Base class for all <code>ObjectTransformer</code> implementations that are part of the JSON schema
 * code generation subsystem.
 * 
 * @param <S>  the source type of the object transformation
 * @param <T>  the target type of the object transformation
 */
public abstract class AbstractJsonSchemaTransformer<S, T> extends AbstractCodegenTransformer<S, T> {
	
	protected JsonSchemaCodegenUtils jsonUtils;
	
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
	protected void transformDocumentation(TLDocumentationOwner docOwner, JsonSchema targetSchema) {
		TLDocumentation doc = DocumentationFinder.getDocumentation( docOwner );
		
		if (doc != null) {
	        ObjectTransformer<TLDocumentation, JsonSchemaDocumentation, CodeGenerationTransformerContext> transformer =
	        		getTransformerFactory().getTransformer(doc, JsonSchemaDocumentation.class);
			
	        targetSchema.setDocumentation( transformer.transform( doc ) );
		}
	}
	
	/**
	 * Transforms the OTM documentation for the given owner and assigns it to the
	 * target schema reference provided.
	 * 
	 * @param docOwner  the OTM documentation owner
	 * @param targetRef  the target schema reference that will receive the documentation
	 */
	protected void transformDocumentation(TLDocumentationOwner docOwner, JsonSchemaReference targetRef) {
		TLDocumentation doc = DocumentationFinder.getDocumentation( docOwner );
		
		if (doc != null) {
	        ObjectTransformer<TLDocumentation, JsonSchemaDocumentation, CodeGenerationTransformerContext> transformer =
	        		getTransformerFactory().getTransformer(doc, JsonSchemaDocumentation.class);
			
	        targetRef.setSchemaPathDocumentation( transformer.transform( doc ) );
		}
	}
	
    /**
     * Returns the JSON schema information for the given OTM library.
     * 
     * @param library  the OTM library instance for which to return info
     * @return JsonLibraryInfo
     */
    protected JsonLibraryInfo getLibraryInfo(AbstractLibrary library) {
    	CodeGenerationContext cgContext = context.getCodegenContext();
    	JsonLibraryInfo libraryInfo = new JsonLibraryInfo();

        libraryInfo.setProjectName( cgContext.getValue( CodeGenerationContext.CK_PROJECT_FILENAME ) );
        libraryInfo.setLibraryName( library.getName() );
        libraryInfo.setLibraryVersion( library.getVersion() );
        libraryInfo.setSourceFile( URLUtils.getShortRepresentation( library.getLibraryUrl() ) );
        libraryInfo.setCompilerVersion( SchemaCompilerInfo.getInstance().getCompilerVersion() );
        libraryInfo.setCompileDate( new Date() );

        if (library instanceof TLLibrary) {
        	TLLibrary tlLibrary = (TLLibrary) library;
        	
        	if (tlLibrary.getStatus() != null) {
        		libraryInfo.setLibraryStatus( tlLibrary.getStatus().toString() );
        	}
        }
        return libraryInfo;
    }

    /**
     * Adds the schemas associated with the given compile-time dependency to the current list of
     * dependencies maintained by the orchestrating code generator.
     * 
     * @param dependency
     *            the compile-time dependency to add
     */
    protected void addCompileTimeDependency(SchemaDependency dependency) {
        addCompileTimeDependency(dependency.getSchemaDeclaration());
    }

    /**
     * Adds the schemas associated with the given compile-time dependency to the current list of
     * dependencies maintained by the orchestrating code generator.
     * 
     * @param schemaDeclaration
     *            the compile-time schema declaration to add
     */
    protected void addCompileTimeDependency(SchemaDeclaration schemaDeclaration) {
        CodeGenerator<?> codeGenerator = context.getCodeGenerator();

        if (codeGenerator instanceof AbstractJaxbCodeGenerator) {
            ((AbstractJaxbCodeGenerator<?>) codeGenerator)
                    .addCompileTimeDependency(schemaDeclaration);
        }
    }

    /**
     * Returns the list of compile-time schema dependencies that have been reported during code
     * generation.
     * 
     * @return Collection<SchemaDeclaration>
     */
    protected Collection<SchemaDeclaration> getCompileTimeDependencies() {
        CodeGenerator<?> codeGenerator = context.getCodeGenerator();
        Collection<SchemaDeclaration> dependencies;

        if (codeGenerator instanceof AbstractJaxbCodeGenerator) {
            dependencies = ((AbstractJaxbCodeGenerator<?>) codeGenerator)
                    .getCompileTimeDependencies();
        } else {
            dependencies = Collections.emptySet();
        }
        return dependencies;
    }

    /**
     * Returns the location of the base output folder for the schema that is being generated.
     * 
     * @return File
     */
    protected File getBaseOutputFolder() {
        return XsdCodegenUtils.getBaseOutputFolder(context.getCodegenContext());
    }

}
