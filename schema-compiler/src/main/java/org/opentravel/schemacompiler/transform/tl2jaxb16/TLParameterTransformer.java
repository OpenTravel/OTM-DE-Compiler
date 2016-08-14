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
package org.opentravel.schemacompiler.transform.tl2jaxb16;

import org.opentravel.ns.ota2.librarymodel_v01_06.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_06.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_06.Example;
import org.opentravel.ns.ota2.librarymodel_v01_06.ParamLocation;
import org.opentravel.ns.ota2.librarymodel_v01_06.Parameter;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLParamLocation;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Handles the transformation of objects from the <code>TLParameter</code> type to the
 * <code>Parameter</code> type.
 *
 * @author S. Livezey
 */
public class TLParameterTransformer extends TLComplexTypeTransformer<TLParameter,Parameter> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public Parameter transform(TLParameter source) {
        ObjectTransformer<TLEquivalent, Equivalent, SymbolResolverTransformerContext> equivTransformer =
        		getTransformerFactory().getTransformer(TLEquivalent.class, Equivalent.class);
        ObjectTransformer<TLExample, Example, SymbolResolverTransformerContext> exTransformer =
        		getTransformerFactory().getTransformer(TLExample.class, Example.class);
		TLMemberField<?> sourceFieldRef = source.getFieldRef();
		Parameter parameter = new Parameter();
		
		parameter.setLocation(transformLocation(source.getLocation()));
		
        if (sourceFieldRef != null) {
        	parameter.setFieldName(sourceFieldRef.getName());
        } else {
        	parameter.setFieldName(trimString(source.getFieldRefName(), false));
        }
		
        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation, Documentation, SymbolResolverTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(TLDocumentation.class, Documentation.class);

            parameter.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }
        
        for (TLEquivalent sourceEquiv : source.getEquivalents()) {
        	parameter.getEquivalent().add(equivTransformer.transform(sourceEquiv));
        }

        for (TLExample sourceEx : source.getExamples()) {
        	parameter.getExample().add(exTransformer.transform(sourceEx));
        }

		return parameter;
	}
	
	/**
	 * Transforms the given <code>TLParamLocation</code> value.
	 * 
	 * @param sourceLocation  the enumeration value to transform
	 * @return ParamLocation
	 */
	private ParamLocation transformLocation(TLParamLocation sourceLocation) {
		ParamLocation location;
		
		if (sourceLocation != null) {
			switch (sourceLocation) {
				case HEADER:
					location = ParamLocation.HEADER;
					break;
				case PATH:
					location = ParamLocation.PATH;
					break;
				case QUERY:
					location = ParamLocation.QUERY;
					break;
				default:
					location = null;
					break;
				
			}
		} else {
			location = null;
		}
		return location;
	}
}
