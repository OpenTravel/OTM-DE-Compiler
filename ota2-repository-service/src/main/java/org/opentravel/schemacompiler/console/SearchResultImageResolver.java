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

package org.opentravel.schemacompiler.console;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opentravel.schemacompiler.index.SearchResult;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.repository.Release;
import org.opentravel.schemacompiler.repository.RepositoryItemType;

/**
 * Used to resolve the icon image for search results based on the entity type.
 * 
 * @author S. Livezey
 */
public class SearchResultImageResolver {
	
	private static final Map<Class<?>,String> imageMap;
	private static final String UNKNOWN_IMAGE = "unknown.png";
	
	/**
	 * Returns the filename of the image for the given object.
	 * 
	 * @param obj  the object for which to return an icon image filename
	 * @return String
	 */
	public String getIconImage(Object obj) {
		String imageFilename;
		
		if (obj instanceof SearchResult) {
			imageFilename = _getIconImage( (SearchResult<?>) obj );
			
		} else if (obj instanceof NamedEntity) {
			imageFilename = _getIconImage( (NamedEntity) obj );
			
		} else if (obj instanceof NamespaceItem) {
			imageFilename = _getIconImage( (NamespaceItem) obj );
			
		} else if (obj == null) {
			throw new NullPointerException("Object cannot be null.");
			
		} else {
			throw new IllegalArgumentException(
					"Unable to resolve icon image for object type: " + obj.getClass().getName());
		}
		return imageFilename;
	}
	
	/**
	 * Returns the filename of the image for the given search result item.
	 * 
	 * @param resultItem  the search result item for which to return an icon image
	 * @return String
	 */
	private String _getIconImage(SearchResult<?> resultItem) {
		return _getIconImage( resultItem.getEntityType() );
	}
	
	/**
	 * Returns the filename of the image for the given OTM entity.
	 * 
	 * @param entity  the OTM entity for which to return an icon image
	 * @return String
	 */
	private String _getIconImage(NamedEntity entity) {
		return _getIconImage( entity.getClass() );
	}
	
	/**
	 * Returns the filename of the image for the given namespace item.
	 * 
	 * @param resultItem  the namespace item for which to return an icon image
	 * @return String
	 */
	private String _getIconImage(NamespaceItem item) {
		Class<?> itemType = TLLibrary.class;
		
		if (RepositoryItemType.RELEASE.isItemType( item.getFilename() )) {
			itemType = Release.class;
		}
		return _getIconImage( itemType );
	}
	
	/**
	 * Returns the filename of the image for the given OTM entity type.
	 * 
	 * @param entityType  the OTM entity type for which to return an icon image
	 * @return String
	 */
	private String _getIconImage(Class<?> entityType) {
		String imageFilename = imageMap.get( entityType );
		
		if (imageFilename == null) {
			imageFilename = UNKNOWN_IMAGE;
		}
		return imageFilename;
	}
	
	/**
	 * Initializes the image mapping for each entity type.
	 */
	static {
		try {
			Map<Class<?>,String> _imageMap = new HashMap<>();
			
			_imageMap.put( Release.class, "release.gif" );
			_imageMap.put( TLLibrary.class, "library.png" );
			_imageMap.put( TLSimple.class, "simple.gif" );
			_imageMap.put( TLOpenEnumeration.class, "enum.gif" );
			_imageMap.put( TLClosedEnumeration.class, "enum.gif" );
			_imageMap.put( TLValueWithAttributes.class, "vwa.gif" );
			_imageMap.put( TLCoreObject.class, "core_object.gif" );
			_imageMap.put( TLChoiceObject.class, "choice_object.gif" );
			_imageMap.put( TLBusinessObject.class, "business_object.png" );
			_imageMap.put( TLFacet.class, "facet.gif" );
			_imageMap.put( TLContextualFacet.class, "facet_contextual.gif" );
			_imageMap.put( TLExtensionPointFacet.class, "facet_contextual.gif" );
			_imageMap.put( TLService.class, "service.gif" );
			_imageMap.put( TLOperation.class, "operation.gif" );
			_imageMap.put( TLResource.class, "resource.gif" );
			_imageMap.put( TLParamGroup.class, "parameter.gif" );
			_imageMap.put( TLParameter.class, "parameter.gif" );
			_imageMap.put( TLActionFacet.class, "action_facet.gif" );
			_imageMap.put( TLAction.class, "resource_action.gif" );
			_imageMap.put( TLActionRequest.class, "request.gif" );
			_imageMap.put( TLActionResponse.class, "response.gif" );
			imageMap = Collections.unmodifiableMap( _imageMap );
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError( t );
		}
	}
	
}
