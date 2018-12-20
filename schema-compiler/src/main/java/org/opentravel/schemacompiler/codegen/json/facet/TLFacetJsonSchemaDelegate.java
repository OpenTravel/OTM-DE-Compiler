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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.CorrelatedCodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.codegen.json.AbstractJsonSchemaTransformer;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
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
import org.opentravel.schemacompiler.transform.ObjectTransformer;

/**
 * Base class for facet code generation delegates used to generate code artifacts for
 * <code>TLFacet</code> model elements.
 */
public class TLFacetJsonSchemaDelegate extends FacetJsonSchemaDelegate<TLFacet> {
	
	public static final String DISCRIMINATOR_PROPERTY = "@type";
	
    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet  the source facet
     */
    public TLFacetJsonSchemaDelegate(TLFacet sourceFacet) {
        super(sourceFacet);
    }
    
    /**
	 * @see org.opentravel.schemacompiler.codegen.json.facet.FacetJsonSchemaDelegate#generateArtifacts()
	 */
	@Override
	public CorrelatedCodegenArtifacts generateArtifacts() {
    	CorrelatedCodegenArtifacts artifacts = new CorrelatedCodegenArtifacts();

        if (hasContent()) {
			JsonSchemaNamedReference facetDef = createDefinition();
			TLFacet sourceFacet = getSourceFacet();
			
			if (facetDef != null) {
	            artifacts.addArtifact( sourceFacet, facetDef );
				artifacts.addArtifact( sourceFacet, createGlobalElement( null, facetDef.getName() ) );
			}
    		
    		for (TLAlias alias : sourceFacet.getAliases()) {
    			JsonSchemaNamedReference aliasDef = createDefinition( alias );
    			
    			if (aliasDef != null) {
        			artifacts.addArtifact( alias, aliasDef );
        			artifacts.addArtifact( alias, createGlobalElement( alias, aliasDef.getName() ) );
    			}
    		}
        }
		return artifacts;
	}

