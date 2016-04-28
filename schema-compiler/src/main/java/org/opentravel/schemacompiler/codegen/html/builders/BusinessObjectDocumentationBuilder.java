/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.builders;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;

import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.BusinessObjectWriter;

/**
 * @author Eric.Bronson
 *
 */
public class BusinessObjectDocumentationBuilder extends
		ComplexTypeDocumentationBuilder<TLBusinessObject> {

	public BusinessObjectDocumentationBuilder(TLBusinessObject t) {
		super(t);
		TLExtension tle = t.getExtension();
		if (tle != null) {
			superType = new BusinessObjectDocumentationBuilder(
					(TLBusinessObject) tle.getExtendsEntity());
		}
	}

	@Override
	protected void initializeFacets(TLBusinessObject t) {
		for (TLFacet facet : FacetCodegenUtils.getAllFacetsOfType(element,
				TLFacetType.ID)) {
			addFacet(facet);
		}

		for (TLFacet facet : FacetCodegenUtils.getAllFacetsOfType(element,
				TLFacetType.SUMMARY)) {
			if (shouldAddFacet(facet)) {
				addFacet(facet);
			}
		}

		for (TLFacet facet : FacetCodegenUtils.getAllFacetsOfType(element,
				TLFacetType.DETAIL)) {
			if (shouldAddFacet(facet)) {
				addFacet(facet);
			}
		}

		for (TLFacet facet : FacetCodegenUtils.getAllFacetsOfType(element,
				TLFacetType.CUSTOM)) {
			addFacet(facet);
		}

		for (TLFacet facet : FacetCodegenUtils.getAllFacetsOfType(element,
				TLFacetType.QUERY)) {
			addFacet(facet);
		}
	}

	@Override
	public DocumentationBuilderType getDocType() {
		return DocumentationBuilderType.BUSINESS_OBJECT;
	}
	
	@Override
	public void build() throws Exception {
		BusinessObjectWriter writer = new BusinessObjectWriter(this, prev, next);
		Content contentTree = writer.getHeader();
		writer.addMemberInheritanceTree(contentTree);
		Content classContentTree = writer.getContentHeader();
		Content tree = writer.getMemberTree(classContentTree);

		Content classInfoTree = writer.getMemberInfoItemTree();
		writer.addDocumentationInfo(classInfoTree);
		tree.addContent(classInfoTree);

		classInfoTree = writer.getMemberInfoItemTree();
		writer.addFacetInfo(classInfoTree);
		tree.addContent(classInfoTree);

		classInfoTree = writer.getMemberInfoItemTree();
		writer.addAliasInfo(classInfoTree);
		tree.addContent(classInfoTree);

		Content desc = writer.getMemberInfoTree(tree);
		classContentTree.addContent(desc);
		contentTree.addContent(classContentTree);
		writer.addFooter(contentTree);
		writer.printDocument(contentTree);
		writer.close();
		super.build();
	}

}
