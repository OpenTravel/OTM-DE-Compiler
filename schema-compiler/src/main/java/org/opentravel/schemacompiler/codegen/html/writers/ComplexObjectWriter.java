/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers;

import org.opentravel.schemacompiler.codegen.html.builders.ComplexTypeDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.info.AliasInfoWriter;

/**
 * @author Eric.Bronson
 *
 */
public class ComplexObjectWriter<T extends ComplexTypeDocumentationBuilder<?>> extends FacetOwnerWriterImpl<T> implements
		AliasOwnerWriter{

	

	public ComplexObjectWriter(T member,
			DocumentationBuilder prev,
			DocumentationBuilder next) throws Exception {
		super(member, prev, next);
	}

	/* (non-Javadoc)
	 * @see org.opentravel.schemacompiler.codegen.html.writers.AliasOwnerWriter#addAliasInfo(org.opentravel.schemacompiler.codegen.html.Content)
	 */
	@Override
	public void addAliasInfo(Content aliasTree) {
		if (member.getAliases().size() > 0) {
			AliasInfoWriter aliasWriter = new AliasInfoWriter(this,
					member);
			aliasWriter.addInfo(aliasTree);
		}
	}

}
