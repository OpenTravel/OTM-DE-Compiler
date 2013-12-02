/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Example;
import org.opentravel.ns.ota2.librarymodel_v01_04.ValueWithAttributes;

import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExample;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Handles the transformation of objects from the <code>TLValueWithAttributes</code> type to the
 * <code>ValueWithAttributes</code> type.
 *
 * @author S. Livezey
 */
public class TLValueWithAttributesTransformer extends TLComplexTypeTransformer<TLValueWithAttributes,ValueWithAttributes> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public ValueWithAttributes transform(TLValueWithAttributes source) {
		ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
				getTransformerFactory().getTransformer(TLDocumentation.class, Documentation.class);
		ObjectTransformer<TLEquivalent,Equivalent,SymbolResolverTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(TLEquivalent.class, Equivalent.class);
		ObjectTransformer<TLExample,Example,SymbolResolverTransformerContext> exTransformer =
				getTransformerFactory().getTransformer(TLExample.class, Example.class);
		NamedEntity parentType = source.getParentType();
		ValueWithAttributes simpleType = new ValueWithAttributes();
		
		simpleType.setName( trimString(source.getName(), false) );
		simpleType.getAttribute().addAll(transformAttributes(source.getAttributes()));
		simpleType.getIndicator().addAll(transformIndicators(source.getIndicators()));
		
		if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
			simpleType.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		if ((source.getValueDocumentation() != null) && !source.getValueDocumentation().isEmpty()) {
			simpleType.setValueDocumentation( docTransformer.transform(source.getValueDocumentation()) );
		}
		
		for (TLEquivalent sourceEquiv : source.getEquivalents()) {
			simpleType.getEquivalent().add( equivTransformer.transform(sourceEquiv) );
		}
		
		for (TLExample sourceEx : source.getExamples()) {
			simpleType.getExample().add( exTransformer.transform(sourceEx) );
		}
		
		if (parentType != null) {
			simpleType.setType( context.getSymbolResolver().buildEntityName(
					parentType.getNamespace(), parentType.getLocalName()) );
		} else {
			simpleType.setType( trimString(source.getParentTypeName(), false) );
		}
		return simpleType;
	}
	
}
