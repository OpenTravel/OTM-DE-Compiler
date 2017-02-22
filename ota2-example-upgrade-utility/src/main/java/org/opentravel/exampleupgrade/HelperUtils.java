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

package org.opentravel.exampleupgrade;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.w3c.dom.Element;

/**
 * Static utility methods for the Example Helper application.
 */
public class HelperUtils {
	
	private static FacetCodegenDelegateFactory facetDelegateFactory = new FacetCodegenDelegateFactory(null);
	private static VersionScheme otaVersionScheme;
	
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
		
		addContextualFacets( businessObject.getCustomFacets(), facetList, new HashSet<TLContextualFacet>() );
		addContextualFacets( FacetCodegenUtils.findGhostFacets( businessObject, TLFacetType.CUSTOM ),
				facetList, new HashSet<TLContextualFacet>() );
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

		addContextualFacets( choiceObject.getChoiceFacets(), facetList, new HashSet<TLContextualFacet>() );
		addContextualFacets( FacetCodegenUtils.findGhostFacets( choiceObject, TLFacetType.CHOICE ),
				facetList, new HashSet<TLContextualFacet>() );
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
	 * Recursive method that adds the list of child facets to the list of contextual facets.
	 * 
	 * @param facetsToAdd  the list of facets to add
	 * @param contextualFacets  the final list of contextual facets being assembled
	 * @param visitedFacets  collection of facets already visited (prevents infinite loops)
	 */
	private static void addContextualFacets(List<TLContextualFacet> facetsToAdd,
			List<TLFacet> contextualFacets, Set<TLContextualFacet> visitedFacets) {
		for (TLContextualFacet facet : facetsToAdd) {
			if (!visitedFacets.contains( facet )) {
				visitedFacets.add( facet );
				addIfContentExists( facet, contextualFacets );
				addContextualFacets( facet.getChildFacets(), contextualFacets, visitedFacets );
				addContextualFacets( FacetCodegenUtils.findGhostFacets( facet, facet.getFacetType() ),
						contextualFacets, visitedFacets );
			}
		}
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
	 * Returns a QName for the given DOM element.
	 * 
	 * @param domElement  the DOM element for which to return a qualified name
	 * @return QName
	 */
	public static QName getElementName(Element domElement) {
		String prefix = domElement.getPrefix();
		
		return new QName( domElement.getNamespaceURI(),
				domElement.getLocalName(), (prefix == null) ? "" : prefix );
	}
	
	/**
	 * Returns the base namespace of the given namespace.
	 * 
	 * @param ns  the namespace for which to return the base
	 * @return String
	 */
	public static String getBaseNamespace(String ns) {
		return otaVersionScheme.getBaseNamespace( ns );
	}
	
	/**
	 * Initializes the OTA2 version scheme.
	 */
	static {
		try {
			VersionSchemeFactory factory = VersionSchemeFactory.getInstance();
			otaVersionScheme = factory.getVersionScheme( factory.getDefaultVersionScheme() );
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}
	
}
