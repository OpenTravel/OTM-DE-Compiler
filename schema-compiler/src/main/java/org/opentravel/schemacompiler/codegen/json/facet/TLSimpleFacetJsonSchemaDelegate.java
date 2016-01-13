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

import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.util.JsonSchemaNamingUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLSimpleFacet;

/**
 * Code generation delegate for <code>TLSimpleFacet</code> instances with a facet type of
 * <code>SIMPLE</code> and a facet owner of type <code>TLCoreObject</code>.
 */
public class TLSimpleFacetJsonSchemaDelegate extends FacetJsonSchemaDelegate<TLSimpleFacet> {
	
    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet  the source facet
     */
    public TLSimpleFacetJsonSchemaDelegate(TLSimpleFacet sourceFacet) {
        super(sourceFacet);
    }
    
    /**
	 * @see org.opentravel.schemacompiler.codegen.json.facet.FacetJsonSchemaDelegate#createDefinition()
	 */
	@Override
	protected JsonSchemaNamedReference createDefinition() {
		JsonSchemaNamedReference definition = new JsonSchemaNamedReference();
		JsonSchemaReference schemaRef = new JsonSchemaReference();
		TLSimpleFacet sourceFacet = getSourceFacet();
		NamedEntity baseType = sourceFacet.getSimpleType();
		JsonType jsonType;
		
		definition.setName( JsonSchemaNamingUtils.getGlobalDefinitionName( sourceFacet ));
		definition.setSchema( schemaRef );
		
        // Special Case: For core objects, use the simple facet as the base type
		if (baseType instanceof TLCoreObject) {
			baseType = ((TLCoreObject) baseType).getSimpleFacet();
		}
		
		jsonType = JsonType.valueOf( baseType );
		
		if (jsonType != null) {
			JsonSchema schema = new JsonSchema();
			
			schema.setType( jsonType );
			transformDocumentation( sourceFacet, schema );
			schema.setEntityInfo( jsonUtils.getEntityInfo( sourceFacet ) );
			schema.getExampleItems().addAll( jsonUtils.getExampleInfo( sourceFacet ) );
			schema.getEquivalentItems().addAll( jsonUtils.getEquivalentInfo( sourceFacet ) );
			schemaRef.setSchema( schema );
			
		} else {
    		transformDocumentation( sourceFacet, schemaRef );
    		schemaRef.getExampleItems().addAll( jsonUtils.getExampleInfo( sourceFacet ) );
    		schemaRef.getEquivalentItems().addAll( jsonUtils.getEquivalentInfo( sourceFacet ) );
    		schemaRef.setSchemaPath( jsonUtils.getSchemaReferencePath( baseType, sourceFacet ) );
		}
		return definition;
	}

}
