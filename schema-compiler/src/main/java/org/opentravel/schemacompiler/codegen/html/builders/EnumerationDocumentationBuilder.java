/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.builders;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;

import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.EnumerationWriter;

/**
 * @author Eric.Bronson
 *
 */
public class EnumerationDocumentationBuilder extends
		NamedEntityDocumentationBuilder<TLAbstractEnumeration> {

	private boolean isOpen;

	List<EnumValueDocumentationBuilder> values = new ArrayList<EnumValueDocumentationBuilder>();

	/**
	 * @param manager
	 */
	public EnumerationDocumentationBuilder(TLAbstractEnumeration t) {
		super(t);
		if (t instanceof TLOpenEnumeration) {
			isOpen = true;
		}
		for (TLEnumValue value : t.getValues()) {
			values.add(new EnumValueDocumentationBuilder(value));
		}
	}

	public boolean isOpen() {
		return isOpen;
	}

	public List<EnumValueDocumentationBuilder> getValues() {
		return values;
	}

	@Override
	public DocumentationBuilderType getDocType() {
		return isOpen ? DocumentationBuilderType.OPEN_ENUM
				: DocumentationBuilderType.CLOSED_ENUM;
	}

	@Override
	public void build() throws Exception {
		EnumerationWriter writer = new EnumerationWriter(this, prev, next);
		Content contentTree = writer.getHeader();
		Content classContentTree = writer.getContentHeader();
		Content tree = writer.getMemberTree(classContentTree);
		
		Content classInfoTree = writer.getMemberInfoItemTree();
		writer.addDocumentationInfo(classInfoTree);
		tree.addContent(classInfoTree);
		
		classInfoTree = writer.getMemberInfoItemTree();
		writer.addValueInfo(classInfoTree);
		tree.addContent(classInfoTree);
		
		Content desc = writer.getMemberInfoTree(tree);
		classContentTree.addContent(desc);
		contentTree.addContent(classContentTree);
		writer.addFooter(contentTree);
		writer.printDocument(contentTree);
		writer.close();
	}

}
