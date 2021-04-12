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

package org.opentravel.schemacompiler.codegen.json.facet;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.CorrelatedCodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.codegen.json.AbstractJsonSchemaTransformer;
import org.opentravel.schemacompiler.codegen.json.JsonSchemaCodegenUtils;
import org.opentravel.schemacompiler.codegen.json.JsonTypeNameBuilder;
import org.opentravel.schemacompiler.codegen.json.model.JsonDiscriminator;
import org.opentravel.schemacompiler.codegen.json.model.JsonDiscriminator.DiscriminatorFormat;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLMemberFieldOwner;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.version.Versioned;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Base class for facet code generation delegates used to generate code artifacts for <code>TLFacet</code> model
 * elements.
 */
public class TLFacetJsonSchemaDelegate extends FacetJsonSchemaDelegate<TLFacet> {

    public static final String DISCRIMINATOR_PROPERTY = "@type";

    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet the source facet
     */
    public TLFacetJsonSchemaDelegate(TLFacet sourceFacet) {
        super( sourceFacet );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.facet.FacetJsonSchemaDelegate#generateArtifacts()
     */
    @Override
    public CorrelatedCodegenArtifacts generateArtifacts() {
        CorrelatedCodegenArtifacts artifacts = new CorrelatedCodegenArtifacts();

        if (hasContent()) {
            JsonSchemaNamedReference facetDef = createDefinition();
            JsonSchemaNamedReference nonSubFacetDef =
                hasNonSubstitutableElement() ? createDefinition( null, true ) : null;
            TLFacet sourceFacet = getSourceFacet();

            if (facetDef != null) {
                artifacts.addArtifact( sourceFacet, facetDef );
                artifacts.addArtifact( sourceFacet, createGlobalElement( null, facetDef.getName() ) );
            }
            if (nonSubFacetDef != null) {
                artifacts.addArtifact( sourceFacet, nonSubFacetDef );
                artifacts.addArtifact( sourceFacet, createGlobalElement( null, nonSubFacetDef.getName() ) );
            }

            for (TLAlias alias : sourceFacet.getAliases()) {
                JsonSchemaNamedReference aliasDef = createDefinition( alias );
                JsonSchemaNamedReference nonSubAliasDef =
                    hasNonSubstitutableElement() ? createDefinition( alias, true ) : null;

                if (aliasDef != null) {
                    artifacts.addArtifact( alias, aliasDef );
                    artifacts.addArtifact( alias, createGlobalElement( alias, aliasDef.getName() ) );
                }
                if (nonSubAliasDef != null) {
                    artifacts.addArtifact( alias, nonSubAliasDef );
                    artifacts.addArtifact( alias, createGlobalElement( alias, nonSubAliasDef.getName() ) );
                }
            }
        }
        return artifacts;
    }

    /**
     * Creates the JSON definiton for the facet or the alias if one is specified.
     * 
     * @param alias the facet alias for which to generate a definition
     * @return JsonSchemaNamedReference
     */
    protected JsonSchemaNamedReference createDefinition(TLAlias alias) {
        return createDefinition( alias, false );
    }

    /**
     * Creates the JSON definiton for the facet or the alias if one is specified.
     * 
     * @param alias the facet alias for which to generate a definition
     * @param useNonSubstitutableName flag indicating whether the definition's non-substitutable name should be used
     * @return JsonSchemaNamedReference
     */
    protected JsonSchemaNamedReference createDefinition(TLAlias alias, boolean useNonSubstitutableName) {
        TLFacet sourceFacet = getSourceFacet();
        TLFacet baseFacet = getLocalBaseFacet();
        SchemaDependency baseFacetDependency = getLocalBaseFacetDependency();
        JsonSchemaNamedReference definition = new JsonSchemaNamedReference();
        JsonSchema localFacetSchema = new JsonSchema();
        JsonSchema facetSchema;

        if (useNonSubstitutableName) {
            definition.setName( getNonSubstitableElementName( alias ) );

        } else {
            definition.setName( getDefinitionName( (alias != null) ? alias : sourceFacet ) );
        }

        if (baseFacet != null) {
            TLAlias baseAlias = (alias == null) ? null : AliasCodegenUtils.getOwnerAlias( alias );
            NamedEntity baseType = (alias == null) ? baseFacet : baseAlias;
            JsonSchemaReference baseSchemaRef =
                new JsonSchemaReference( jsonUtils.getSchemaDefinitionPath( baseType, sourceFacet ) );

            facetSchema = new JsonSchema();
            facetSchema.getAllOf().add( baseSchemaRef );
            facetSchema.getAllOf().add( new JsonSchemaReference( localFacetSchema ) );

        } else if (baseFacetDependency != null) {
            JsonSchemaReference baseSchemaRef =
                new JsonSchemaReference( jsonUtils.getSchemaReferencePath( baseFacetDependency, sourceFacet ) );

            facetSchema = new JsonSchema();
            facetSchema.getAllOf().add( baseSchemaRef );
            facetSchema.getAllOf().add( new JsonSchemaReference( localFacetSchema ) );
            addCompileTimeDependency( baseFacetDependency );

        } else {
            CodeGenerationContext context = getTransformerFactory().getContext().getCodegenContext();
            DiscriminatorFormat discriminatorFormat = (context == null) ? DiscriminatorFormat.OPENAPI
                : DiscriminatorFormat.valueOf( context.getValue( CodeGenerationContext.CK_JSON_DISCRIMINATOR_FORMAT ) );
            JsonSchemaNamedReference discriminatorProperty = createDiscriminatorProperty();
            JsonDiscriminator discriminator = new JsonDiscriminator();

            discriminator.setPropertyName( discriminatorProperty.getName() );
            discriminator.setFormat( discriminatorFormat );
            localFacetSchema.getProperties().add( discriminatorProperty );
            localFacetSchema.setDiscriminator( discriminator );
            definition.setSchema( new JsonSchemaReference( localFacetSchema ) );
            facetSchema = localFacetSchema;
        }

        if (sourceFacet.getOwningEntity() instanceof TLEquivalentOwner) {
            facetSchema.getEquivalentItems()
                .addAll( jsonUtils.getEquivalentInfo( (TLEquivalentOwner) sourceFacet.getOwningEntity() ) );
        }

        facetSchema.setDocumentation( createJsonDocumentation( sourceFacet ) );
        facetSchema.setEntityInfo( jsonUtils.getEntityInfo( FacetCodegenUtils.getTopLevelOwner( sourceFacet ) ) );
        definition.setSchema( new JsonSchemaReference( facetSchema ) );

        localFacetSchema.getProperties().addAll( createDefinitions() );

        if (hasExtensionPoint()) {
            JsonSchemaNamedReference extensionPoint = getExtensionPointProperty();

            if (extensionPoint != null) {
                localFacetSchema.getProperties().add( extensionPoint );
            }
        }
        return definition;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.facet.FacetJsonSchemaDelegate#createDefinition()
     */
    @Override
    protected JsonSchemaNamedReference createDefinition() {
        return createDefinition( null );
    }

    /**
     * Constructs a global definition in the form of a single-property schema of the type specified by the global
     * element and type names provided.
     * 
     * @param alias the alias associated with the the global element
     * @param typeName the name of the type to reference in the #/definitions section of the local schema
     * @return JsonSchemaReference
     */
    protected JsonSchemaReference createGlobalElement(TLAlias alias, String typeName) {
        JsonSchemaReference globalElement = null;
        QName globalElementName;

        if (alias == null) {
            globalElementName = XsdCodegenUtils.getGlobalElementName( getSourceFacet() );

        } else {
            globalElementName = XsdCodegenUtils.getGlobalElementName( alias );
        }

        if (globalElementName != null) {
            JsonSchemaNamedReference globalDef = new JsonSchemaNamedReference();
            JsonSchema globalDefSchema = new JsonSchema();

            globalDef.setName( globalElementName.getLocalPart() );
            globalDef.setRequired( true );

            globalDef.setSchema(
                new JsonSchemaReference( jsonUtils.getSchemaDefinitionPath( getSourceFacet(), getSourceFacet() ) ) );
            globalDefSchema.getProperties().add( globalDef );
            globalElement = new JsonSchemaReference( globalDefSchema );
        }
        return globalElement;
    }

    /**
     * If the value returned from '<code>getLocalBaseFacet()</code>' is null, this method may return an alternative in
     * the form of a <code>SchemaDependency</code> object. By default, this method returns null; subclasses may override
     * for facet-specific configurations.
     * 
     * @return SchemaDependency
     */
    protected SchemaDependency getLocalBaseFacetDependency() {
        return null;
    }

    /**
     * Creates the discriminator property for a JSON definition that may be used to support inheritance and polymorphism
     * in JSON messages.
     * 
     * @return JsonSchemaNamedReference
     */
    protected JsonSchemaNamedReference createDiscriminatorProperty() {
        JsonSchemaNamedReference discriminator = new JsonSchemaNamedReference();
        JsonSchema schema = new JsonSchema();

        discriminator.setRequired( true );
        discriminator.setName( DISCRIMINATOR_PROPERTY );
        discriminator.setSchema( new JsonSchemaReference( schema ) );
        schema.setType( JsonType.JSON_STRING );
        return discriminator;
    }

    /**
     * Constructs the list of <code>JsonSchemaNamedReference</code> definitions that are based on OTM attributes and
     * non-element indicators.
     * 
     * @return List&lt;JsonSchemaNamedReference&gt;
     */
    protected List<JsonSchemaNamedReference> createDefinitions() {
        ObjectTransformer<TLAttribute,CodegenArtifacts,CodeGenerationTransformerContext> attributeTransformer =
            getTransformerFactory().getTransformer( TLAttribute.class, CodegenArtifacts.class );
        ObjectTransformer<TLProperty,JsonSchemaNamedReference,CodeGenerationTransformerContext> elementTransformer =
            getTransformerFactory().getTransformer( TLProperty.class, JsonSchemaNamedReference.class );
        ObjectTransformer<TLIndicator,JsonSchemaNamedReference,CodeGenerationTransformerContext> indicatorTransformer =
            getTransformerFactory().getTransformer( TLIndicator.class, JsonSchemaNamedReference.class );
        List<JsonSchemaNamedReference> definitions = new ArrayList<>();
        Map<String,List<DefinitionTypePair>> definitionMap = new HashMap<>();
        boolean hasNameConflicts = false;

        setMemberFieldOwner( getSourceFacet() );

        for (TLMemberField<?> field : getMemberFields()) {
            if (field instanceof TLAttribute) {
                definitions.addAll( attributeTransformer.transform( (TLAttribute) field )
                    .getArtifactsOfType( JsonSchemaNamedReference.class ) );

            } else if (field instanceof TLProperty) {
                TLPropertyType propertyType =
                    getLatestMinorVersion( PropertyCodegenUtils.resolvePropertyType( ((TLProperty) field).getType() ) );
                JsonSchemaNamedReference definition = elementTransformer.transform( (TLProperty) field );
                List<DefinitionTypePair> definitionTypeList =
                    definitionMap.computeIfAbsent( definition.getName(), n -> new ArrayList<DefinitionTypePair>() );

                definitions.add( definition );
                definitionTypeList.add( new DefinitionTypePair( definition, propertyType ) );
                hasNameConflicts |= (definitionTypeList.size() > 1);

            } else if (field instanceof TLIndicator) {
                definitions.add( indicatorTransformer.transform( (TLIndicator) field ) );
            }
        }
        if (hasNameConflicts) {
            handleElementNameConflicts( definitionMap,
                new JsonTypeNameBuilder( getSourceFacet().getOwningModel(), null ) );
        }
        setMemberFieldOwner( null );

        return definitions;
    }

    /**
     * Scans the map of field definitions and resolves any conflicts due to duplicate names.
     * 
     * @param definitionMap the map of field names to lists of field definitions
     */
    private void handleElementNameConflicts(Map<String,List<DefinitionTypePair>> definitionMap,
        JsonTypeNameBuilder nameBuilder) {
        Map<String,List<DefinitionTypePair>> secondPassMap = new HashMap<>();

        // For the first pass, use the type name builder to deconflict the name collisions
        for (List<DefinitionTypePair> definitionList : definitionMap.values()) {
            if (definitionList.size() > 1) {
                // Attempt to fix any name conflicts
                for (DefinitionTypePair defType : definitionList) {
                    defType.getJsonDefinition().setName( nameBuilder.getJsonTypeName( defType.getOtmType() ) );
                }
            }

            // Add definitions to the second-pass list
            for (DefinitionTypePair defType : definitionList) {
                List<DefinitionTypePair> definitionTypeList = secondPassMap
                    .computeIfAbsent( defType.getJsonDefinition().getName(), n -> new ArrayList<DefinitionTypePair>() );

                definitionTypeList.add( defType );
            }
        }

        // The second pass handles name collisions that occur when multiple elements have
        // the same name. XML handles this through element ordering (with UPA violation detection), but
        // JSON does not enforce element ordering so name conflicts can still occur.
        for (List<DefinitionTypePair> definitionList : secondPassMap.values()) {
            if (definitionList.size() > 1) {
                int count = 1;

                for (DefinitionTypePair defType : definitionList) {
                    JsonSchemaNamedReference definition = defType.getJsonDefinition();

                    if (count > 1) {
                        definition.setName( definition.getName() + count );
                    }
                    count++;
                }
            }
        }
    }

    /**
     * If the given entity is <code>Versioned</code> this method will return the latest minor version of that entity. If
     * the entity is not versioned, the original entity will be returned.
     * 
     * @param entity the entity for which to return the latest minor version
     * @return E
     */
    @SuppressWarnings("unchecked")
    private <E extends NamedEntity> E getLatestMinorVersion(E entity) {
        E lmvEntity = entity;

        if (entity instanceof Versioned) {
            lmvEntity = (E) JsonSchemaCodegenUtils.getLatestMinorVersion( (Versioned) entity );
        }
        return lmvEntity;
    }

    /**
     * Returns JSON schema documentation for the source facet of this delegate.
     * 
     * @param docOwner the owner for which JSON documentation should be created
     * @return JsonDocumentation
     */
    protected JsonDocumentation createJsonDocumentation(TLDocumentationOwner docOwner) {
        TLDocumentation doc = DocumentationFinder.getDocumentation( docOwner );
        JsonDocumentation jsonDoc = null;

        if (doc != null) {
            ObjectTransformer<TLDocumentation,JsonDocumentation,CodeGenerationTransformerContext> transformer =
                getTransformerFactory().getTransformer( doc, JsonDocumentation.class );

            jsonDoc = transformer.transform( doc );
        }
        return jsonDoc;
    }

    /**
     * Returns the name of the non-substitutable element used to represent the source facet or the specified alias.
     * 
     * @param facetAlias the alias of the source facet element being created (may be null)
     * @return String
     */
    protected final String getNonSubstitableElementName(TLAlias facetAlias) {
        return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() ))
            .getNonSubstitableElementName( facetAlias );
    }

