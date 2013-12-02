/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.tl2jaxb;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.ns.ota2.librarymodel_v01_04.Attribute;
import org.opentravel.ns.ota2.librarymodel_v01_04.Indicator;
import org.opentravel.ns.ota2.librarymodel_v01_04.Property;

import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;

/**
 * Base class for transformers that must handle nested type definitions such as attributes,
 * elements, and indicators.
 *
 * @param <S>  the source type of the object transformation
 * @param <T>  the target type of the object transformation
 * @author S. Livezey
 */
public abstract class TLComplexTypeTransformer<S,T> extends BaseTransformer<S,T,SymbolResolverTransformerContext> {
	
	/**
	 * Extracts the list of alias names from the given list of model entities and returns them as
	 * a simple list of strings.
	 * 
	 * @param aliases  the list of alias model entities to process
	 * @return List<String>
	 */
	protected List<String> getAliasNames(List<TLAlias> aliases) {
		List<String> aliasNames = new ArrayList<String>();
		
		for (TLAlias alias : aliases) {
			String aliasName = trimString(alias.getName());
			
			if (aliasName != null) {
				aliasNames.add( aliasName );
			}
		}
		return aliasNames;
	}
	
	/**
	 * Handles the transformation of <code>TLModel</code> attributes into their JAXB equivalents.
	 * 
	 * @param modelAttributes  the list of model attributes to convert
	 * @return List<Attribute>
	 */
	protected List<Attribute> transformAttributes(List<TLAttribute> modelAttributes) {
		ObjectTransformer<TLAttribute,Attribute,SymbolResolverTransformerContext> attributeTransformer =
				getTransformerFactory().getTransformer(TLAttribute.class, Attribute.class);
		List<Attribute> attributes = new ArrayList<Attribute>();
		
		if (modelAttributes != null) {
			for (TLAttribute modelAttribute : modelAttributes) {
				attributes.add( attributeTransformer.transform(modelAttribute) );
			}
		}
		return attributes;
	}
	
	/**
	 * Handles the transformation of <code>TLModel</code> properties into their JAXB equivalents.
	 * 
	 * @param modelProperties  the list of model properties to convert
	 * @return List<Property>
	 */
	protected List<Property> transformElements(List<TLProperty> modelProperties) {
		ObjectTransformer<TLProperty,Property,SymbolResolverTransformerContext> propertyTransformer =
				getTransformerFactory().getTransformer(TLProperty.class, Property.class);
		List<Property> properties = new ArrayList<Property>();
		
		if (modelProperties != null) {
			for (TLProperty modelProperty : modelProperties) {
				properties.add( propertyTransformer.transform(modelProperty) );
			}
		}
		return properties;
	}
	
	/**
	 * Handles the transformation of <code>TLModel</code> indicators into their JAXB equivalents.
	 * 
	 * @param jaxbIndicators  the list of JAXB indicators to convert
	 * @param symbolResolver  the symbol resolver to use for type name construction
	 * @return List<Indicator>
	 */
	protected List<Indicator> transformIndicators(List<TLIndicator> modelIndicators) {
		ObjectTransformer<TLIndicator,Indicator,SymbolResolverTransformerContext> indicatorTransformer =
				getTransformerFactory().getTransformer(TLIndicator.class, Indicator.class);
		List<Indicator> indicators = new ArrayList<Indicator>();
		
		if (modelIndicators != null) {
			for (TLIndicator modelIndicator : modelIndicators) {
				indicators.add( indicatorTransformer.transform(modelIndicator) );
			}
		}
		return indicators;
	}
	
}
