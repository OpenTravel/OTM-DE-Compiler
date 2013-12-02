/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.jaxb13_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_03.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_03.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_03.ValueWithAttributes;

import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExample;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.DefaultTransformerContext;

/**
 * Handles the transformation of objects from the <code>ValueWithAttributes</code> type to the
 * <code>TLValueWithAttributes</code> type.
 *
 * @author S. Livezey
 */
public class ValueWithAttributesTransformer extends ComplexTypeTransformer<ValueWithAttributes,TLValueWithAttributes> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLValueWithAttributes transform(ValueWithAttributes source) {
		ObjectTransformer<Equivalent,TLEquivalent,DefaultTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(Equivalent.class, TLEquivalent.class);
		String exampleValue = trimString(source.getEx());
		final TLValueWithAttributes simpleType = new TLValueWithAttributes();
		
		simpleType.setName( trimString(source.getName()) );
		simpleType.setParentTypeName( trimString(source.getType()) );
		
		if (exampleValue != null) {
			TLExample example = new TLExample();
			
			example.setContext(LibraryTransformer.DEFAULT_CONTEXT_ID);
			example.setValue(exampleValue);
			simpleType.addExample(example);
		}
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
			
			simpleType.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		for (Equivalent sourceEquiv : source.getEquivalent()) {
			simpleType.addEquivalent( equivTransformer.transform(sourceEquiv) );
		}
		
		for (TLAttribute attribute : transformAttributes(source.getAttribute())) {
			simpleType.addAttribute(attribute);
		}
		
		for (TLIndicator indicator : transformIndicators(source.getIndicator())) {
			simpleType.addIndicator(indicator);
		}
		
		return simpleType;
	}
	
}
