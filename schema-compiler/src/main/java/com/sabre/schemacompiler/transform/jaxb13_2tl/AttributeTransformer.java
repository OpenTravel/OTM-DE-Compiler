/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.jaxb13_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_03.Attribute;
import org.opentravel.ns.ota2.librarymodel_v01_03.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_03.Equivalent;

import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExample;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.DefaultTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Attribute</code> type to the
 * <code>TLAttribute</code> type.
 *
 * @author S. Livezey
 */
public class AttributeTransformer extends BaseTransformer<Attribute,TLAttribute,DefaultTransformerContext> {
	
	@Override
	public TLAttribute transform(Attribute source) {
		ObjectTransformer<Equivalent,TLEquivalent,DefaultTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(Equivalent.class, TLEquivalent.class);
		String exampleValue = trimString(source.getEx());
		String attributeTypeName = source.getType();
		final TLAttribute attribute = new TLAttribute();
		
		attribute.setName( trimString(source.getName()) );
		attribute.setMandatory( (source.isMandatory() == null) ? false : source.isMandatory().booleanValue() );
		attribute.setTypeName( trimString(attributeTypeName) );
		
		if (exampleValue != null) {
			TLExample example = new TLExample();
			
			example.setContext(LibraryTransformer.DEFAULT_CONTEXT_ID);
			example.setValue(exampleValue);
			attribute.addExample(example);
		}
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
			
			attribute.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		for (Equivalent sourceEquiv : source.getEquivalent()) {
			attribute.addEquivalent( equivTransformer.transform(sourceEquiv) );
		}
		
		return attribute;
	}
	
}
