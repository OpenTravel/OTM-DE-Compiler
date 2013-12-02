/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Example;
import org.opentravel.ns.ota2.librarymodel_v01_04.Property;

import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExample;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.model.TLPropertyType;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLProperty</code> type to the
 * <code>Property</code> type.
 *
 * @author S. Livezey
 */
public class TLPropertyTransformer extends BaseTransformer<TLProperty,Property,SymbolResolverTransformerContext> {
	
	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public Property transform(TLProperty source) {
		ObjectTransformer<TLEquivalent,Equivalent,SymbolResolverTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(TLEquivalent.class, Equivalent.class);
		ObjectTransformer<TLExample,Example,SymbolResolverTransformerContext> exTransformer =
				getTransformerFactory().getTransformer(TLExample.class, Example.class);
		TLPropertyType propertyType = source.getType();
		Property property = new Property();
		
		property.setName( trimString(source.getName(), false) );
		property.setRepeat( convertRepeatValue(source.getRepeat()) );
		property.setMandatory(source.isMandatory() ? Boolean.TRUE : null);
		property.setReference(source.isReference() ? Boolean.TRUE : null);
		
		if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
			ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(TLDocumentation.class, Documentation.class);
			
			property.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		for (TLEquivalent sourceEquiv : source.getEquivalents()) {
			property.getEquivalent().add( equivTransformer.transform(sourceEquiv) );
		}
		
		for (TLExample sourceEx : source.getExamples()) {
			property.getExample().add( exTransformer.transform(sourceEx) );
		}
		
		if (source.getType() != null) {
			property.setType( context.getSymbolResolver().buildEntityName(
					propertyType.getNamespace(), propertyType.getLocalName()) );
		} else {
			property.setType( trimString(source.getTypeName(), false) );
		}
		return property;
	}
	
	private String convertRepeatValue(int repeatInt) {
		return (repeatInt < 0) ? UNLIMITED_TOKEN : (repeatInt + "");
	}
	
}
