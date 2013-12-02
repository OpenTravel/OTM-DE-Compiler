/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Example;
import org.opentravel.ns.ota2.librarymodel_v01_04.Property;

import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExample;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.DefaultTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Property</code> type to the
 * <code>TLProperty</code> type.
 *
 * @author S. Livezey
 */
public class PropertyTransformer extends BaseTransformer<Property,TLProperty,DefaultTransformerContext> {
	
	@Override
	public TLProperty transform(Property source) {
		ObjectTransformer<Equivalent,TLEquivalent,DefaultTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(Equivalent.class, TLEquivalent.class);
		ObjectTransformer<Example,TLExample,DefaultTransformerContext> exampleTransformer =
				getTransformerFactory().getTransformer(Example.class, TLExample.class);
		final TLProperty property = new TLProperty();
		String propertyTypeName = source.getType();
		
		property.setName( trimString(source.getName()) );
		property.setRepeat( convertRepeatValue( trimString(source.getRepeat()) ) );
		property.setMandatory( (source.isMandatory() == null) ? false : source.isMandatory().booleanValue() );
		property.setReference( (source.isReference() == null) ? false : source.isReference().booleanValue() );
		property.setTypeName( trimString(propertyTypeName) );
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
			
			property.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		for (Equivalent sourceEquiv : source.getEquivalent()) {
			property.addEquivalent( equivTransformer.transform(sourceEquiv) );
		}
		
		for (Example sourceExample : source.getExample()) {
			property.addExample( exampleTransformer.transform(sourceExample) );
		}
		
		return property;
	}
	
	/**
	 * If the string represents an integer value, that value will be returned.  A string
	 * value of "unlimited" will be result in a -1 return value.  Any other non-numeric strings
	 * will result in a zero return value.
	 * 
	 * @param repeatStr  the repeat string value to convert
	 * @return int
	 */
	private int convertRepeatValue(String repeatStr) {
		int result = 0;
		
		if (repeatStr != null) {
			if (repeatStr.equalsIgnoreCase(UNLIMITED_TOKEN)) {
				result = -1;
			} else {
				try {
					result = Integer.parseInt(repeatStr);
					
				} catch (NumberFormatException e) {
					// Ignore - method will return zero
				}
			}
		}
		return result;
	}
	
}
