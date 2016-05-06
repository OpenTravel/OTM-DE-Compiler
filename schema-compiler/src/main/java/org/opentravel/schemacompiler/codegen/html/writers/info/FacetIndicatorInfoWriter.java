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

import org.opentravel.schemacompiler.codegen.html.builders.FacetDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

/**
 * @author Eric.Bronson
 *
 */
public class FacetIndicatorInfoWriter extends AbstractIndicatorInfoWriter<FacetDocumentationBuilder> {

	/**
	 * @param writer
	 * @param owner
	 */
	public FacetIndicatorInfoWriter(SubWriterHolderWriter writer,
			FacetDocumentationBuilder owner) {
		super(writer, owner);
	}

	/* (non-Javadoc)
	 * @see org.opentravel.schemacompiler.codegen.documentation.html.writers.AbstractFieldInfoWriter#getParent(org.opentravel.schemacompiler.codegen.documentation.DocumentationBuilder)
	 */
	@Override
	protected FacetDocumentationBuilder getParent(
			FacetDocumentationBuilder classDoc) {
		return (FacetDocumentationBuilder) classDoc.getSuperType();
	}

}
