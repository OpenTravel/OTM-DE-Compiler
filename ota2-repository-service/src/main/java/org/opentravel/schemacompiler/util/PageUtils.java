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

package org.opentravel.schemacompiler.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.opentravel.schemacompiler.console.NamespaceItem;
import org.opentravel.schemacompiler.index.EntitySearchResult;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemType;

/**
 * Utility methods used to assist with the rendering of JSP pages.
 * 
 * @author S. Livezey
 */
public class PageUtils {
	
	private static final String DATETIME_FORMAT = "dd-MMM-yyyy hh:mm a";
	
	private static DateFormat dateTimeFormat;
	
	/**
	 * Returns true if the given string is null or empty.
	 * 
	 * @param docValue  the documentation value to analyze
	 * @return boolean
	 */
	public boolean isBlank(String docValue) {
		return trimString( docValue ) == null;
	}
	
	/**
	 * Returns true if the given namespace item represents an OTM release.
	 * 
	 * @param item  the namespace item to check
	 * @return boolean
	 */
	public boolean isRelease(NamespaceItem item) {
		return (item != null) && RepositoryItemType.RELEASE.isItemType( item.getFilename() );
	}
	
	/**
	 * Returns the opposite of the current value between 'value1' and 'value2'.
	 * 
	 * @param currentValue  the current value
	 * @param value1  the first possible return value
	 * @param value2  the second possible return value
	 * @return String
	 */
	public String swapValue(String currentValue, String value1, String value2) {
		return ((currentValue == null) || currentValue.equals( value2 )) ? value1 : value2;
	}
	
	/**
	 * Return the extension reference string for the given entity if the entity contains
	 * such a reference.
	 * 
	 * @param indexEntity  the entity index document
	 * @return String
	 */
	public String getExtensionRef(EntitySearchResult indexEntity) {
		NamedEntity entity = (indexEntity == null) ? null : indexEntity.getItemContent();
		String extensionRef = null;
		
		if (entity instanceof TLExtensionOwner) {
			TLExtension extension = ((TLExtensionOwner) entity).getExtension();
			
			if (extension != null) {
				extensionRef = PageUtils.trimString( extension.getExtendsEntityName() );
			}
		}
		return extensionRef;
	}
	
	/**
	 * Return the simple facet string for the given entity if the entity contains
	 * such a reference.  If the given entity is not a core or does not define a simple
	 * facet reference, this method will return null.
	 * 
	 * @param indexEntity  the entity index document
	 * @return String
	 */
	public String getSimpleFacetRef(EntitySearchResult indexEntity) {
		NamedEntity entity = (indexEntity == null) ? null : indexEntity.getItemContent();
		String sfRef = null;
		
		if (entity instanceof TLCoreObject) {
			TLSimpleFacet simpleFacet = ((TLCoreObject) entity).getSimpleFacet();
			
			if (simpleFacet != null) {
				sfRef = simpleFacet.getSimpleTypeName();
			}
		}
		return sfRef;
	}
	
	/**
	 * Returns the local name component of the entity's search index ID.
	 * 
	 * @param indexEntity  the entity index document
	 * @return String
	 */
	public String getEntityLocalName(EntitySearchResult indexEntity) {
		String searchIndexId = (indexEntity == null) ? null : indexEntity.getSearchIndexId();
		String localName = null;
		
		if (searchIndexId != null) {
			int delimIdx = searchIndexId.lastIndexOf(':');
			
			if ((delimIdx >= 0) && (delimIdx < (searchIndexId.length() - 1))) {
				localName = searchIndexId.substring( delimIdx + 1 );
			}
		}
		return localName;
	}
	
    /**
     * Returns the last modified timestamp of the metadata file for the specified repository item.
     * 
     * @param item  the repository item for which to return a timestamp
     * @return Date
     */
    public Date getLastModified(RepositoryItem item) {
    	return NamespaceItem.getLastModified( item );
    }
    
    /**
     * Formats the given date-time value for display.
     * 
     * @param dateTime  the date-time value to be displayed
     * @return String
     */
    public String formatDateTime(Date dateTime) {
    	return (dateTime == null) ? "&nbsp;" : dateTimeFormat.format( dateTime ).replaceAll( "\\s+", "\\&nbsp\\;");
    }
    
	/**
	 * Trims the given string value and returns null if the resulting string length is zero.
	 * 
	 * @param str  the string to be trimmed
	 * @return String
	 */
	protected static String trimString(String str) {
		String trimmedStr = null;
		
		if (str != null) {
			trimmedStr = str.trim();
			
			if (trimmedStr.length() == 0) {
				trimmedStr = null;
			}
		}
		return trimmedStr;
	}
	
	/**
	 * Initializes the date-time formatter with the correct time zone.
	 */
	static {
		try {
			String timeZoneId = System.getProperty("user.timezone", "EST");
			TimeZone timeZone = TimeZone.getTimeZone( timeZoneId );
			
			dateTimeFormat = new SimpleDateFormat( DATETIME_FORMAT );
			dateTimeFormat.setTimeZone( timeZone );
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError( t );
		}
	}
	
}
