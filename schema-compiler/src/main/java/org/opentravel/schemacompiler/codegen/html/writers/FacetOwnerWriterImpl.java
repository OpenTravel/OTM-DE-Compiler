/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers;

import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.FacetOwnerDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.info.FacetInfoWriter;

/**
 * @author Eric.Bronson
 *
 */
public class FacetOwnerWriterImpl<T extends FacetOwnerDocumentationBuilder<?>> extends NamedEntityWriter<T> implements
		FacetOwnerWriter {


	public FacetOwnerWriterImpl(T member,
			DocumentationBuilder prev,
			DocumentationBuilder next) throws Exception {
		super(member, prev, next);
	}

	/* (non-Javadoc)
	 * @see org.opentravel.schemacompiler.codegen.html.writers.FacetOwnerWriter#addFacetInfo(org.opentravel.schemacompiler.codegen.html.Content)
	 */
	@Override
	public void addFacetInfo(Content objectTree) {
		if(member.getFacets().size() > 0){
			FacetInfoWriter facetWriter = new FacetInfoWriter(this, member);
			facetWriter.addInfo(objectTree);
		}
	}

}
