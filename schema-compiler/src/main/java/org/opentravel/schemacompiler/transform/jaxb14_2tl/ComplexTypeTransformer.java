/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.transform.jaxb14_2tl;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.ns.ota2.librarymodel_v01_04.Attribute;
import org.opentravel.ns.ota2.librarymodel_v01_04.Indicator;
import org.opentravel.ns.ota2.librarymodel_v01_04.Property;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Base class for transformers that must handle nested type definitions such as attributes,
 * elements, and indicators.
 * 
 * @param <S>  the source type of the object transformation
 * @param <T>  the target type of the object transformation
 * @author S. Livezey
 */
public abstract class ComplexTypeTransformer<S,T> extends BaseTransformer<S,T,DefaultTransformerContext> {
	
	/**
	 * Handles the transformation of JAXB attributes into their <code>TLAttributeType</code>
	 * equivalents.
	 * 
	 * @param jaxbAttributes  the list of JAXB attributes to convert
	 * @return List<TLAttribute>
	 */
	protected List<TLAttribute> transformAttributes(List<Attribute> jaxbAttributes) {
		ObjectTransformer<Attribute,TLAttribute,DefaultTransformerContext> attributeTransformer =
				getTransformerFactory().getTransformer(Attribute.class, TLAttribute.class);
		List<TLAttribute> attributes = new ArrayList<TLAttribute>();
		
		if (jaxbAttributes != null) {
			for (Attribute jaxbAttribute : jaxbAttributes) {
				attributes.add( attributeTransformer.transform(jaxbAttribute) );
			}
		}
		return attributes;
	}
	
	/**
	 * Handles the transformation of JAXB properties into their <code>TLProperty</code>
	 * equivalents.
	 * 
	 * @param jaxbProperties  the list of JAXB properties to convert
	 * @return List<TLProperty>
	 */
	protected List<TLProperty> transformElements(List<Property> jaxbProperties) {
		ObjectTransformer<Property,TLProperty,DefaultTransformerContext> propertyTransformer =
				getTransformerFactory().getTransformer(Property.class, TLProperty.class);
		List<TLProperty> properties = new ArrayList<TLProperty>();
		
		if (jaxbProperties != null) {
			for (Property jaxbProperty : jaxbProperties) {
				properties.add( propertyTransformer.transform(jaxbProperty) );
			}
		}
		return properties;
	}
	
	/**
	 * Handles the transformation of JAXB indicators into their <code>TLIndicator</code>
	 * equivalents.
	 * 
	 * @param jaxbIndicators  the list of JAXB indicators to convert
	 * @return List<TLIndicator>
	 */
	protected List<TLIndicator> transformIndicators(List<Indicator> jaxbIndicators) {
		ObjectTransformer<Indicator,TLIndicator,DefaultTransformerContext> indicatorTransformer =
				getTransformerFactory().getTransformer(Indicator.class, TLIndicator.class);
		List<TLIndicator> indicators = new ArrayList<TLIndicator>();
		
		if (jaxbIndicators != null) {
			for (Indicator jaxbAttribute : jaxbIndicators) {
				indicators.add( indicatorTransformer.transform(jaxbAttribute) );
			}
		}
		return indicators;
	}
	
}
