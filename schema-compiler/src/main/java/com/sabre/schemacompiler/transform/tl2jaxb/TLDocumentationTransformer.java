/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.tl2jaxb;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.ns.ota2.librarymodel_v01_04.AdditionalDoc;
import org.opentravel.ns.ota2.librarymodel_v01_04.Description;
import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;

import com.sabre.schemacompiler.model.TLAdditionalDocumentationItem;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLDocumentationItem;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLDocumentation</code> type to the
 * <code>Documentation</code> type.
 *
 * @author S. Livezey
 */
public class TLDocumentationTransformer extends BaseTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> {

	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public Documentation transform(TLDocumentation source) {
		Description jaxbDescription = new Description();
		Documentation target = new Documentation();
		
		if (source.getDescription() != null) {
			jaxbDescription.setValue( trimString(source.getDescription(), false) );
		} else {
			jaxbDescription.setValue("");
		}
		target.setDescription(jaxbDescription);
		
		target.getDeprecated().addAll( buildDescriptions(source.getDeprecations()) );
		target.getImplementer().addAll( buildDescriptions(source.getImplementers()) );
		target.getReference().addAll( buildTexts(source.getReferences()) );
		target.getMoreInfo().addAll( buildTexts(source.getMoreInfos()) );
		
		for (TLAdditionalDocumentationItem sourceOtherDoc : source.getOtherDocs()) {
			if (sourceOtherDoc != null) {
				AdditionalDoc otherDoc = new AdditionalDoc();
				
				otherDoc.setContext( trimString(sourceOtherDoc.getContext(), false) );
				otherDoc.setValue( trimString(sourceOtherDoc.getText(), false) );
				target.getOtherDoc().add(otherDoc);
			}
		}
		return target;
	}
	
	/**
	 * Constructs a list of JAXB descriptions using the list of <code>TLDocumentationItems</code> provided.
	 * 
	 * @param items  the list of documentation items to convert
	 * @return List<Description>
	 */
	private List<Description> buildDescriptions(List<TLDocumentationItem> items) {
		List<Description> result = new ArrayList<Description>();
		
		if (items != null) {
			for (TLDocumentationItem item : items) {
				String text = (item == null) ? null : trimString(item.getText());
				Description jaxbDescription = new Description();
				
				jaxbDescription.setValue( (text == null) ? "" : text );
				result.add(jaxbDescription);
			}
		}
		return result;
	}
	
	/**
	 * Constructs a list of strings using the list of <code>TLDocumentationItems</code> provided.
	 * 
	 * @param items  the list of documentation items to convert
	 * @return List<String>
	 */
	private List<String> buildTexts(List<TLDocumentationItem> items) {
		List<String> result = new ArrayList<String>();
		
		if (items != null) {
			for (TLDocumentationItem item : items) {
				String text = (item == null) ? null : trimString(item.getText());
				
				result.add( (text == null) ? "" : text );
			}
		}
		return result;
	}
	
}