    /**
     * Creates the JSON definiton for the facet or the alias if one is specified.
     * 
     * @param alias  the facet alias for which to generate a definition
     * @return JsonSchemaNamedReference
     */
    protected JsonSchemaNamedReference createDefinition(TLAlias alias) {
        TLFacet sourceFacet = getSourceFacet();
        TLFacet baseFacet = getLocalBaseFacet();
        SchemaDependency baseFacetDependency = getLocalBaseFacetDependency();
        JsonSchemaNamedReference definition = new JsonSchemaNamedReference();
        JsonSchema localFacetSchema = new JsonSchema();
        JsonSchema facetSchema;
        
        definition.setName( getDefinitionName( (alias != null) ? alias : sourceFacet ) );
        
        if (baseFacet != null) {
        	TLAlias baseAlias = (alias == null) ? null : AliasCodegenUtils.getOwnerAlias( alias );
        	NamedEntity baseType = (alias == null) ? baseFacet : baseAlias;
        	JsonSchemaReference baseSchemaRef = new JsonSchemaReference(
        			jsonUtils.getSchemaReferencePath( baseType, sourceFacet ) );
        	
        	facetSchema = new JsonSchema();
        	facetSchema.getAllOf().add( baseSchemaRef );
        	facetSchema.getAllOf().add( new JsonSchemaReference( localFacetSchema ) );
        	
        } else if (baseFacetDependency != null) {
        	JsonSchemaReference baseSchemaRef = new JsonSchemaReference(
        			jsonUtils.getSchemaReferencePath( baseFacetDependency, sourceFacet ) );
        	
        	facetSchema = new JsonSchema();
        	facetSchema.getAllOf().add( baseSchemaRef );
        	facetSchema.getAllOf().add( new JsonSchemaReference( localFacetSchema ) );
            addCompileTimeDependency( baseFacetDependency );
        	
        } else {
        	JsonSchemaNamedReference discriminator = createDiscriminatorProperty();
        	
        	localFacetSchema.getProperties().add( discriminator );
        	localFacetSchema.setDiscriminator( discriminator.getName() );
        	definition.setSchema( new JsonSchemaReference( localFacetSchema ) );
        	facetSchema = localFacetSchema;
        }
        
		if (sourceFacet.getOwningEntity() instanceof TLEquivalentOwner) {
			facetSchema.getEquivalentItems().addAll( jsonUtils.getEquivalentInfo(
					(TLEquivalentOwner) sourceFacet.getOwningEntity() ) );
		}
		
        facetSchema.setDocumentation( createJsonDocumentation( sourceFacet ) );
		facetSchema.setEntityInfo( jsonUtils.getEntityInfo( sourceFacet.getOwningEntity() ) );
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
	 * Constructs a global definition in the form of a single-property schema of the type
	 * specified by the global element and type names provided.
	 * 
	 * @param elementName  the name of the global element
	 * @param typeName  the name of the type to reference in the #/definitions section of the local schema
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
			
			globalDef.setSchema( new JsonSchemaReference(
					jsonUtils.getSchemaReferencePath( getSourceFacet(), getSourceFacet() ) ) );
			globalDefSchema.getProperties().add( globalDef );
			globalElement = new JsonSchemaReference( globalDefSchema );
		}
		return globalElement;
	}
	
    /**
     * If the value returned from '<code>getLocalBaseFacet()</code>' is null, this method
     * may return an alternative in the form of a <code>SchemaDependency</code> object.  By
     * default, this method returns null; subclasses may override for facet-specific
     * configurations.
     * 
     * @return SchemaDependency
     */
    protected SchemaDependency getLocalBaseFacetDependency() {
    	return null;
    }

	/**
	 * Creates the discriminator property for a JSON definition that may be used to support
	 * inheritance and polymorphism in JSON messages.
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
	 * Constructs the list of <code>JsonSchemaNamedReference</code> definitions that are based on
	 * OTM attributes and non-element indicators.
	 * 
	 * @return List<JsonSchemaNamedReference>
	 */
	protected List<JsonSchemaNamedReference> createDefinitions() {
        ObjectTransformer<TLAttribute, CodegenArtifacts, CodeGenerationTransformerContext> attributeTransformer =
        		getTransformerFactory().getTransformer(TLAttribute.class, CodegenArtifacts.class);
        ObjectTransformer<TLProperty, JsonSchemaNamedReference, CodeGenerationTransformerContext> elementTransformer =
        		getTransformerFactory().getTransformer(TLProperty.class, JsonSchemaNamedReference.class);
        ObjectTransformer<TLIndicator, JsonSchemaNamedReference, CodeGenerationTransformerContext> indicatorTransformer =
        		getTransformerFactory().getTransformer(TLIndicator.class, JsonSchemaNamedReference.class);
        List<JsonSchemaNamedReference> definitions = new ArrayList<>();
		CodeGenerationTransformerContext transformContext = getTransformerFactory().getContext();
		
		transformContext.setContextCacheEntry( AbstractJsonSchemaTransformer.MEMBER_FIELD_OWNER_KEY, getSourceFacet() );
		
        for (TLMemberField<?> field : getMemberFields()) {
        	if (field instanceof TLAttribute) {
        		definitions.addAll( attributeTransformer.transform( (TLAttribute) field )
        				.getArtifactsOfType( JsonSchemaNamedReference.class ) );
        		
        	} else if (field instanceof TLProperty) {
        		definitions.add( elementTransformer.transform( (TLProperty) field ) );
        		
        	} else if (field instanceof TLIndicator) {
        		definitions.add( indicatorTransformer.transform( (TLIndicator) field ) );
        	}
        }
        
		transformContext.setContextCacheEntry( AbstractJsonSchemaTransformer.MEMBER_FIELD_OWNER_KEY, null );
		
        return definitions;
	}
	
	/**
	 * Returns JSON schema documentation for the source facet of this delegate.
	 * 
	 * @param docOwner  the owner for which JSON documentation should be created
	 * @return JsonDocumentation
	 */
	protected JsonDocumentation createJsonDocumentation(TLDocumentationOwner docOwner) {
		TLDocumentation doc = DocumentationFinder.getDocumentation( docOwner );
		JsonDocumentation jsonDoc = null;
		
		if (doc != null) {
	        ObjectTransformer<TLDocumentation, JsonDocumentation, CodeGenerationTransformerContext> transformer =
	        		getTransformerFactory().getTransformer(doc, JsonDocumentation.class);
			
	        jsonDoc = transformer.transform( doc );
		}
		return jsonDoc;
	}

	/**
     * Returns the name of the non-substitutable element used to represent the source facet or the
     * specified alias.
     * 
     * @param facetAlias  the alias of the source facet element being created (may be null)
     * @return String
     */
    protected final String getNonSubstitableElementName(TLAlias facetAlias) {
    	return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() ))
    			.getNonSubstitableElementName( facetAlias );
    }
    
    /**
     * Returns the facet instance that should serve as the base type for the source facet. In some
     * cases (business/core object extension), the facet returned by this method may have a
     * different owner than that of the source facet.
     * 
     * @return TLFacet
     */
    protected final TLFacet getBaseFacet() {
    	return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() )).getBaseFacet();
    }
    
    /**
     * Returns true if the source facet should have a non-substitutable facet in addition to the
     * substitutable one that is created by default.
     * 
     * @return boolean
     */
    protected final boolean hasNonSubstitutableElement() {
    	return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() )).hasNonSubstitutableElement();
    }
    
    /**
     * If the source facet should support an extension point element, this method will return
     * the extension point property to use in the facet's JSON schema definition. If extensions
     * are not supported for the facet, this method will return null.
     * 
     * @return JsonSchemaNamedReference
     */
    protected final JsonSchemaNamedReference getExtensionPointProperty() {
    	QName extensionPointName = ((TLFacetCodegenDelegate)
    			xsdDelegateFactory.getDelegate( getSourceFacet() )).getExtensionPointElement();
    	JsonSchemaNamedReference extensionPointProperty = null;
    	
    	if (extensionPointName != null) {
    		String schemaPath = null;
    		
    		// Look through all of the schema dependencies to find the one that matches are extension
    		// point QName.  This is a bit inefficient, but it keeps us from having to replicate all
    		// of the extension point logic from the XSD facet delegates.
    		for (SchemaDependency dependency : SchemaDependency.getAllDependencies()) {
    			if ((extensionPointName.getNamespaceURI().equals( dependency.getSchemaDeclaration().getNamespace() ))
    					&& extensionPointName.getLocalPart().equals( dependency.getLocalName() )) {
    				CodeGenerationContext cgContext = getTransformerFactory().getContext().getCodegenContext();
    				String builtInLocation = XsdCodegenUtils.getBuiltInSchemaOutputLocation( cgContext );
    				String referencedFilename = dependency.getSchemaDeclaration().getFilename(
    						CodeGeneratorFactory.JSON_SCHEMA_TARGET_FORMAT );
    				
    				if ((referencedFilename != null) && !isLocalNameReferencesEnabled()) {
    					schemaPath = builtInLocation + referencedFilename + "#/definitions/" + dependency.getLocalName();
    					addCompileTimeDependency( dependency );
    				} else {
    					schemaPath = "#/definitions/" + dependency.getLocalName();
    				}
    			}
    		}
    		extensionPointProperty = new JsonSchemaNamedReference(
    				extensionPointName.getLocalPart(),
    				new JsonSchemaReference(schemaPath) );
    	}
    	return extensionPointProperty;
    }
    
    /**
     * Returns the list of all attribute, element, and indicator member fields.
     * 
     * @return List<TLMemberField<?>>
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
     * @return List<TLAttribute>
     */
    protected final List<TLAttribute> getAttributes() {
    	return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() )).getAttributes();
    }

    /**
     * Returns the list of elements (properties) to be generated for the source facet.
     * 
     * @return List<TLProperty>
     */
    protected final List<TLProperty> getElements() {
    	return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() )).getElements();
    }

    /**
     * Returns the list of indicators to be generated for the source facet.
     * 
     * @return List<TLIndicator>
     */
    protected final List<TLIndicator> getIndicators() {
    	return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() )).getIndicators();
    }

}
