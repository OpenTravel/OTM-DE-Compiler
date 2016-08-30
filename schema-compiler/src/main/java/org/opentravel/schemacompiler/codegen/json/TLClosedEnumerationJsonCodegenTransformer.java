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

import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.util.EnumCodegenUtils;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;

/**
 * Performs the translation from <code>TLClosedEnumeration</code> objects to the JSON schema elements
 * used to produce the output.
 */
public class TLClosedEnumerationJsonCodegenTransformer extends AbstractJsonSchemaTransformer<TLClosedEnumeration, CodegenArtifacts> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public CodegenArtifacts transform(TLClosedEnumeration source) {
		JsonSchemaNamedReference closedEnum = new JsonSchemaNamedReference();
		CodegenArtifacts artifacts = new CodegenArtifacts();
		JsonSchema schema = new JsonSchema();
		List<String> enumValues = schema.getEnumValues();
		
		closedEnum.setName( getDefinitionName( source ) );
		closedEnum.setSchema( new JsonSchemaReference( schema ) );
		
		transformDocumentation( source, schema );
        schema.setEntityInfo( jsonUtils.getEntityInfo( source ) );
        schema.setType( JsonType.jsonString );
		
        for (TLEnumValue modelEnum : EnumCodegenUtils.getInheritedValues( source )) {
        	enumValues.add( modelEnum.getLiteral() );
        }
		artifacts.addArtifact( closedEnum );
		
		return artifacts;
	}
	
}
