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
package org.opentravel.schemacompiler.transform.jaxb15_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_05.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_05.FacetAction;
import org.opentravel.ns.ota2.librarymodel_v01_05.ReferenceType;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;

/**
 * Handles the transformation of objects from the <code>FacetAction</code> type to the
 * <code>TLActionFacet</code> type.
 *
 * @author S. Livezey
 */
public class FacetActionTransformer extends ComplexTypeTransformer<FacetAction,TLActionFacet> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLActionFacet transform(FacetAction source) {
		TLActionFacet facet = new TLActionFacet();
		
		facet.setName(trimString(source.getLabel()));
		facet.setReferenceType(transformReferenceType(source.getReferenceType()));
		facet.setReferenceFacetName(trimString(source.getReferenceFacet()));
		facet.setReferenceRepeat(PropertyTransformer.convertRepeatValue(source.getReferenceRepeat()));
		facet.setBasePayloadName( source.getBasePayload() );
		
        if ((source.getPayloadFacetFilter() != null) && !source.getPayloadFacetFilter().isEmpty()) {
        	facet.setPayloadFacetFilter( source.getPayloadFacetFilter() );
        }

        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation, TLDocumentation, DefaultTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(Documentation.class, TLDocumentation.class);

            facet.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }
		return facet;
	}
	
	/**
	 * Transforms the given reference-type value.
	 * 
	 * @param refType  the enumeration value to transform
	 * @return TLReferenceType
	 */
	private TLReferenceType transformReferenceType(ReferenceType sourceRefType) {
		TLReferenceType refType;
		
		if (sourceRefType != null) {
			switch (sourceRefType) {
				case NONE:
					refType = TLReferenceType.NONE;
					break;
				case OPTIONAL:
					refType = TLReferenceType.OPTIONAL;
					break;
				case REQUIRED:
					refType = TLReferenceType.REQUIRED;
					break;
				default:
					refType = null;
			}
		} else {
			refType = null;
		}
		return refType;
	}
	
}
