package org.opentravel.schemacompiler.codegen.html.writers;

import java.io.IOException;

import org.opentravel.schemacompiler.model.TLDocumentation;

import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.VWADocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.info.DocumentationInfoWriter;
import org.opentravel.schemacompiler.codegen.html.writers.info.ExampleInfoWriter;
import org.opentravel.schemacompiler.codegen.html.writers.info.InfoWriter;
import org.opentravel.schemacompiler.codegen.html.writers.info.VWAAttributeInfoWriter;
import org.opentravel.schemacompiler.codegen.html.writers.info.VWAIndicatorInfoWriter;

public class VWAWriter extends 
	NamedEntityWriter<VWADocumentationBuilder> implements
		FieldOwnerWriter {
	
	/**
	 * @param configuration
	 * @param filename
	 * @throws IOException
	 */
	public VWAWriter(VWADocumentationBuilder classDoc,
			DocumentationBuilder prev,
			DocumentationBuilder next) throws Exception {
		super(classDoc, prev, next);
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.html.writers.FieldOwnerWriter
	 * #getAttributeTree
	 * (org.opentravel.schemacompiler.codegen.html.Content)
	 */
	@Override
	public void addAttributeInfo(Content memberTree) {
		if (member.getAttributes().size() > 0) {
			VWAAttributeInfoWriter attWriter = new VWAAttributeInfoWriter(this,
					member);
			attWriter.addInfo(memberTree);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.html.writers.FieldOwnerWriter
	 * #getIndicatorTree
	 * (org.opentravel.schemacompiler.codegen.html.Content)
	 */
	@Override
	public void addIndicatorInfo(Content memberTree) {
		if (member.getIndicators().size() > 0) {
			VWAIndicatorInfoWriter attWriter = new VWAIndicatorInfoWriter(this,
					member);
			attWriter.addInfo(memberTree);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.html.writers.FieldOwnerWriter
	 * #getExampleTree(org.opentravel.schemacompiler.codegen.html.Content)
	 */
	@Override
	public void addExampleInfo(Content memberTree) {
		if ((member.getExampleJSON() != null && !"".equals(member.getExampleJSON()))
				|| (member.getExampleXML() != null && !"".equals(member.getExampleXML()))) {
			ExampleInfoWriter exampleWriter = new ExampleInfoWriter(this,
					member);
			exampleWriter.addInfo(memberTree);
		}
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.html.writers.LibraryMemberWriter
	 * #
	 * addMemberDescription(org.opentravel.schemacompiler.codegen.html.Content
	 * )
	 */
	@Override
	public void addDocumentationInfo(Content classInfoTree) {
		super.addDocumentationInfo(classInfoTree);
		addValueDocumentation(classInfoTree);
	}


	private void addValueDocumentation(Content classInfoTree) {
		TLDocumentation doc = member.getValueDoc();
		if(doc != null){
			InfoWriter docWriter = new DocumentationInfoWriter(this, doc);
			docWriter.setTitle(getResource("doclet.Value_Documentation_Summary"));
//			docWriter.setCaption(configuration.getText("doclet.Value_Documentation"));
			docWriter.addInfo(classInfoTree);
		}
	}


	@Override
	public void addPropertyInfo(Content memberTree) {
	}
	

}
