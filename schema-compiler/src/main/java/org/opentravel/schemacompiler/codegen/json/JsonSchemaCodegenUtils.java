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
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.opentravel.ns.ota2.appinfo_v01_00.OTA2Entity;
import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.LibraryTrimmedFilenameBuilder;
import org.opentravel.schemacompiler.codegen.json.model.JsonContextualValue;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentationOwner;
import org.opentravel.schemacompiler.codegen.json.model.JsonEntityInfo;
import org.opentravel.schemacompiler.codegen.json.model.JsonLibraryInfo;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.util.JsonSchemaNamingUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.util.SchemaCompilerInfo;
import org.opentravel.schemacompiler.util.SimpleTypeInfo;
import org.opentravel.schemacompiler.util.URLUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
	 * Constructs a JSON schema for the given simple field type.
	 * 
	 * @param simpleInfo  the pre-determined simple type info for the member field (must not be null)
	 * @param jsonType  the pre-determined JSON type of the member field (must not be null)
	 * @return JsonSchema
	 */
	public JsonSchema buildSimpleTypeSchema(SimpleTypeInfo simpleInfo, JsonType jsonType) {
    	JsonSchema attrSchema = buildSimpleTypeSchema( jsonType );
    	JsonSchema itemSchema = null;
    	
		applySimpleTypeConstraints( attrSchema, simpleInfo );
		
		// Identify special cases where the simple type is actually an array of simple types
		if (simpleInfo.getOriginalSimpleType() instanceof TLSimple) {
			TLSimple simpleType = (TLSimple) simpleInfo.getOriginalSimpleType();
			
			if (simpleType.isListTypeInd()) {
				itemSchema = attrSchema;
			}
			
		} else if (simpleInfo.getOriginalSimpleType() instanceof TLListFacet) {
			TLListFacet listFacet = (TLListFacet) simpleInfo.getOriginalSimpleType();
			
			if (listFacet.getFacetType() == TLFacetType.SIMPLE) {
				itemSchema = attrSchema;
			}
		}
		
		if (itemSchema != null) {
			attrSchema = new JsonSchema();
			attrSchema.setType( JsonType.jsonArray );
			attrSchema.setItems( new JsonSchemaReference( itemSchema ) );
		}
		return attrSchema;
	}
	
	/**
	 * Adds documentation to the given schema componnent that describes the original OTM type for an
	 * element or attribute.
	 * 
	 * @param docOwner  the JSON schema component to which documentation should be added
	 * @param fieldType  the original type assigned to the attribute, element, or VWA value
	 */
	public void applySimpleTypeDocumentation(JsonDocumentationOwner docOwner, NamedEntity fieldType) {
		if ((fieldType != null) && !fieldType.getNamespace().equals( XMLConstants.W3C_XML_SCHEMA_NS_URI )) {
			applySupplementalDescription( docOwner, getAssignedTypeLabel( fieldType ) );
		}
	}
	
	/**
	 * Returns the assigned type label that should be included in the JSON documentation
	 * for OTM simple types.
	 * 
	 * @param fieldType  the type assigned to the attribute, element, or VWA value
	 * @return String
	 */
	private String getAssignedTypeLabel(NamedEntity fieldType) {
		AbstractLibrary owningLibrary = fieldType.getOwningLibrary();
		String prefix = (owningLibrary == null) ? "" : owningLibrary.getPrefix() + ":";
		
		return "Assigned Type: " + prefix + fieldType.getLocalName();
	}
	
	/**
	 * Returns a JSON schema for the given JSON simple type.
	 * 
	 * @param jsonType  the JSON simple type for which to return a schema
	 * @return JsonSchema
	 */
	public JsonSchema buildSimpleTypeSchema(JsonType jsonType) {
    	JsonSchema schema = new JsonSchema();
		
		schema.setType( jsonType );
		
		// Special case for IDREFS; schema is an array of strings
    	if (jsonType == JsonType.jsonRefs) {
        	JsonSchema itemSchema = new JsonSchema();
    		
        	schema.setType( JsonType.jsonArray );
        	itemSchema.setType( JsonType.jsonString );
        	schema.setItems( new JsonSchemaReference( itemSchema ));
    	}
    	return schema;
	}
	
	/**
	 * Applies the OTM simple type constraints provided to the given JSON schema.
	 * 
	 * @param schema  the JSON schema to which the type constraints will be applied
	 * @param simpleInfo  the simple type constraint information to apply
	 * @return JsonSchema
	 */
	public void applySimpleTypeConstraints(JsonSchema schema, SimpleTypeInfo simpleInfo) {
        if (simpleInfo.getMinLength() > 0) {
        	schema.setMinLength( simpleInfo.getMinLength() );
        }
        if (simpleInfo.getMaxLength() > 0) {
        	schema.setMaxLength( simpleInfo.getMaxLength() );
        }
        if ((simpleInfo.getPattern() != null) && (simpleInfo.getPattern().length() > 0)) {
        	schema.setPattern( simpleInfo.getPattern() );
        }
        if ((simpleInfo.getMinInclusive() != null) && (simpleInfo.getMinInclusive().length() > 0)) {
        	schema.setMinimum( parseNumber( simpleInfo.getMinInclusive() ) );
        	schema.setExclusiveMinimum( false );
        }
        if ((simpleInfo.getMaxInclusive() != null) && (simpleInfo.getMaxInclusive().length() > 0)) {
        	schema.setMaximum( parseNumber( simpleInfo.getMaxInclusive() ) );
        	schema.setExclusiveMaximum( false );
        }
        if ((simpleInfo.getMinExclusive() != null) && (simpleInfo.getMinExclusive().length() > 0)) {
        	schema.setMinimum( parseNumber( simpleInfo.getMinExclusive() ) );
        	schema.setExclusiveMinimum( true );
        }
        if ((simpleInfo.getMaxExclusive() != null) && (simpleInfo.getMaxExclusive().length() > 0)) {
        	schema.setMaximum( parseNumber( simpleInfo.getMaxExclusive() ) );
        	schema.setExclusiveMaximum( true );
        }
	}
	
	/**
	 * Applies an additional line of documentation to the given schema component's list of
	 * descriptions.  If then given doc-owner does not yet contain any documentation, it will
	 * be created automatically.
	 * 
	 * @param docOwner  the schema componnent to which the supplemental description will be applied
	 * @param supplementalDescription  the text of the supplemental description
	 */
	public void applySupplementalDescription(JsonDocumentationOwner docOwner, String supplementalDescription) {
		JsonDocumentation schemaDoc = docOwner.getDocumentation();
		List<String> descriptions;
		
		if (schemaDoc == null) {
			schemaDoc = new JsonDocumentation();
			docOwner.setDocumentation( schemaDoc );
		}
		descriptions = new ArrayList<>( Arrays.asList( schemaDoc.getDescriptions() ) );
		descriptions.add( 0, supplementalDescription );
		schemaDoc.setDescriptions( descriptions.toArray( new String[ descriptions.size() ] ) );
	}
	
    /**
     * Returns the JSON schema information for the given OTM library.
     * 
     * @param library  the OTM library instance for which to return info
     * @return JsonLibraryInfo
     */
    public JsonLibraryInfo getLibraryInfo(AbstractLibrary library) {
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
     * Returns the JSON schema information for the given OTM resource.
     * 
     * @param resource  the OTM resource instance for which to return info
     * @return JsonLibraryInfo
     */
    public JsonLibraryInfo getResourceInfo(TLResource resource) {
    	JsonLibraryInfo resourceInfo = getLibraryInfo( resource.getOwningLibrary() );
    	
    	resourceInfo.setResourceName( resource.getName() );
    	return resourceInfo;
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
		JsonTypeNameBuilder typeNameBuilder = getTypeNameBuilder();
		StringBuilder referencePath = new StringBuilder();
		
		if ((typeNameBuilder == null) && ((referencingEntity == null) ||
				(referencedEntity.getOwningLibrary() != referencingEntity.getOwningLibrary()))) {
			AbstractCodeGenerator<?> codeGenerator = (AbstractCodeGenerator<?>) context.getCodeGenerator();
			CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder;
			
			if (referencingEntity != null) {
				filenameBuilder = (CodeGenerationFilenameBuilder<AbstractLibrary>) codeGenerator.getFilenameBuilder();
			} else {
				filenameBuilder = new LibraryTrimmedFilenameBuilder( null ); // swagger reference scenario
			}
			
			if (referencedEntity.getOwningLibrary() instanceof BuiltInLibrary) {
				String builtInLocation = XsdCodegenUtils.getBuiltInSchemaOutputLocation( context.getCodegenContext() );
				
				referencePath.append( builtInLocation );
			}
			referencePath.append( filenameBuilder.buildFilename(
					referencedEntity.getOwningLibrary(), JsonSchemaCodegenUtils.JSON_SCHEMA_FILENAME_EXT ) );
		}
		referencePath.append( "#/definitions/" );
		
		if (typeNameBuilder != null) {
			referencePath.append( typeNameBuilder.getJsonTypeName( referencedEntity ) );
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
		JsonTypeNameBuilder typeNameBuilder = getTypeNameBuilder();
		String referencedFilename = schemaDependency.getSchemaDeclaration().getFilename(
				CodeGeneratorFactory.JSON_SCHEMA_TARGET_FORMAT );
		String referencePath = null;
		
		if ((typeNameBuilder == null) && (referencedFilename != null)) {
			String builtInLocation = XsdCodegenUtils.getBuiltInSchemaOutputLocation( context.getCodegenContext() );
			
			referencePath = builtInLocation + referencedFilename
					+ "#/definitions/" + schemaDependency.getLocalName();
		} else {
			referencePath = "#/definitions/" + schemaDependency.getLocalName();
		}
		return referencePath;
	}
	
	/**
	 * Returns a relative path reference to the XML schema definition of the given named entity.
	 * 
	 * @param referencedEntity  the named entity for which to return a reference
	 * @return String
	 */
	public String getXmlSchemaReferencePath(NamedEntity referencedEntity) {
		CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder = new LibraryTrimmedFilenameBuilder( null );
		StringBuilder referencePath = new StringBuilder();
		QName elementName = null;
		String entityName = null;
		
		if ((referencedEntity instanceof TLPropertyType) &&
				PropertyCodegenUtils.hasGlobalElement( (TLPropertyType) referencedEntity )) {
			
			if ((referencedEntity instanceof TLAlias) &&
					(((TLAlias) referencedEntity).getOwningEntity() instanceof TLFacet)) {
				elementName = XsdCodegenUtils.getSubstitutableElementName( (TLAlias) referencedEntity );
			}
			if (referencedEntity instanceof TLFacet) {
				elementName = XsdCodegenUtils.getSubstitutableElementName( (TLFacet) referencedEntity );
			}
		}
		if (elementName == null) {
			elementName = XsdCodegenUtils.getGlobalElementName( referencedEntity );
		}
		if (elementName != null) {
			entityName = elementName.getLocalPart();
		}
		if (entityName == null) {
			entityName = XsdCodegenUtils.getGlobalTypeName( referencedEntity );
		}
		referencePath.append( filenameBuilder.buildFilename( referencedEntity.getOwningLibrary(), "xsd" ) );
		referencePath.append( "#/" ).append( entityName );
		return referencePath.toString();
	}
	
	/**
	 * If the context has been configured for single-file swagger generation, this method
	 * will return the shared <code>JsonTypeNameBuilder</code>.  Otherwise, null will be
	 * returned.
	 * 
	 * @return JsonTypeNameBuilder
	 */
	private JsonTypeNameBuilder getTypeNameBuilder() {
		return (JsonTypeNameBuilder) context.getContextCacheEntry( JsonTypeNameBuilder.class.getSimpleName() );
	}
	
	/**
	 * Shared method that constructs the JSON structures for the 'x-otm-annotations' element
	 * of a schema.  If no annotations are required, this method will return with no action.
	 * 
	 * @param targetJson  the target JSON document to which the annotation element will be applied
	 * @param docOwner  the JSON documentation owner from which to obtain the documentation content
	 */
	public static void createOtmAnnotations(JsonObject targetJson, JsonDocumentationOwner docOwner) {
		JsonDocumentation documentation = docOwner.getDocumentation();
		List<JsonContextualValue> equivalentItems = docOwner.getEquivalentItems();
		List<JsonContextualValue> exampleItems = docOwner.getExampleItems();
		
		if ((documentation != null) || !equivalentItems.isEmpty() || !exampleItems.isEmpty()) {
			JsonObject jsonDoc = (documentation == null) ? null : documentation.toJson();
			JsonObject otmAnnotations = new JsonObject();
			boolean hasOtmAnnotation = false;
			
			if (jsonDoc != null) {
				// Since the 'description' field is supported by the JSON schema spec,
				// we will move that property from the 'x-otm-documentation' element
				// to the main schema properties
				if (documentation.hasDescription()) {
					JsonElement jsonDesc = jsonDoc.get( "description" );
					String firstDescription;
					
					if (jsonDesc instanceof JsonArray) {
						JsonArray descList = (JsonArray) jsonDesc;
						firstDescription = descList.remove( 0 ).getAsString();
						
						if (descList.size() == 1) {
							jsonDoc.remove( "description" );
							targetJson.addProperty( "description", descList.get( 0 ).getAsString() );
						}
					} else {
						firstDescription = jsonDesc.getAsString();
						jsonDoc.remove( "description" );
					}
					targetJson.addProperty( "description", firstDescription );
				}
				if (!jsonDoc.entrySet().isEmpty()) {
					otmAnnotations.add( "documentation", jsonDoc );
					hasOtmAnnotation = true;
				}
			}
			if (!equivalentItems.isEmpty()) {
				JsonArray itemList = new JsonArray();
				
				for (JsonContextualValue item : equivalentItems) {
					itemList.add( item.toJson() );
				}
				otmAnnotations.add( "equivalents", itemList );
				hasOtmAnnotation = true;
			}
			if (!exampleItems.isEmpty()) {
				JsonArray itemList = new JsonArray();
				
				for (JsonContextualValue item : exampleItems) {
					itemList.add( item.toJson() );
				}
				otmAnnotations.add( "examples", itemList );
				hasOtmAnnotation = true;
			}
			if (hasOtmAnnotation) {
				targetJson.add( "x-otm-annotations", otmAnnotations );
			}
		}
	}
	
	/**
	 * Parses the given numeric string and returns a <code>Number</code>.
	 * 
	 * @param numStr  the numeric string to parse
	 * @return Number
	 */
	public static Number parseNumber(String numStr) {
		Number result = null;
		
		try {
			result = Integer.parseInt( numStr );
		} catch (NumberFormatException e) {}
		
		try {
			if (result == null) {
				result = Double.parseDouble( numStr );
			}
		} catch (NumberFormatException e) {}
		
		return result;
	}
	
	/**
	 * Recursively processes the given JSON document, removing all properties whose names
	 * begin with 'x-otm-'.
	 * 
	 * @param jsonDocument  the JSON document to be processed
	 */
	public static void stripOtmExtensions(JsonElement jsonDocument) {
		if (jsonDocument instanceof JsonArray) {
			JsonArray jArray = (JsonArray) jsonDocument;
			
			for (JsonElement arrayMember : jArray) {
				stripOtmExtensions( arrayMember );
			}
			
		} else if (jsonDocument instanceof JsonObject) {
			JsonObject jObject = (JsonObject) jsonDocument;
			Iterator<Entry<String,JsonElement>> iterator = jObject.entrySet().iterator();
			
			while (iterator.hasNext()) {
				Entry<String,JsonElement> jProperty = iterator.next();
				
				if (jProperty.getKey().startsWith( "x-otm-" )) {
					iterator.remove();
					
				} else {
					stripOtmExtensions( jProperty.getValue() );
				}
			}
		}
	}
	
}
