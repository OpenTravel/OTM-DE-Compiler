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

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.transform.ObjectTransformer;

/**
 * Performs the translation from <code>TLValueWithAttributes</code> objects to the JSON schema elements
 * used to produce the output.
 */
public class TLValueWithAttributesJsonCodegenTransformer extends AbstractJsonSchemaTransformer<TLValueWithAttributes, CodegenArtifacts> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public CodegenArtifacts transform(TLValueWithAttributes source) {
        ObjectTransformer<TLAttribute, CodegenArtifacts, CodeGenerationTransformerContext> attributeTransformer = getTransformerFactory()
                .getTransformer(TLAttribute.class, CodegenArtifacts.class);
        ObjectTransformer<TLIndicator, JsonSchemaNamedReference, CodeGenerationTransformerContext> indicatorTransformer = getTransformerFactory()
                .getTransformer(TLIndicator.class, JsonSchemaNamedReference.class);
        NamedEntity vwaParentType = getBaseParentType(source);
        CodegenArtifacts artifacts = new CodegenArtifacts();
		JsonSchemaNamedReference targetVwa = new JsonSchemaNamedReference();
		JsonSchema vwaSchema = new JsonSchema();

        targetVwa.setName( source.getName() );
        targetVwa.setSchema( new JsonSchemaReference( vwaSchema ) );
        transformDocumentation( source, vwaSchema );
        vwaSchema.setEntityInfo( getEntityInfo( source ) );
        
        // Create the attribute(s) for the VWA parent type
        if ((vwaParentType != null) && !PropertyCodegenUtils.isEmptyStringType( vwaParentType )) {
        	JsonSchemaReference vwaValueSchemaRef = new JsonSchemaReference();
        	String referencePath = getSchemaReferencePath( vwaParentType, source );
        	
            vwaSchema.getProperties().add( new JsonSchemaNamedReference( "value", vwaValueSchemaRef ) );
            
            if ((vwaParentType instanceof TLOpenEnumeration)
                    || (vwaParentType instanceof TLRoleEnumeration)) {
        		JsonSchemaReference extAttrSchemaRef = new JsonSchemaReference();
        		
        		extAttrSchemaRef.setSchemaPath( getSchemaReferencePath(
        				SchemaDependency.getEnumExtension(), source ) );
        		vwaSchema.getProperties().add( new JsonSchemaNamedReference( "extension", extAttrSchemaRef ) );
            	referencePath += "_Base";
            }
        	vwaValueSchemaRef.setSchemaPath(  referencePath );
        	
            if (source.getValueDocumentation() != null) {
    	        ObjectTransformer<TLDocumentation, JsonSchemaDocumentation, CodeGenerationTransformerContext> docTransformer =
    	        		getTransformerFactory().getTransformer(source.getValueDocumentation(), JsonSchemaDocumentation.class);
                
                vwaValueSchemaRef.setSchemaPathDocumentation( docTransformer.transform( source.getValueDocumentation() ) );
            }
        }

        // Transform the attributes and indicators of the target type
        for (TLAttribute modelAttribute : PropertyCodegenUtils.getInheritedAttributes(source)) {
        	vwaSchema.getProperties().addAll( attributeTransformer.transform( modelAttribute )
        			.getArtifactsOfType( JsonSchemaNamedReference.class ) );
        }
        for (TLIndicator modelIndicator : PropertyCodegenUtils.getInheritedIndicators(source)) {
        	vwaSchema.getProperties().add( indicatorTransformer.transform( modelIndicator ) );
        }

        artifacts.addArtifact( targetVwa );
        return artifacts;
	}
	
    /**
     * Returns the root parent type for the given VWA. If the parent type is another VWA, this
     * method will search the hierarchy for the base (non-VWA) parent type.
     * 
     * @param vwa
     *            the VWA for which schema artifacts are being generated
     * @return NamedEntity
     */
    private NamedEntity getBaseParentType(TLValueWithAttributes vwa) {
        NamedEntity parentType = vwa.getParentType();

        while (parentType instanceof TLValueWithAttributes) {
            parentType = ((TLValueWithAttributes) parentType).getParentType();
        }
        return parentType;
    }

}
