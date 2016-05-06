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
package org.opentravel.schemacompiler.codegen.html.writers.info;

import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.VWADocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

/**
 * @author Eric.Bronson
 *
 */
public class VWAAttributeInfoWriter extends AbstractAttributeInfoWriter<VWADocumentationBuilder> {

	/**
	 * @param writer
	 * @param owner
	 */
	public VWAAttributeInfoWriter(SubWriterHolderWriter writer,
			VWADocumentationBuilder owner) {
		super(writer, owner);
	}

	@Override
	protected VWADocumentationBuilder getParent(VWADocumentationBuilder classDoc) {
		DocumentationBuilder parent = classDoc.getSuperType();
		if(parent instanceof VWADocumentationBuilder){
			return (VWADocumentationBuilder) parent;
		}
		return null;
	}

}
