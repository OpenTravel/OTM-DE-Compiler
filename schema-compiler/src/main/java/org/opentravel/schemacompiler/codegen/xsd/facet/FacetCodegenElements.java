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
package org.opentravel.schemacompiler.codegen.xsd.facet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAlias;
import org.w3._2001.xmlschema.Element;

/**
 * Container that correlates XSD elements with the model elements from which they were constructed.
 * 
 * @author S. Livezey
 */
public class FacetCodegenElements {

    private Map<NamedEntity,Element> subgroupElements = new HashMap<>();
    private Map<NamedEntity,List<Element>> facetElements = new HashMap<>();

    /**
     * Adds/merges the contents of the given <code>FacetCodegenElements</code> with this instance.
     * 
     * @param facetElements
     *            the collection of facet elements to merge with this instance
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
     * @param elementOwner
     *            the element owner that was used to construct the given element
     * @param element
     *            the XSD element to add
     */
    public void addSubstitutionGroupElement(NamedEntity elementOwner, Element element) {
        if (element != null) {
            subgroupElements.put(elementOwner, element);
        }
    }

    /**
     * Returns the top-level element for the owner's substitution group, if one was created.
     * 
     * @param elementOwner
     *            the owner for which to return the substitution group element
     * @return Element
     */
    public Element getSubstitutionGroupElement(NamedEntity elementOwner) {
        return subgroupElements.get(elementOwner);
    }

    /**
     * Adds an element that will be associated with the given facet owner.
     * 
     * @param elementOwner
     *            the element owner whose facet was used to construct the given element
     * @param element
     *            the XSD element to add
     */
    public void addFacetElement(NamedEntity elementOwner, Element element) {
        if (element != null) {
            facetElements.computeIfAbsent( elementOwner, o -> facetElements.put( o, new ArrayList<>() ) );
            facetElements.get( elementOwner ).add( element );
        }
    }

    /**
     * Returns the list of facet elements that are associated with the given facet owner.
     * 
     * @param elementOwner
     *            the owner for which to return the facet element
     * @return List<Element>
     */
    public List<Element> getFacetElements(NamedEntity elementOwner) {
        List<Element> elementList = facetElements.get(elementOwner);

        if (elementList == null) {
            elementList = new ArrayList<>();
        }
        return elementList;
    }
    
    /**
     * Returns the list of all elements for all entities in this collection.
     * 
     * @return List<Element>
     */
    public List<Element> getAllFacetElements() {
    	List<Element> elementList = new ArrayList<>();
    	
    	for (NamedEntity entity : facetElements.keySet()) {
    		if (!(entity instanceof TLAlias)) {
    			elementList.addAll( facetElements.get( entity ) );
    		}
    	}
    	for (NamedEntity entity : facetElements.keySet()) {
    		if (entity instanceof TLAlias) {
    			elementList.addAll( facetElements.get( entity ) );
    		}
    	}
    	return elementList;
    }
    
}
