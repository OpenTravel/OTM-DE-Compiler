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

import org.opentravel.schemacompiler.codegen.json.TLDocumentationJsonCodegenTransformer;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaDocumentation;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLFacet;

/**
 * Code generation delegate for <code>TLFacet</code> instances with a facet type of
 * <code>SUMMARY</code> and a facet owner of type <code>TLCoreObject</code>.
 */
public class CoreObjectSummaryFacetJsonSchemaDelegate extends CoreObjectFacetJsonSchemaDelegate {
	
    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet  the source facet
     */
    public CoreObjectSummaryFacetJsonSchemaDelegate(TLFacet sourceFacet) {
        super(sourceFacet);
    }

	/**
	 * @see org.opentravel.schemacompiler.codegen.json.facet.TLFacetJsonSchemaDelegate#createJsonDocumentation(org.opentravel.schemacompiler.model.TLDocumentationOwner)
	 */
	@Override
	protected JsonSchemaDocumentation createJsonDocumentation(TLDocumentationOwner docOwner) {
		JsonSchemaDocumentation jsonDoc = null;

        if (docOwner instanceof TLFacet) {
            TLFacet sourceFacet = (TLFacet) docOwner;

            if (XsdCodegenUtils.isSimpleCoreObject( sourceFacet.getOwningEntity() )) {
            	JsonSchemaDocumentation ownerDoc =
            			super.createJsonDocumentation( (TLDocumentationOwner) sourceFacet.getOwningEntity() );
            	JsonSchemaDocumentation facetDoc =
            			super.createJsonDocumentation( sourceFacet );

                jsonDoc = TLDocumentationJsonCodegenTransformer.mergeDocumentation( ownerDoc, facetDoc );
            }
        }
        if (jsonDoc == null) {
        	jsonDoc = super.createJsonDocumentation( docOwner );
        }
        return jsonDoc;
	}
    
}
