/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers;

import org.opentravel.schemacompiler.codegen.html.builders.NamedEntityDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.SimpleDocumentationBuilder;

/**
 * @author Eric.Bronson
 *
 */
public class SimpleWriter extends NamedEntityWriter<SimpleDocumentationBuilder> {

	public SimpleWriter(SimpleDocumentationBuilder member,
			NamedEntityDocumentationBuilder<?> prev,
			NamedEntityDocumentationBuilder<?> nextClass) throws Exception {
		super(member, prev, nextClass);
	}
	
}
