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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;

/**
 * Performs the translation from <code>TLDocumentation</code> objects to a
 * <code>JsonDocumentation</code> item.
 */
public class TLDocumentationJsonCodegenTransformer extends AbstractJsonSchemaTransformer<TLDocumentation, JsonDocumentation> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public JsonDocumentation transform(TLDocumentation source) {
		JsonDocumentation schemaDoc = new JsonDocumentation( source.getDescription() );
		
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
	
    /**
     * Merges multiple JSON schema documentation elements into a single element.
     * 
     * @param jsonDocs  the JSON schema documentation elements to merge
     * @return JsonDocumentation
     */
    public static JsonDocumentation mergeDocumentation(JsonDocumentation... jsonDocs) {
    	JsonDocumentation mergedDoc = new JsonDocumentation();
    	List<String> descriptions = new ArrayList<>();
    	
    	for (JsonDocumentation jsonDoc : jsonDocs) {
    		if (jsonDoc == null) {
    			continue;
    		}
    		if ((jsonDoc.getDescriptions() != null) && (jsonDoc.getDescriptions().length > 0)) {
    			descriptions.addAll( Arrays.asList( jsonDoc.getDescriptions() ) );
    		}
    		for (String deprecation : jsonDoc.getDeprecations()) {
    			mergedDoc.addDeprecation( deprecation );
    		}
    		for (String reference : jsonDoc.getReferences()) {
    			mergedDoc.addReference( reference );
    		}
    		for (String implementers : jsonDoc.getImplementers()) {
    			mergedDoc.addImplementer( implementers );
    		}
    		for (String moreInfo : jsonDoc.getMoreInfos()) {
    			mergedDoc.addMoreInfo( moreInfo );
    		}
    		for (String docContext : jsonDoc.getOtherDocumentationContexts()) {
    			while (mergedDoc.getOtherDocumentationContexts().contains( docContext )) {
    				docContext = "_" + docContext;
    			}
    			mergedDoc.addOtherDocumentation( docContext, jsonDoc.getOtherDocumentation( docContext ) );
    		}
    	}
    	return mergedDoc;
    }
    
}
