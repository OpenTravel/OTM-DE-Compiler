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
package org.opentravel.schemacompiler.codegen.xsd;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.w3._2001.xmlschema.ComplexType;

/**
 * Performs the translation from <code>TLActionRequest</code> objects to the JAXB nodes used to
 * produce the schema output.
 */
public class TLActionRequestCodegenTransformer extends TLBaseActionCodegenTransformer<TLActionRequest> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public CodegenArtifacts transform(TLActionRequest source) {
		CodegenArtifacts artifacts = new CodegenArtifacts();
		QName elementName = XsdCodegenUtils.getGlobalElementName( source );
		String typeName = XsdCodegenUtils.getGlobalTypeName( source );
		
		if ((elementName != null) && (typeName != null)
				&& (source.getPayloadType() instanceof TLActionFacet)) {
			ComplexType type = createType( typeName, (TLActionFacet) source.getPayloadType(), source );
			
			if (type != null) {
				artifacts.addArtifact( createElement( elementName, typeName ) );
				artifacts.addArtifact( type );
			}
		}
		return artifacts;
	}
	
}
