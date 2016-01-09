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

import java.util.List;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.transform.ObjectTransformer;

/**
 * Base transformer class for JSON schema code generation of action requests and responses.
 */
public abstract class TLBaseActionJsonCodegenTransformer<S> extends AbstractJsonSchemaTransformer<S,JsonSchemaNamedReference> {
	
	/**
	 * Creates an XSD complex type with the specified name using the payload information
	 * provided in the action facet.
	 * 
	 * @param definitionName  the name of the JSON definition to create
	 * @param payloadType  the action facet that specifies the structure and content of the payload
	 * @param source  the source object that will provide documentation for the generated type
	 * @return ComplexType
	 */
	protected JsonSchemaNamedReference createDefinition(String definitionName, TLActionFacet payloadType, TLDocumentationOwner source) {
        List<TLAttribute> attributeList = PropertyCodegenUtils.getInheritedAttributes( payloadType );
        List<TLProperty> elementList = PropertyCodegenUtils.getInheritedProperties( payloadType );
        List<TLIndicator> indicatorList = PropertyCodegenUtils.getInheritedIndicators( payloadType );
        boolean hasFacetPayload = (definitionName != null) && ((payloadType.getReferenceRepeat() != 0)
        		|| !attributeList.isEmpty() || !elementList.isEmpty() || !indicatorList.isEmpty());
        JsonSchemaNamedReference definition = null;
        
        // Only create a new definition if we have multiple items that will belong to the type.  For example,
        // if the payload is only a non-repeating business object, then the BO itself can be the RQ/RS
        // payload without requiring that it be wrapped in another complex type.
        if (hasFacetPayload) {
        	JsonSchema defSchema = new JsonSchema();
        	
        	definition = new JsonSchemaNamedReference();
        	definition.setName( definitionName );
        	definition.setSchema( new JsonSchemaReference( defSchema ) );
        	defSchema.setEntityInfo( jsonUtils.getEntityInfo( (NamedEntity) source ) );
            transformDocumentation( source, defSchema );
            
            ObjectTransformer<TLAttribute, CodegenArtifacts, CodeGenerationTransformerContext> attributeTransformer = getTransformerFactory()
                    .getTransformer(TLAttribute.class, CodegenArtifacts.class);
            ObjectTransformer<TLProperty, JsonSchemaNamedReference, CodeGenerationTransformerContext> elementTransformer = getTransformerFactory()
                    .getTransformer(TLProperty.class, JsonSchemaNamedReference.class);
            ObjectTransformer<TLIndicator, JsonSchemaNamedReference, CodeGenerationTransformerContext> indicatorTransformer = getTransformerFactory()
                    .getTransformer(TLIndicator.class, JsonSchemaNamedReference.class);
            SchemaDependency extensionPointElement = SchemaDependency.getExtensionPointElement();
    		TLProperty boElement = ResourceCodegenUtils.getBusinessObjectElement( payloadType );
    		
    		if (boElement != null) {
    			defSchema.getProperties().add( elementTransformer.transform( boElement ) );
    		}
            for (TLProperty element : elementList) {
            	defSchema.getProperties().add( elementTransformer.transform( element ) );
            }
            for (TLIndicator indicator : indicatorList) {
                if (indicator.isPublishAsElement()) {
                	defSchema.getProperties().add( indicatorTransformer .transform( indicator ) );
                }
            }
            for (TLAttribute attribute : attributeList) {
            	defSchema.getProperties().addAll( attributeTransformer.transform( attribute )
            			.getArtifactsOfType( JsonSchemaNamedReference.class ) );
            }
            for (TLIndicator indicator : indicatorList) {
                if (!indicator.isPublishAsElement()) {
                	defSchema.getProperties().add( indicatorTransformer.transform( indicator ) );
                }
            }
        	defSchema.getProperties().add( new JsonSchemaNamedReference(
        			extensionPointElement.toQName().getLocalPart(),
        			new JsonSchemaReference( jsonUtils.getSchemaReferencePath( extensionPointElement, (NamedEntity) source ) ) ) );
        }
		return definition;
	}
}
