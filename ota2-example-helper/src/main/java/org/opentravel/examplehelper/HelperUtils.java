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

package org.opentravel.examplehelper;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOperation;

/**
 * Static utility methods for the Example Helper application.
 */
public class HelperUtils {
	
	private static FacetCodegenDelegateFactory facetDelegateFactory = new FacetCodegenDelegateFactory(null);
	
	/**
	 * Returns the list of available facets for the substitution group.
	 * 
	 * @param businessObject  the business object for which to return available facets
	 * @return List<TLFacet>
	 */
	public static List<TLFacet> getAvailableFacets(TLBusinessObject businessObject) {
		List<TLFacet> facetList = new ArrayList<>();

		addIfContentExists(businessObject.getIdFacet(), facetList);
		addIfContentExists(businessObject.getSummaryFacet(), facetList);
		addIfContentExists(businessObject.getDetailFacet(), facetList);
		
		for (TLFacet facet : businessObject.getCustomFacets()) {
			addIfContentExists(facet, facetList);
		}
        for (TLFacet ghostFacet :
        	FacetCodegenUtils.findGhostFacets(businessObject, TLFacetType.CUSTOM)) {
			addIfContentExists(ghostFacet, facetList);
        }
		return facetList;
	}
	
	/**
	 * Returns the list of available facets for the substitution group.
	 * 
	 * @param coreObject  the core object for which to return available facets
	 * @return List<TLFacet>
	 */
	public static List<TLFacet> getAvailableFacets(TLCoreObject coreObject) {
		List<TLFacet> facetList = new ArrayList<>();

		addIfContentExists(coreObject.getSummaryFacet(), facetList);
		addIfContentExists(coreObject.getDetailFacet(), facetList);
		return facetList;
	}
	
	/**
	 * Returns the list of available facets for the substitution group.
	 * 
	 * @param choiceObject  the choice object for which to return available facets
	 * @return List<TLFacet>
	 */
	public static List<TLFacet> getAvailableFacets(TLChoiceObject choiceObject) {
		List<TLFacet> facetList = new ArrayList<>();

		for (TLFacet facet : choiceObject.getChoiceFacets()) {
			addIfContentExists(facet, facetList);
		}
        for (TLFacet ghostFacet :
        	FacetCodegenUtils.findGhostFacets(choiceObject, TLFacetType.CHOICE)) {
			addIfContentExists(ghostFacet, facetList);
        }
		return facetList;
	}
	
	/**
	 * Returns the list of available facets for the operation.
	 * 
	 * @param operation  the operation for which to return available facets
	 * @return List<TLFacet>
	 */
	public static List<TLFacet> getAvailableFacets(TLOperation operation) {
		List<TLFacet> facetList = new ArrayList<>();
		
		addIfContentExists(operation.getRequest(), facetList);
		addIfContentExists(operation.getResponse(), facetList);
		addIfContentExists(operation.getNotification(), facetList);
		return facetList;
	}
	
	/**
	 * If the given facet declares or inherits fields, this method will add
	 * it to the list provided.
	 * 
	 * @param facet  the facet to verify and add
	 * @param facetList  the list of facets to which the given one may be appended
	 */
	private static void addIfContentExists(TLFacet facet, List<TLFacet> facetList) {
		if (facetDelegateFactory.getDelegate(facet).hasContent()) {
			facetList.add(facet);
		}
	}
	
	/**
	 * Returns a display name label for the given OTM entity.
	 * 
	 * @param entity  the entity for which to return a display name
	 * @param showPrefix  flag indicating whether the owning library's prefix should be included in the label
	 * @return String
	 */
	public static String getDisplayName(NamedEntity entity, boolean showPrefix) {
		TLLibrary library = (TLLibrary) entity.getOwningLibrary();
		QName elementName = XsdCodegenUtils.getGlobalElementName(entity);
		String localName = (elementName != null) ? elementName.getLocalPart() : entity.getLocalName();
		StringBuilder displayName = new StringBuilder();
		
		if (showPrefix && (library.getPrefix() != null)) {
			displayName.append( library.getPrefix() ).append( ":" );
		}
		displayName.append( localName );
		
		return displayName.toString();
	}
	
}
