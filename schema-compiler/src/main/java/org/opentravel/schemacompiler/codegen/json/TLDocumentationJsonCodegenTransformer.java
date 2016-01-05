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

import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaDocumentation;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;

/**
 * Performs the translation from <code>TLDocumentation</code> objects to a
 * <code>JsonSchemaDocumentation</code> item.
 */
public class TLDocumentationJsonCodegenTransformer extends AbstractJsonSchemaTransformer<TLDocumentation, JsonSchemaDocumentation> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public JsonSchemaDocumentation transform(TLDocumentation source) {
		JsonSchemaDocumentation schemaDoc = new JsonSchemaDocumentation( source.getDescription() );
		
		for (TLDocumentationItem deprecation : source.getDeprecations()) {
			schemaDoc.addDeprecation( deprecation.getText() );
		}
		for (TLDocumentationItem reference : source.getReferences()) {
			schemaDoc.addReference( reference.getText() );
		}
		for (TLDocumentationItem implementers : source.getImplementers()) {
			schemaDoc.addImplementer( implementers.getText() );
		}
		for (TLDocumentationItem moreInfo : source.getMoreInfos()) {
			schemaDoc.addMoreInfo( moreInfo.getText() );
		}
		for (TLAdditionalDocumentationItem otherDoc : source.getOtherDocs()) {
			schemaDoc.addOtherDocumentation( otherDoc.getContext(), otherDoc.getText() );
		}
		return schemaDoc;
	}
	
}
