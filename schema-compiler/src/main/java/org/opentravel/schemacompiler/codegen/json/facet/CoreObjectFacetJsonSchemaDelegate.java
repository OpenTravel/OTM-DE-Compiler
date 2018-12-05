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

import java.util.List;

import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;

/**
 * Base class for facet code generation delegates used to generate code artifacts for
 * <code>TLFacet</code model elements that are owned by <code>TLCoreObject</code> instances.
 */
public class CoreObjectFacetJsonSchemaDelegate extends TLFacetJsonSchemaDelegate {
	
    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet  the source facet
     */
    public CoreObjectFacetJsonSchemaDelegate(TLFacet sourceFacet) {
        super(sourceFacet);
    }

	/**
	 * @see org.opentravel.schemacompiler.codegen.json.facet.TLFacetJsonSchemaDelegate#createDefinitions()
	 */
	@Override
	protected List<JsonSchemaNamedReference> createDefinitions() {
		List<JsonSchemaNamedReference> definitions = super.createDefinitions();
		
        if (getLocalBaseFacet() == null) {
            TLCoreObject owner = (TLCoreObject) getSourceFacet().getOwningEntity();

            while (owner != null) {
                TLCoreObject ownerExtension = (TLCoreObject) FacetCodegenUtils
                        .getFacetOwnerExtension(owner);

                if (!owner.getRoleEnumeration().getRoles().isEmpty()) {
                	JsonSchemaNamedReference roleAttr = new JsonSchemaNamedReference();
                	
                	if (ownerExtension != null) {
                		roleAttr.setName(
                        		XsdCodegenUtils.getRoleAttributeName( owner.getLocalName() ) );
                	} else {
                		roleAttr.setName("role");
                	}
                	roleAttr.setSchema( new JsonSchemaReference(
                			jsonUtils.getSchemaReferencePath( owner.getRoleEnumeration(), owner )));
                	definitions.add( roleAttr );
                }
                owner = ownerExtension;
            }
        }
		return definitions;
	}

}
