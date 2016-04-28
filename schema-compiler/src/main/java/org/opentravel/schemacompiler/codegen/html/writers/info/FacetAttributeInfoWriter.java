/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers.info;

import org.opentravel.schemacompiler.codegen.html.builders.FacetDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.writers.SubWriterHolderWriter;

/**
 * @author Eric.Bronson
 *
 */
public class FacetAttributeInfoWriter extends AbstractAttributeInfoWriter<FacetDocumentationBuilder> {

	/**
	 * @param writer
	 * @param owner
	 */
	public FacetAttributeInfoWriter(SubWriterHolderWriter writer,
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
