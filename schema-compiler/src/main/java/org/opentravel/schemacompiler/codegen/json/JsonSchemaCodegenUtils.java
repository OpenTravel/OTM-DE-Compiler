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

import org.opentravel.ns.ota2.appinfo_v01_00.OTA2Entity;
import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
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
import org.opentravel.schemacompiler.model.LibraryMember;
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
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.PatchVersionHelper;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.opentravel.schemacompiler.version.Versioned;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * Static utility methods used during the generation of JSON schema output.
 */
public class JsonSchemaCodegenUtils {

    private static final String DESCRIPTION = "description";

    public static final String JSON_SCHEMA_FILENAME_EXT = "schema.json";

    private CodeGenerationTransformerContext context;

    /**
     * Constructor that supplies the current code generation transformer context.
     * 
     * @param context the code generation transformer context
     */
    public JsonSchemaCodegenUtils(CodeGenerationTransformerContext context) {
        this.context = context;
    }

    /**
     * Constructs a JSON schema for the given simple field type.
     * 
     * @param simpleInfo the pre-determined simple type info for the member field (must not be null)
     * @param jsonType the pre-determined JSON type of the member field (must not be null)
     * @return JsonSchema
     */
    public JsonSchema buildSimpleTypeSchema(SimpleTypeInfo simpleInfo, JsonType jsonType) {
        JsonSchema attrSchema = buildSimpleTypeSchema( jsonType );
        JsonSchema itemSchema = null;

        applySimpleTypeConstraints( attrSchema, simpleInfo );

        // Identify special cases where the simple type is actually an array of
        // simple types
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
            attrSchema.setType( JsonType.JSON_ARRAY );
            attrSchema.setItems( new JsonSchemaReference( itemSchema ) );
        }
        return attrSchema;
    }

    /**
     * Returns a JSON schema for the given JSON simple type.
     * 
     * @param jsonType the JSON simple type for which to return a schema
     * @return JsonSchema
     */
    public JsonSchema buildSimpleTypeSchema(JsonType jsonType) {
        JsonSchema schema = new JsonSchema();

        schema.setType( jsonType );

        // Special case for IDREFS; schema is an array of strings
        if (jsonType == JsonType.JSON_REFS) {
            JsonSchema itemSchema = new JsonSchema();

            schema.setType( JsonType.JSON_ARRAY );
            itemSchema.setType( JsonType.JSON_STRING );
            schema.setItems( new JsonSchemaReference( itemSchema ) );
        }
        return schema;
    }

    /**
     * Adds documentation to the given schema componnent that describes the original OTM type for an element or
     * attribute.
     * 
     * @param docOwner the JSON schema component to which documentation should be added
     * @param fieldType the original type assigned to the attribute, element, or VWA value
     */
    public void applySimpleTypeDocumentation(JsonDocumentationOwner docOwner, NamedEntity fieldType) {
        if ((fieldType != null) && !fieldType.getNamespace().equals( XMLConstants.W3C_XML_SCHEMA_NS_URI )) {
            applySupplementalDescription( docOwner, getAssignedTypeLabel( fieldType ) );
        }
    }

    /**
     * Returns the assigned type label that should be included in the JSON documentation for OTM simple types.
     * 
     * @param fieldType the type assigned to the attribute, element, or VWA value
     * @return String
     */
    private String getAssignedTypeLabel(NamedEntity fieldType) {
        AbstractLibrary owningLibrary = fieldType.getOwningLibrary();
        String prefix = (owningLibrary == null) ? "" : owningLibrary.getPrefix() + ":";

        return "Assigned Type: " + prefix + fieldType.getLocalName();
    }

    /**
     * Applies the OTM simple type constraints provided to the given JSON schema.
     * 
     * @param schema the JSON schema to which the type constraints will be applied
     * @param simpleInfo the simple type constraint information to apply
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
     * Applies an additional line of documentation to the given schema component's list of descriptions. If then given
     * doc-owner does not yet contain any documentation, it will be created automatically.
     * 
     * @param docOwner the schema componnent to which the supplemental description will be applied
     * @param supplementalDescription the text of the supplemental description
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
        schemaDoc.setDescriptions( descriptions.toArray( new String[descriptions.size()] ) );
    }

    /**
     * Returns the JSON schema information for the given OTM library.
     * 
     * @param library the OTM library instance for which to return info
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
     * @param resource the OTM resource instance for which to return info
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
     * @param entity the OTM library instance for which to return info
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
     * @param entity the entity for which to equivalent EXAMPLE values
     * @return List&lt;JsonContextualValue&gt;
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
     * Returns the list of EXAMPLE values for the JSON schema documentation.
     * 
     * @param entity the entity for which to return EXAMPLE values
     * @return List&lt;JsonContextualValue&gt;
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
     * @param referencedEntity the named entity for which to return a reference
     * @param referencingEntity the named entity which owns the reference
     * @return String
     */
    public String getSchemaDefinitionPath(NamedEntity referencedEntity, NamedEntity referencingEntity) {
        StringBuilder referencePath = buildSchemaPath( referencedEntity, referencingEntity );
        JsonTypeNameBuilder typeNameBuilder = getTypeNameBuilder();

        if (typeNameBuilder != null) {
            referencePath.append( typeNameBuilder.getJsonTypeName( referencedEntity ) );
        } else {
            referencePath.append( JsonSchemaNamingUtils.getGlobalDefinitionName( referencedEntity ) );
        }
        return referencePath.toString();
    }

    /**
     * Returns a relative path reference to the JSON schema definition of the given named entity.
     * 
     * @param referencedEntity the named entity for which to return a reference
     * @param referencingEntity the named entity which owns the reference
     * @return String
     */
    public String getSchemaReferencePath(NamedEntity referencedEntity, NamedEntity referencingEntity) {
        StringBuilder referencePath = buildSchemaPath( referencedEntity, referencingEntity );
        JsonTypeNameBuilder typeNameBuilder = getTypeNameBuilder();

        if (typeNameBuilder != null) {
            referencePath.append( typeNameBuilder.getJsonReferenceName( referencedEntity ) );
        } else {
            referencePath.append( JsonSchemaNamingUtils.getGlobalReferenceName( referencedEntity ) );
        }
        return referencePath.toString();
    }

    /**
     * Constructs the path to the given referenced entity. The string builder that is returned contains the path leading
     * up to, but not including, the entity name itself.
     * 
     * @param referencedEntity the named entity for which to return a path
     * @param referencingEntity the named entity which owns the reference
     * @return StringBuilder
     */
    @SuppressWarnings("unchecked")
    private StringBuilder buildSchemaPath(NamedEntity referencedEntity, NamedEntity referencingEntity) {
        AbstractLibrary referencedLibrary =
            (referencedEntity == null) ? null : getLatestMinorVersion( referencedEntity.getOwningLibrary() );
        AbstractLibrary referencingLibrary =
            (referencingEntity == null) ? null : getLatestMinorVersion( referencingEntity.getOwningLibrary() );
        JsonTypeNameBuilder typeNameBuilder = getTypeNameBuilder();
        StringBuilder referencePath = new StringBuilder();

        if ((typeNameBuilder == null) && ((referencingEntity == null) || (referencedLibrary != referencingLibrary))) {
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
            referencePath
                .append( buildLatestMinorVersionFilename( referencedEntity.getOwningLibrary(), filenameBuilder ) );
        }

        if (referencePath.length() != 0) {
            referencePath.append( "#/definitions/" );
        } else {
            referencePath.append( getBaseDefinitionsPath( context ) );
        }
        return referencePath;
    }

    /**
     * Returns the filename of the library's latest minor version using the builder provided.
     * 
     * @param library the library whose latest minor version filename should be returned
     * @param filenameBuilder the filename builder to use when constructing the filename
     * @return String
     */
    private String buildLatestMinorVersionFilename(AbstractLibrary library,
        CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder) {
        AbstractLibrary latestMinorVersion = getLatestMinorVersion( library );

        return filenameBuilder.buildFilename( latestMinorVersion, JsonSchemaCodegenUtils.JSON_SCHEMA_FILENAME_EXT );
    }

    /**
     * Returns a relative path reference to the JSON schema definition of the given schema dependency. If the referenced
     * type does not have an associated JSON definition, this method will return null.
     * 
     * @param schemaDependency the schema dependency for which to return a reference
     * @param referencingEntity the named entity which owns the reference
     * @return String
     */
    public String getSchemaReferencePath(SchemaDependency schemaDependency, NamedEntity referencingEntity) {
        String referencedFilename =
            schemaDependency.getSchemaDeclaration().getFilename( CodeGeneratorFactory.JSON_SCHEMA_TARGET_FORMAT );
        String referencePath = null;

        if (referencedFilename != null) {
            referencePath = XsdCodegenUtils.getBuiltInSchemaOutputLocation( context.getCodegenContext() )
                + referencedFilename + "#/definitions/" + schemaDependency.getLocalName();

        } else {
            referencePath = getBaseDefinitionsPath( context ) + schemaDependency.getLocalName();
        }
        return referencePath;
    }

    /**
     * Returns a relative path reference to the XML schema definition of the given named entity.
     * 
     * @param referencedEntity the named entity for which to return a reference
     * @return String
     */
    public String getXmlSchemaReferencePath(NamedEntity referencedEntity) {
        CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder = new LibraryTrimmedFilenameBuilder( null );
        StringBuilder referencePath = new StringBuilder();
        QName elementName = null;
        String entityName = null;

        if ((referencedEntity instanceof TLPropertyType)
            && PropertyCodegenUtils.hasGlobalElement( (TLPropertyType) referencedEntity )) {

            if ((referencedEntity instanceof TLAlias)
                && (((TLAlias) referencedEntity).getOwningEntity() instanceof TLFacet)) {
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
     * If the context has been configured for single-file swagger generation, this method will return the shared
     * <code>JsonTypeNameBuilder</code>. Otherwise, null will be returned.
     * 
     * @return JsonTypeNameBuilder
     */
    public JsonTypeNameBuilder getTypeNameBuilder() {
        return (JsonTypeNameBuilder) context.getContextCacheEntry( JsonTypeNameBuilder.class.getSimpleName() );
    }

    /**
     * Returns the base JSON schema path to the 'definitions' section of a Swagger document (or the 'components' section
     * for OpenAPI documents).
     * 
     * @param context the code generationn transformer context
     * @return String
     */
    public static String getBaseDefinitionsPath(CodeGenerationTransformerContext context) {
        CodeGenerationContext cgContext = (context == null) ? null : context.getCodegenContext();
        String baseDefsPath = "#/definitions/";

        if (cgContext != null) {
            baseDefsPath = cgContext.getValue( CodeGenerationContext.CK_BASE_DEFINITIONS_PATH );
        }
        return baseDefsPath;
    }

    /**
     * Returns true if the given library is assigned to a patch version.
     * 
     * @param library the library to check for a patch version assignment
     * @return boolean
     */
    public static boolean isPatchVersion(TLLibrary library) {
        boolean isPatch = false;
        try {
            VersionScheme versionScheme =
                VersionSchemeFactory.getInstance().getVersionScheme( library.getVersionScheme() );

            isPatch = versionScheme.isPatchVersion( library.getNamespace() );

        } catch (VersionSchemeException e) {
            // No action - ignore error and return false
        }
        return isPatch;
    }

    /**
     * Returns true if the given library is the latest in its minor version chain or a patch of the latest minor
     * version.
     * 
     * @param library the library to check for being the latest minor version
     * @return boolean
     * @throws CodeGenerationException thrown if the library's version scheme is invalid
     */
    public static boolean isLatestMinorVersion(TLLibrary library) {
        boolean isLMV = false;
        try {
            MinorVersionHelper mvHelper = new MinorVersionHelper();

            if (!isPatchVersion( library )) {
                isLMV = mvHelper.getLaterMinorVersions( library ).isEmpty();
            }

        } catch (VersionSchemeException e) {
            // Ignore error and return false
        }
        return isLMV;
    }

    /**
     * Returns true if the given entity is the latest minor version of its major version chain.
     * 
     * @param entity the entity for which to perform the latest minor version check
     * @return boolean
     * @throws CodeGenerationException thrown if the library's version scheme is invalid
     */
    public static boolean isLatestMinorVersion(Versioned entity) {
        boolean isLMV = false;
        try {
            isLMV = new MinorVersionHelper().getLaterMinorVersions( entity ).isEmpty();

        } catch (VersionSchemeException e) {
            // Ignore error and return false
        }
        return isLMV;
    }

    /**
     * Returns true if the given library is the latest in its minor version chain or a patch of the latest minor
     * version.
     * 
     * @param library the library for which to return the latest minor version
     * @return L
     */
    @SuppressWarnings("unchecked")
    public static <L extends AbstractLibrary> L getLatestMinorVersion(L library) {
        L lmvLibrary = library;
        try {
            if (library instanceof TLLibrary) {
                TLLibrary lib = (TLLibrary) library;
                MinorVersionHelper mvHelper = new MinorVersionHelper();
                List<TLLibrary> laterMinorVersions;

                if (isPatchVersion( lib )) {
                    lib = mvHelper.getPriorMinorVersion( lib );
                }
                laterMinorVersions = mvHelper.getLaterMinorVersions( lib );

                if (!laterMinorVersions.isEmpty()) {
                    lmvLibrary = (L) laterMinorVersions.get( laterMinorVersions.size() - 1 );
                }
            }

        } catch (VersionSchemeException e) {
            // Ignore error and return the original library
        }
        return lmvLibrary;
    }

    /**
     * Returns true if the given library is the latest in its minor version chain or a patch of the latest minor
     * version.
     * 
     * @param library the library for which to return the latest minor version
     * @return TLLibrary
     */
    public static <V extends Versioned> V getLatestMinorVersion(V entity) {
        V lmvEntity = entity;
        try {
            MinorVersionHelper mvHelper = new MinorVersionHelper();
            List<V> laterMinorVersions = mvHelper.getLaterMinorVersions( entity );

            if (!laterMinorVersions.isEmpty()) {
                lmvEntity = laterMinorVersions.get( laterMinorVersions.size() - 1 );
            }

        } catch (VersionSchemeException e) {
            // Ignore error and return the original library
        }
        return lmvEntity;
    }

    /**
     * Returns the latest minor version of each library member from the given library and all of its prior minor
     * versions.
     * 
     * @param library the library from which to return its latest minor version members
     * @return List&lt;LibraryMember&gt;
     */
    public static List<LibraryMember> getLatestMinorVersionMembers(TLLibrary library) {
        List<LibraryMember> lmvMembers = new ArrayList<>();

        if (!isPatchVersion( library )) {
            MinorVersionHelper mvHelper = new MinorVersionHelper();
            PatchVersionHelper patchHelper = new PatchVersionHelper();
            TLLibrary lib = library;

            // Collect members from the latest and all prior minor versions
            while (lib != null) {
                for (LibraryMember member : lib.getNamedMembers()) {
                    try {
                        if (member instanceof Versioned) {
                            if (mvHelper.getLaterMinorVersions( (Versioned) member ).isEmpty()) {
                                lmvMembers.add( member );
                            }

                        } else {
                            lmvMembers.add( member );
                        }

                    } catch (VersionSchemeException e) {
                        // Ignore error and assume the object is an LMV
                        lmvMembers.add( member );
                    }
                }
                try {
                    lib = mvHelper.getPriorMinorVersion( lib );

                } catch (VersionSchemeException e) {
                    // Ignore error and break out of the loop
                    lib = null;
                }
            }

            // Collect members from all patch libraries of the latest minor version
            try {
                for (TLLibrary patchLib : patchHelper.getLaterPatchVersions( library )) {
                    lmvMembers.addAll( patchLib.getNamedMembers() );
                }

            } catch (VersionSchemeException e) {
                // Should never happen by the time we get to code generation (ignore and omit patch members)
            }

        } else {
            lmvMembers.addAll( library.getNamedMembers() );
        }
        return lmvMembers;
    }

    /**
     * Shared method that constructs the JSON structures for the 'x-otm-annotations' element of a schema. If no
     * annotations are required, this method will return with no action.
     * 
     * @param targetJson the target JSON document to which the annotation element will be applied
     * @param docOwner the JSON documentation owner from which to obtain the documentation content
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
                if (documentation.hasDescription()) {
                    setJsonSchemaDescription( jsonDoc, targetJson );
                }
                if (!jsonDoc.entrySet().isEmpty()) {
                    otmAnnotations.add( "documentation", jsonDoc );
                    hasOtmAnnotation = true;
                }
            }
            hasOtmAnnotation =
                addExampleAndEquivalentAnnotations( exampleItems, equivalentItems, otmAnnotations ) || hasOtmAnnotation;

            if (hasOtmAnnotation) {
                targetJson.add( "x-otm-annotations", otmAnnotations );
            }
        }
    }

    /**
     * Since the 'description' field is supported by the JSON schema spec, we will move that property from the
     * 'x-otm-documentation' element to the main schema properties.
     * 
     * @param jsonDoc the JSON object containing the OTM model documentation fields
     * @param targetJson the target JSON schema to which the description will be assigned
     */
    private static void setJsonSchemaDescription(JsonObject jsonDoc, JsonObject targetJson) {
        JsonElement jsonDesc = jsonDoc.get( DESCRIPTION );
        String firstDescription;

        if (jsonDesc instanceof JsonArray) {
            JsonArray descList = (JsonArray) jsonDesc;
            firstDescription = descList.remove( 0 ).getAsString();

            if (descList.size() == 1) {
                jsonDoc.remove( DESCRIPTION );
                targetJson.addProperty( DESCRIPTION, descList.get( 0 ).getAsString() );
            }
        } else {
            firstDescription = jsonDesc.getAsString();
            jsonDoc.remove( DESCRIPTION );
        }
        targetJson.addProperty( DESCRIPTION, firstDescription );
    }

    /**
     * Adds annotation entries for the given example and equivalent items.
     * 
     * @param exampleItems the JSON content for OTM examples
     * @param equivalentItems the JSON content for OTM equivalents
     * @param otmAnnotations the JSON schema object representing the OTM annotations
     * @return boolean
     */
    private static boolean addExampleAndEquivalentAnnotations(List<JsonContextualValue> exampleItems,
        List<JsonContextualValue> equivalentItems, JsonObject otmAnnotations) {
        boolean hasOtmAnnotation = false;

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
        return hasOtmAnnotation;
    }

    /**
     * Parses the given numeric string and returns a <code>Number</code>.
     * 
     * @param numStr the numeric string to parse
     * @return Number
     */
    public static Number parseNumber(String numStr) {
        Number result = null;

        try {
            result = Integer.parseInt( numStr );
        } catch (NumberFormatException e) {
            // Ignore exception and return null
        }

        try {
            if (result == null) {
                result = Double.parseDouble( numStr );
            }
        } catch (NumberFormatException e) {
            // Ignore exception and return null
        }
        return result;
    }

    /**
     * Recursively processes the given JSON document, removing all properties whose names begin with 'x-otm-'.
     * 
     * @param jsonDocument the JSON document to be processed
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

                if (jProperty.getKey().startsWith( "x-otm-" ) || jProperty.getKey().startsWith( "x-xml-" )) {
                    iterator.remove();

                } else {
                    stripOtmExtensions( jProperty.getValue() );
                }
            }
        }
    }

}
