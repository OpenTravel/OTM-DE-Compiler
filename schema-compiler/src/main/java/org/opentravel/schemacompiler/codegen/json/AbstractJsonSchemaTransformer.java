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
package org.opentravel.schemacompiler.codegen.json;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import org.opentravel.ns.ota2.appinfo_v01_00.OTA2Entity;
import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.AbstractCodegenTransformer;
import org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.codegen.json.model.JsonContextualValue;
import org.opentravel.schemacompiler.codegen.json.model.JsonEntityInfo;
import org.opentravel.schemacompiler.codegen.json.model.JsonLibraryInfo;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
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
	
	/**
	 * Returns a relative path reference to the JSON schema definition of the given named entity.
	 * 
	 * @param referencedEntity  the named entity for which to return a reference
	 * @param referencingEntity  the named entity which owns the reference
	 * @return String
	 */
	@SuppressWarnings("unchecked")
	protected String getSchemaReferencePath(NamedEntity referencedEntity, NamedEntity referencingEntity) {
		QName elementName = XsdCodegenUtils.getGlobalElementName( referencedEntity );
		StringBuilder referencePath = new StringBuilder();
		
		if (referencedEntity.getOwningLibrary() != referencingEntity.getOwningLibrary()) {
			AbstractJsonSchemaCodeGenerator<?> codeGenerator = (AbstractJsonSchemaCodeGenerator<?>) context.getCodeGenerator();
			CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder =
					(CodeGenerationFilenameBuilder<AbstractLibrary>) codeGenerator.getFilenameBuilder();
			
			if (referencedEntity.getOwningLibrary() instanceof BuiltInLibrary) {
				referencePath.append( getBuiltInSchemaOutputLocation() );
				
			} else {
				referencePath.append( "./" );
			}
			referencePath.append( filenameBuilder.buildFilename( referencedEntity.getOwningLibrary(), "json" ) );
		}
		referencePath.append( "#/definitions/" );
		
		if (elementName != null) {
			referencePath.append( elementName.getLocalPart() );
			
		} else {
			referencePath.append( XsdCodegenUtils.getGlobalTypeName( referencedEntity ) );
		}
		return referencePath.toString();
	}
	
	/**
	 * Returns a relative path reference to the JSON schema definition of the given schema dependency.  If
	 * the referenced type does not have an associated JSON definition, this method will return null.
	 * 
	 * @param referencedEntity  the schema dependency for which to return a reference
	 * @param referencingEntity  the named entity which owns the reference
	 * @return String
	 */
	protected String getSchemaReferencePath(SchemaDependency schemaDependency, NamedEntity referencingEntity) {
		String referencedFilename = schemaDependency.getSchemaDeclaration().getFilename(
				CodeGeneratorFactory.JSON_SCHEMA_TARGET_FORMAT );
		String referencePath = null;
		
		if (referencedFilename != null) {
			referencePath = getBuiltInSchemaOutputLocation() + referencedFilename
					+ "#/definitions/" + schemaDependency.getLocalName();
		}
		return referencePath;
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
     * Returns the JSON schema information for the given OTM named entity.
     * 
     * @param entity  the OTM library instance for which to return info
     * @return JsonEntityInfo
     */
    protected JsonEntityInfo getEntityInfo(NamedEntity entity) {
    	OTA2Entity jaxbInfo = XsdCodegenUtils.buildEntityAppInfo( entity );
    	JsonEntityInfo entityInfo = new JsonEntityInfo();
    	
    	entityInfo.setEntityName( jaxbInfo.getValue() );
    	entityInfo.setEntityType( jaxbInfo.getType() );
    	return entityInfo;
    }
    
    /**
     * Returns the list of equivalent values for the JSON schema documentation.
     * 
     * @param entity  the entity for which to equivalent example values
     * @return List<JsonContextualValue>
     */
    protected List<JsonContextualValue> getEquivalentInfo(TLEquivalentOwner entity) {
    	List<JsonContextualValue> equivValues = new ArrayList<>();
    	
    	for (TLEquivalent equiv : entity.getEquivalents()) {
    		JsonContextualValue jsonEquiv = new JsonContextualValue();
    		
    		jsonEquiv.setContext( equiv.getContext() );
    		jsonEquiv.setValue( equiv.getDescription() );
    		equivValues.add( jsonEquiv );
    	}
    	return equivValues;
    }
    
    /**
     * Returns the list of example values for the JSON schema documentation.
     * 
     * @param entity  the entity for which to return example values
     * @return List<JsonContextualValue>
     */
    protected List<JsonContextualValue> getExampleInfo(TLExampleOwner entity) {
    	List<JsonContextualValue> exampleValues = new ArrayList<>();
    	
    	for (TLExample example : entity.getExamples()) {
    		JsonContextualValue jsonExample = new JsonContextualValue();
    		
    		jsonExample.setContext( example.getContext() );
    		jsonExample.setValue( example.getValue() );
    		exampleValues.add( jsonExample );
    	}
    	return exampleValues;
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
