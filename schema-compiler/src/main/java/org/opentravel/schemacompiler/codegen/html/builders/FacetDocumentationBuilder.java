/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemacompiler.codegen.html.builders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.FacetWriter;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLProperty;

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
		properties = new ArrayList<>();
		for (TLProperty prop : t.getElements()) {
			properties.add(new PropertyDocumentationBuilder(prop));
		}
		TLFacet superFacet = getSuperFacet(t);

		if (superFacet != null && superFacet.declaresContent()) {
			superType = DocumentationBuilderFactory.getInstance()
					.getDocumentationBuilder(superFacet);
		}
		aliases = new ArrayList<>();
		for (TLAlias alias : t.getAliases()) {
			aliases.add(alias.getLocalName());
		}
	}

	private TLFacet getSuperFacet(TLFacet t) {
		TLFacet superFacet = null;
		TLFacetOwner facetOwner = t.getOwningEntity();
		
		// First, look for a contextual facet owner that declares content
		while (facetOwner instanceof TLContextualFacet) {
			TLContextualFacet owningFacet = (TLContextualFacet) facetOwner;
			
			if (owningFacet.declaresContent()) {
				break;
				
			} else {
				t = owningFacet;
				facetOwner = t.getOwningEntity();
			}
		}
		
		if (facetOwner instanceof TLContextualFacet) {
			superFacet = (TLContextualFacet) facetOwner;
		}
		
		if (superFacet == null) {
			switch (t.getFacetType()) {
				case CUSTOM:
				case DETAIL:
					superFacet = getCustomOrDetailSuperfacet(facetOwner);
					break;
				case CHOICE:
					superFacet = getChoiceSuperfacet(facetOwner);
					break;
				case SUMMARY:
					superFacet = FacetCodegenUtils.getFacetOfType(facetOwner, TLFacetType.ID);
					break;
				default:
					break;
			}
		}
		return superFacet;
	}

	/**
	 * Returns the super-facet for the given custom or detail facet owner.
	 * 
	 * @param facetOwner  the owner of the custom or detail facet
	 * @return TLFacet
	 */
	private TLFacet getCustomOrDetailSuperfacet(TLFacetOwner facetOwner) {
		TLFacet superFacet;
		superFacet = FacetCodegenUtils.getFacetOfType(facetOwner, TLFacetType.SUMMARY);
		
		if (!superFacet.declaresContent()) {
			TLFacetOwner ext = FacetCodegenUtils.getFacetOwnerExtension(facetOwner);
			
			while (ext != null) {
				TLFacet extFacet = FacetCodegenUtils.getFacetOfType(ext, TLFacetType.SUMMARY);
				
				if (extFacet.declaresContent()) {
					superFacet = extFacet;
					ext = null;
				} else {
					ext = FacetCodegenUtils.getFacetOwnerExtension(ext);
				}
			}

		}
		if (!superFacet.declaresContent()) {
			superFacet = FacetCodegenUtils.getFacetOfType(facetOwner, TLFacetType.ID);
		}
		return superFacet;
	}

	/**
	 * Returns the super-facet for the given choice facet owner.
	 * 
	 * @param facetOwner  the owner of the choice facet
	 * @return TLFacet
	 */
	private TLFacet getChoiceSuperfacet(TLFacetOwner facetOwner) {
		TLFacet superFacet;
		superFacet = FacetCodegenUtils.getFacetOfType(facetOwner, TLFacetType.SHARED);
		
		if (!superFacet.declaresContent()) {
			TLFacetOwner ext = FacetCodegenUtils.getFacetOwnerExtension(facetOwner);
			
			while (ext != null) {
				TLFacet extFacet = FacetCodegenUtils.getFacetOfType(ext, TLFacetType.SUMMARY);
				
				if (extFacet.declaresContent()) {
					superFacet = extFacet;
					ext = null;
				} else {
					ext = FacetCodegenUtils.getFacetOwnerExtension(ext);
				}
			}

		}
		return superFacet;
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

	@SuppressWarnings("unchecked")
	public <T extends TLFacetOwner & TLDocumentationOwner> FacetOwnerDocumentationBuilder<T> getOwner() {
		return (FacetOwnerDocumentationBuilder<T>) owner;
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
	public void build() throws CodeGenerationException {
		try {
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
			
		} catch (IOException e) {
			throw new CodeGenerationException("Error creating doclet writer.", e);
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

}
