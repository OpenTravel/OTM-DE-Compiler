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

import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.util.JsonSchemaNamingUtils;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;

/**
 * Performs the translation from <code>TLActionRequest</code> objects to the JAXB nodes used to
 * produce the schema output.
 */
public class TLActionRequestJsonCodegenTransformer extends TLBaseActionJsonCodegenTransformer<TLActionRequest> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public JsonSchemaNamedReference transform(TLActionRequest source) {
		String definitionName = JsonSchemaNamingUtils.getGlobalDefinitionName( source );
		JsonSchemaNamedReference definition = null;
		
		if ((definitionName != null) && (source.getPayloadType() instanceof TLActionFacet)) {
			definition = createDefinition( definitionName, (TLActionFacet) source.getPayloadType(), source );
		}
		return definition;
	}
	
}
