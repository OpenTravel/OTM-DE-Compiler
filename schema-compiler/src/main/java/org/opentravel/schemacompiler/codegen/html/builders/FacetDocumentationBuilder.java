/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.builders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.FacetWriter;

/**
 * @author Eric.Bronson
 *
 */
public class FacetDocumentationBuilder extends
		AttributeOwnerDocumentationBuilder<TLFacet> implements
		AliasOwnerDocumentationBuilder {

	protected List<String> aliases;

	protected FacetOwnerDocumentationBuilder<?> owner;

	protected List<PropertyDocumentationBuilder> properties;

	/**
	 * @param manager
	 */
	public FacetDocumentationBuilder(TLFacet t) {
		super(t);
		properties = new ArrayList<PropertyDocumentationBuilder>();
		for (TLProperty prop : t.getElements()) {
			properties.add(new PropertyDocumentationBuilder(prop));
		}
		TLFacet superFacet = null;
		TLFacetOwner owner = t.getOwningEntity();
		switch (t.getFacetType()) {
		case CUSTOM:
			superFacet = FacetCodegenUtils.getFacetOfType(owner,
					TLFacetType.SUMMARY);
			if (!superFacet.declaresContent()) {
				TLFacetOwner ext = FacetCodegenUtils
						.getFacetOwnerExtension(owner);
				while (ext != null) {
					TLFacet extFacet = FacetCodegenUtils.getFacetOfType(ext,
							TLFacetType.SUMMARY);
					if (extFacet.declaresContent()) {
						superFacet = extFacet;
						ext = null;
					} else {
						ext = FacetCodegenUtils.getFacetOwnerExtension(ext);
					}
				}

			}
			if (!superFacet.declaresContent()) {
				superFacet = FacetCodegenUtils.getFacetOfType(owner,
						TLFacetType.ID);
			}
			break;
		case DETAIL:
			superFacet = FacetCodegenUtils.getFacetOfType(owner,
					TLFacetType.SUMMARY);
				if (!superFacet.declaresContent()) {
					TLFacetOwner ext = FacetCodegenUtils
							.getFacetOwnerExtension(owner);
					while (ext != null) {
						TLFacet extFacet = FacetCodegenUtils.getFacetOfType(
								ext, TLFacetType.SUMMARY);
						if (extFacet.declaresContent()) {
							superFacet = extFacet;
							ext = null;
						} else {
							ext = FacetCodegenUtils
									.getFacetOwnerExtension(ext);
						}
					}

				}
			if (!superFacet.declaresContent()) {
				superFacet = FacetCodegenUtils.getFacetOfType(owner,
						TLFacetType.ID);
			}
			break;
		case SUMMARY:
			superFacet = FacetCodegenUtils.getFacetOfType(t.getOwningEntity(),
					TLFacetType.ID);
			break;
		default:
			break;
		}
		if (superFacet != null && superFacet.declaresContent()) {
			superType = DocumentationBuilderFactory.getInstance()
					.getDocumentationBuilder(superFacet);
		}
		aliases = new ArrayList<String>();
		for (TLAlias alias : t.getAliases()) {
			aliases.add(alias.getLocalName());
		}
	}

	protected String getLocalName() {
		String localName = name;
		int vIndex = name.lastIndexOf("Summary");
		if (vIndex > 0) {
			localName = name.substring(0, vIndex);
		}
		return localName;
	}

	public List<String> getAliases() {
		return Collections.unmodifiableList(aliases);
	}

	@Override
	public DocumentationBuilderType getDocType() {
		return DocumentationBuilderType.FACET;
	}

	public FacetOwnerDocumentationBuilder<?> getOwner() {
		return owner;
	}

	public List<PropertyDocumentationBuilder> getProperties() {
		return Collections.unmodifiableList(properties);
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(FacetOwnerDocumentationBuilder<?> owner) {
		this.owner = owner;
	}

	public TLFacetType getType() {
		return element.getFacetType();
	}

	@Override
	public void build() throws Exception {
		FacetWriter writer = new FacetWriter(this, prev, next);
		Content contentTree = writer.getHeader();
		writer.addMemberInheritanceTree(contentTree);
		Content classContentTree = writer.getContentHeader();
		Content tree = writer.getMemberTree(classContentTree);

		Content classInfoTree = writer.getMemberInfoItemTree();
		writer.addDocumentationInfo(classInfoTree);
		tree.addContent(classInfoTree);

		classInfoTree = writer.getMemberInfoItemTree();
		writer.addExampleInfo(classInfoTree);
		tree.addContent(classInfoTree);

		classInfoTree = writer.getMemberInfoItemTree();
		writer.addPropertyInfo(classInfoTree);
		tree.addContent(classInfoTree);

		classInfoTree = writer.getMemberInfoItemTree();
		writer.addAttributeInfo(classInfoTree);
		tree.addContent(classInfoTree);

		classInfoTree = writer.getMemberInfoItemTree();
		writer.addIndicatorInfo(classInfoTree);
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
	}

}
