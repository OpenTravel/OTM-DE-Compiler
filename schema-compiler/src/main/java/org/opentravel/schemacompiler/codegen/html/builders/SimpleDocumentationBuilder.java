/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.builders;

import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLSimple;

import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.NamedEntityWriter;

/**
 * @author Eric.Bronson
 *
 */
public class SimpleDocumentationBuilder extends
		NamedEntityDocumentationBuilder<TLSimple> {

	/**
	 * @param manager
	 */
	public SimpleDocumentationBuilder(TLSimple t) {
		super(t);
		TLAttributeType parentType = t.getParentType();
		superType = DocumentationBuilderFactory.getInstance()
				.getDocumentationBuilder(parentType);
	}

	@Override
	public void build() throws Exception {
		NamedEntityWriter<SimpleDocumentationBuilder> writer = new NamedEntityWriter<SimpleDocumentationBuilder>(
				this, prev, next);
		Content contentTree = writer.getHeader();
		writer.addMemberInheritanceTree(contentTree);
		Content classContentTree = writer.getContentHeader();
		Content tree = writer.getMemberTree(classContentTree);

		Content classInfoTree = writer.getMemberInfoItemTree();
		writer.addDocumentationInfo(classInfoTree);
		tree.addContent(classInfoTree);

		classContentTree.addContent(tree);
		contentTree.addContent(classContentTree);
		writer.addFooter(contentTree);
		writer.printDocument(contentTree);
		writer.close();
	}

	@Override
	public DocumentationBuilderType getDocType() {
		return DocumentationBuilderType.SIMPLE;
	}
}
