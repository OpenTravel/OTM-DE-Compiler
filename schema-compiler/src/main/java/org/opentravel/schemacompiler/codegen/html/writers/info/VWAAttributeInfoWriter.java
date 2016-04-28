/**
 * 
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
