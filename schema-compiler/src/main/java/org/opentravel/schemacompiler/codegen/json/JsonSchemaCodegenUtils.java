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

import java.util.ArrayList;
import java.util.List;

import org.opentravel.ns.ota2.appinfo_v01_00.OTA2Entity;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.json.model.JsonContextualValue;
import org.opentravel.schemacompiler.codegen.json.model.JsonEntityInfo;
import org.opentravel.schemacompiler.codegen.util.JsonSchemaNamingUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;

/**
 * Static utility methods used during the generation of JSON schema output.
 */
public class JsonSchemaCodegenUtils {
	
	public static final String JSON_SCHEMA_FILENAME_EXT = "schema.json";
	
	private CodeGenerationTransformerContext context;
	
	/**
	 * Constructor that supplies the current code generation transformer context.
	 * 
	 * @param context  the code generation transformer context
	 */
	public JsonSchemaCodegenUtils(CodeGenerationTransformerContext context) {
		this.context = context;
	}
	
    /**
     * Returns the JSON schema information for the given OTM named entity.
     * 
     * @param entity  the OTM library instance for which to return info
     * @return JsonEntityInfo
     */
    public JsonEntityInfo getEntityInfo(NamedEntity entity) {
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
    public List<JsonContextualValue> getEquivalentInfo(TLEquivalentOwner entity) {
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
    public List<JsonContextualValue> getExampleInfo(TLExampleOwner entity) {
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
	 * Returns a relative path reference to the JSON schema definition of the given named entity.
	 * 
	 * @param referencedEntity  the named entity for which to return a reference
	 * @param referencingEntity  the named entity which owns the reference
	 * @return String
	 */
	@SuppressWarnings("unchecked")
	public String getSchemaReferencePath(NamedEntity referencedEntity, NamedEntity referencingEntity) {
		String elementName = JsonSchemaNamingUtils.getGlobalDefinitionName( referencedEntity );
		StringBuilder referencePath = new StringBuilder();
		
		if (referencedEntity.getOwningLibrary() != referencingEntity.getOwningLibrary()) {
			AbstractJsonSchemaCodeGenerator<?> codeGenerator =
					(AbstractJsonSchemaCodeGenerator<?>) context.getCodeGenerator();
			CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder =
					(CodeGenerationFilenameBuilder<AbstractLibrary>) codeGenerator.getFilenameBuilder();
			
			if (referencedEntity.getOwningLibrary() instanceof BuiltInLibrary) {
				String builtInLocation = XsdCodegenUtils.getBuiltInSchemaOutputLocation( context.getCodegenContext() );
				
				referencePath.append( builtInLocation );
				
			} else {
				referencePath.append( "./" );
			}
			referencePath.append( filenameBuilder.buildFilename(
					referencedEntity.getOwningLibrary(), JsonSchemaCodegenUtils.JSON_SCHEMA_FILENAME_EXT ) );
		}
		referencePath.append( "#/definitions/" );
		
		if (elementName != null) {
			referencePath.append( elementName );
			
		} else {
			referencePath.append( JsonSchemaNamingUtils.getGlobalDefinitionName( referencedEntity ) );
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
	public String getSchemaReferencePath(SchemaDependency schemaDependency, NamedEntity referencingEntity) {
		String referencedFilename = schemaDependency.getSchemaDeclaration().getFilename(
				CodeGeneratorFactory.JSON_SCHEMA_TARGET_FORMAT );
		String referencePath = null;
		
		if (referencedFilename != null) {
			String builtInLocation = XsdCodegenUtils.getBuiltInSchemaOutputLocation( context.getCodegenContext() );
			
			referencePath = builtInLocation + referencedFilename
					+ "#/definitions/" + schemaDependency.getLocalName();
		}
		return referencePath;
	}
	
}
