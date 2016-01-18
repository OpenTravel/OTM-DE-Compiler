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
package org.opentravel.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_05.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_05.FacetAction;
import org.opentravel.ns.ota2.librarymodel_v01_05.ReferenceType;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Handles the transformation of objects from the <code>TLActionFacet</code> type to the
 * <code>FacetAction</code> type.
 *
 * @author S. Livezey
 */
public class TLActionFacetTransformer extends TLComplexTypeTransformer<TLActionFacet,FacetAction> {

	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public FacetAction transform(TLActionFacet source) {
		NamedEntity basePayload = source.getBasePayload();
		FacetAction facet = new FacetAction();
		
		facet.setLabel(trimString(source.getName()));
		facet.setReferenceType(transformReferenceType(source.getReferenceType()));
		facet.setReferenceFacet(trimString(source.getReferenceFacetName()));
		facet.setReferenceRepeat(transformRepeatValue(source.getReferenceRepeat()));
		
        if (basePayload != null) {
        	facet.setBasePayload(context.getSymbolResolver().buildEntityName(
        			basePayload.getNamespace(), basePayload.getLocalName()));
        } else {
        	facet.setBasePayload(trimString(source.getBasePayloadName(), true));
        }
        
        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation, Documentation, SymbolResolverTransformerContext> docTransformer =
            		getTransformerFactory().getTransformer(TLDocumentation.class, Documentation.class);

            facet.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }
		return facet;
	}
	
	/**
	 * Transforms the given <code>TLReferenceType</code> value.
	 * 
	 * @param sourceReferenceType  the enumeration value to transform
	 * @return ReferenceType
	 */
	private ReferenceType transformReferenceType(TLReferenceType sourceReferenceType) {
		ReferenceType referenceType;
		
		if (sourceReferenceType != null) {
			switch (sourceReferenceType) {
				case NONE:
					referenceType = ReferenceType.NONE;
					break;
				case OPTIONAL:
					referenceType = ReferenceType.OPTIONAL;
					break;
				case REQUIRED:
					referenceType = ReferenceType.REQUIRED;
					break;
				default:
					referenceType = null;
					break;
			}
		} else {
			referenceType = null;
		}
		return referenceType;
	}
	
	/**
	 * Transforms the given repeat value.
	 * 
	 * @param repeatInt  the integer value of the repeat
	 * @return String
	 */
    private String transformRepeatValue(int repeatInt) {
        return (repeatInt < 0) ? UNLIMITED_TOKEN : (repeatInt + "");
    }

}
