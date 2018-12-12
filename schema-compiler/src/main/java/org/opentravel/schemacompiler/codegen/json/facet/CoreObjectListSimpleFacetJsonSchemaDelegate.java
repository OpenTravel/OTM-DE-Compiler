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
import org.opentravel.schemacompiler.model.TLListFacet;

/**
 * Code generation delegate for <code>TLListFacet</code> instances with a facet type of
 * <code>SIMPLE</code> and a facet owner of type <code>TLCoreObject</code>.
 */
public class CoreObjectListSimpleFacetJsonSchemaDelegate extends TLListFacetJsonSchemaDelegate {
	
    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet  the source facet
     */
    public CoreObjectListSimpleFacetJsonSchemaDelegate(TLListFacet sourceFacet) {
        super(sourceFacet);
    }
    
	/**
	 * @see org.opentravel.schemacompiler.codegen.json.facet.FacetJsonSchemaDelegate#createDefinition()
	 */
	@Override
	protected JsonSchemaNamedReference createDefinition() {
		JsonSchemaNamedReference definition = new JsonSchemaNamedReference();
		JsonSchemaReference itemSchemaRef = new JsonSchemaReference();
		JsonSchema arraySchema = new JsonSchema();
        TLListFacet sourceFacet = getSourceFacet();
		
        arraySchema.setType( JsonType.JSON_ARRAY );
		arraySchema.setItems( itemSchemaRef );
        itemSchemaRef.setSchemaPath( jsonUtils.getSchemaReferencePath( sourceFacet.getItemFacet(), sourceFacet ) );
		definition.setName( getDefinitionName( sourceFacet ) );
		definition.setSchema( new JsonSchemaReference( arraySchema ) );
		return definition;
	}
    
}
