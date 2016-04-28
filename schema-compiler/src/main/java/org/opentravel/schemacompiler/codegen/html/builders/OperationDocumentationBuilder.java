/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.builders;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLOperation;

import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.OperationWriter;

/**
 * @author Eric.Bronson
 *
 */
public class OperationDocumentationBuilder extends
		FacetOwnerDocumentationBuilder<TLOperation> {

	/**
	 * @param manager
	 */
	public OperationDocumentationBuilder(TLOperation t) {
		super(t);
		TLExtension tle = t.getExtension();
		if (tle != null) {
			superType = new OperationDocumentationBuilder(
					(TLOperation) tle.getExtendsEntity());
		}
	}

	@Override
	public void build() throws Exception {
		OperationWriter writer = new OperationWriter(this, prev, next);
		Content contentTree = writer.getHeader();
		Content classContentTree = writer.getContentHeader();
		Content tree = writer.getMemberTree(classContentTree);
		
		Content classInfoTree = writer.getMemberInfoItemTree();
		writer.addDocumentationInfo(classInfoTree);
		tree.addContent(classInfoTree);
		
		classInfoTree = writer.getMemberInfoItemTree();
		writer.addFacetInfo(classInfoTree);
		tree.addContent(classInfoTree);
		
		classContentTree.addContent(tree);
		contentTree.addContent(classContentTree);
		writer.addFooter(contentTree);
		writer.printDocument(contentTree);
		writer.close();
		super.build();
	}

	@Override
	protected void initializeFacets(TLOperation t) {
		for (TLFacet facet : FacetCodegenUtils.getAllFacetsOfType(element,
				TLFacetType.RESPONSE)) {
			if (shouldAddFacet(facet)) {
				FacetDocumentationBuilder facetBuilder = new FacetDocumentationBuilder(
						facet);
				facets.add(facetBuilder);
				facetBuilder.setOwner(this);
			}
		}

		for (TLFacet facet : FacetCodegenUtils.getAllFacetsOfType(element,
				TLFacetType.REQUEST)) {
			if (shouldAddFacet(facet)) {
				FacetDocumentationBuilder facetBuilder = new FacetDocumentationBuilder(
						facet);
				facets.add(facetBuilder);
				facetBuilder.setOwner(this);
			}
		}

		for (TLFacet facet : FacetCodegenUtils.getAllFacetsOfType(element,
				TLFacetType.NOTIFICATION)) {
			if (shouldAddFacet(facet)) {
				FacetDocumentationBuilder facetBuilder = new FacetDocumentationBuilder(
						facet);
				facets.add(facetBuilder);
				facetBuilder.setOwner(this);
			}
		}
	}

	@Override
	public DocumentationBuilderType getDocType() {
		return DocumentationBuilderType.OPERATION;
	}

}