    /**
     * Returns the facet instance that should serve as the base type for the source facet. In some cases (business/core
     * object extension), the facet returned by this method may have a different owner than that of the source facet.
     * 
     * @return TLFacet
     */
    protected final TLFacet getBaseFacet() {
        return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() )).getBaseFacet();
    }

    /**
     * Returns true if the source facet should have a non-substitutable facet in addition to the substitutable one that
     * is created by default.
     * 
     * @return boolean
     */
    protected final boolean hasNonSubstitutableElement() {
        return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() ))
            .hasNonSubstitutableElement();
    }

    /**
     * If the source facet should support an extension point element, this method will return the extension point
     * property to use in the facet's JSON schema definition. If extensions are not supported for the facet, this method
     * will return null.
     * 
     * @return JsonSchemaNamedReference
     */
    protected final JsonSchemaNamedReference getExtensionPointProperty() {
        QName extensionPointName =
            ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() )).getExtensionPointElement();
        JsonSchemaNamedReference extensionPointProperty = null;

        if (extensionPointName != null) {
            String schemaPath = null;

            // Look through all of the schema dependencies to find the one that matches are extension
            // point QName. This is a bit inefficient, but it keeps us from having to replicate all
            // of the extension point logic from the XSD facet delegates.
            for (SchemaDependency dependency : SchemaDependency.getAllDependencies()) {
                if ((extensionPointName.getNamespaceURI().equals( dependency.getSchemaDeclaration().getNamespace() ))
                    && extensionPointName.getLocalPart().equals( dependency.getLocalName() )) {
                    CodeGenerationTransformerContext context = getTransformerFactory().getContext();
                    CodeGenerationContext cgContext = context.getCodegenContext();
                    String builtInLocation = XsdCodegenUtils.getBuiltInSchemaOutputLocation( cgContext );
                    String referencedFilename =
                        dependency.getSchemaDeclaration().getFilename( CodeGeneratorFactory.JSON_SCHEMA_TARGET_FORMAT );

                    if ((referencedFilename != null) && !isLocalNameReferencesEnabled()) {
                        schemaPath = builtInLocation + referencedFilename
                            + JsonSchemaCodegenUtils.getBaseDefinitionsPath( context ) + dependency.getLocalName();
                        addCompileTimeDependency( dependency );
                    } else {
                        schemaPath =
                            JsonSchemaCodegenUtils.getBaseDefinitionsPath( context ) + dependency.getLocalName();
                    }
                }
            }
            extensionPointProperty = new JsonSchemaNamedReference( extensionPointName.getLocalPart(),
                new JsonSchemaReference( schemaPath ) );
        }
        return extensionPointProperty;
    }

    /**
     * Returns the list of all attribute, element, and indicator member fields.
     * 
     * @param <O> the type of the owner for the returned fields
     * @return List&lt;TLMemberField&lt;?&gt;&gt;
     */
    @SuppressWarnings("unchecked")
    protected <O extends TLMemberFieldOwner> List<TLMemberField<O>> getMemberFields() {
        List<TLMemberField<O>> fieldList = new ArrayList<>();

        getAttributes().forEach( f -> fieldList.add( (TLMemberField<O>) f ) );
        getElements().forEach( f -> fieldList.add( (TLMemberField<O>) f ) );
        getIndicators().forEach( f -> fieldList.add( (TLMemberField<O>) f ) );
        return fieldList;
    }

    /**
     * Returns the list of attributes to be generated for the source facet.
     * 
     * @return List&lt;TLAttribute&gt;
     */
    protected final List<TLAttribute> getAttributes() {
        return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() )).getAttributes();
    }

    /**
     * Returns the list of elements (properties) to be generated for the source facet.
     * 
     * @return List&lt;TLProperty&gt;
     */
    protected final List<TLProperty> getElements() {
        return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() )).getElements();
    }

    /**
     * Returns the list of indicators to be generated for the source facet.
     * 
     * @return List&lt;TLIndicator&gt;
     */
    protected final List<TLIndicator> getIndicators() {
        return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() )).getIndicators();
    }

    /**
     * Assigns the given entity as the current member field owner if an owner is not already assigned.
     * 
     * @param fieldOwner the entity to assign as the owner of all contained fields
     */
    protected void setMemberFieldOwner(NamedEntity fieldOwner) {
        CodeGenerationTransformerContext transformContext = getTransformerFactory().getContext();
        Object existingOwner =
            transformContext.getContextCacheEntry( AbstractJsonSchemaTransformer.MEMBER_FIELD_OWNER_KEY );

        if ((fieldOwner == null) || (existingOwner == null)) {
            transformContext.setContextCacheEntry( AbstractJsonSchemaTransformer.MEMBER_FIELD_OWNER_KEY, fieldOwner );
        }
    }

    /**
     * Maintains a pairing of the JSON definition to the original OTM type of the attribute/element from which the
     * definition was created.
     */
    private static class DefinitionTypePair {

        private JsonSchemaNamedReference jsonDefinition;
        private TLPropertyType otmType;

        /**
         * Full constructor.
         * 
         * @param jsonDefinition the JSON field definition
         * @param otmType the original type of the OTM attribute or element used to create the JSON definition
         */
        public DefinitionTypePair(JsonSchemaNamedReference jsonDefinition, TLPropertyType otmType) {
            this.jsonDefinition = jsonDefinition;
            this.otmType = otmType;
        }

        /**
         * Returns the JSON field definition.
         * 
         * @return JsonSchemaNamedReference
         */
        public JsonSchemaNamedReference getJsonDefinition() {
            return jsonDefinition;
        }

        /**
         * Returns the original type of the OTM attribute or element used to create the JSON definition.
         * 
         * @return TLPropertyType
         */
        public TLPropertyType getOtmType() {
            return otmType;
        }

    }

}
