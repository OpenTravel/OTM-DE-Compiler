/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.codegen.xsd.facet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.w3._2001.xmlschema.Element;

/**
 * Container that correlates XSD elements with the model elements from which they were constructed.
 * 
 * @author S. Livezey
 */
public class FacetCodegenElements {
	
	private Map<NamedEntity,Element> subgroupElements = new HashMap<NamedEntity,Element>();
	private Map<NamedEntity,List<Element>> facetElements = new HashMap<NamedEntity,List<Element>>();
	
	/**
	 * Adds/merges the contents of the given <code>FacetCodegenElements</code> with this instance.
	 * 
	 * @param facetElements  the collection of facet elements to merge with this instance
	 */
	public void addAll(FacetCodegenElements otherFacetElements) {
		if (facetElements != null) {
			this.subgroupElements.putAll(otherFacetElements.subgroupElements);
			
			for (NamedEntity otherFacetOwner : otherFacetElements.facetElements.keySet()) {
				List<Element> otherElements = otherFacetElements.getFacetElements(otherFacetOwner);
				
				for (Element otherElement : otherElements) {
					this.addFacetElement(otherFacetOwner, otherElement);
				}
			}
		}
	}
	
	/**
	 * Adds the top-level element for the owner's substitution group.
	 * 
	 * @param elementOwner  the element owner that was used to construct the given element
	 * @param element  the XSD element to add
	 */
	public void addSubstitutionGroupElement(NamedEntity elementOwner, Element element) {
		if (element != null) {
			subgroupElements.put(elementOwner, element);
		}
	}
	
	/**
	 * Returns the top-level element for the owner's substitution group, if one was created.
	 * 
	 * @param elementOwner  the owner for which to return the substitution group element
	 * @return Element
	 */
	public Element getSubstitutionGroupElement(NamedEntity elementOwner) {
		return subgroupElements.get(elementOwner);
	}
	
	/**
	 * Adds an element that will be associated with the given facet owner.
	 * 
	 * @param elementOwner  the element owner whose facet was used to construct the given element
	 * @param element  the XSD element to add
	 */
	public void addFacetElement(NamedEntity elementOwner, Element element) {
		if (element != null) {
			List<Element> elementList = facetElements.get(elementOwner);
			
			if (elementList == null) {
				elementList = new ArrayList<Element>();
				facetElements.put(elementOwner, elementList);
			}
			elementList.add(element);
		}
	}
	
	/**
	 * Returns the list of facet elements that are associated with the given facet owner.
	 * 
	 * @param elementOwner  the owner for which to return the facet element
	 * @return List<Element>
	 */
	public List<Element> getFacetElements(NamedEntity elementOwner) {
		List<Element> elementList = facetElements.get(elementOwner);
		
		if (elementList == null) {
			elementList = new ArrayList<Element>();
		}
		return elementList;
	}
	
}
