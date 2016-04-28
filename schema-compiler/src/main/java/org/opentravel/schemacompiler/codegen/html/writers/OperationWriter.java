/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers;

import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.OperationDocumentationBuilder;

/**
 * @author Eric.Bronson
 *
 */
public class OperationWriter extends FacetOwnerWriterImpl<OperationDocumentationBuilder> {

	public OperationWriter(OperationDocumentationBuilder member,
			DocumentationBuilder prev,
			DocumentationBuilder next) throws Exception {
		super(member, prev, next);
	}

	

	

}
