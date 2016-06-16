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

package org.opentravel.schemacompiler.diff.impl;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;

/**
 * Methods used to format user-displayable names for various OTM object types.
 */
public class DisplayFomatter {
	
	private static DateFormat dateFormat = new SimpleDateFormat( "MMMMM d, yyyy '&amp;' h:mma z" );
	
	/**
	 * Returns a user-displayable string with the current date and time.
	 * 
	 * @return String
	 */
	public String reportTimestamp() {
		return dateFormat.format( new Date() );
	}
	
	/**
	 * Returns the display name for the given entity type.
	 * 
	 * @param entityType  the entity type for which to return a display name
	 * @return String
	 */
	public String getEntityTypeDisplayName(Class<?> entityType) {
		return (entityType == null) ? null : SchemaCompilerApplicationContext.getContext().getMessage(
				entityType.getSimpleName() + ".displayName", null, Locale.getDefault() );
	}
	
	/**
	 * Returns a display name for the given library.
	 * 
	 * @param library  the library for which to return a display name
	 * @return String
	 */
	public String getLibraryDisplayName(TLLibrary library) {
		String displayName = null;
		
		if (library != null) {
			String prefix = library.getPrefix();
			String name = library.getName();
			
			if ((name == null) || (name.length() == 0)) {
				name = "UNKNOWN";
			}
			if (prefix != null) {
				displayName = prefix + ":";
			}
			displayName += name;
		}
		return displayName;
	}
	
	/**
	 * Returns the filename component of the library's URL.
	 * 
	 * @param library  the library for which to return the filename
	 * @return String
	 */
	public String getLibraryFilename(TLLibrary library) {
		String filename = null;
		
		if (library != null) {
			URL libraryUrl = (library == null) ? null : library.getLibraryUrl();
			String url = (libraryUrl == null) ? null : libraryUrl.toExternalForm();
			
			if (url != null) {
				filename = url.substring( url.lastIndexOf('/') + 1 );
			}
			if (filename == null) {
				filename = library.getName();
			}
		}
		if (filename == null) {
			filename = "UNKNOWN";
		}
		return filename;
	}
	
	/**
	 * Returns a display name for the given entity.
	 * 
	 * @param entity  the entity for which to return a display name
	 * @return String
	 */
	public String getEntityDisplayName(NamedEntity entity) {
		String displayName = null;
		
		if (entity != null) {
			AbstractLibrary owningLibrary = entity.getOwningLibrary();
			
			displayName = getLocalDisplayName( entity );
			
			if (owningLibrary != null) {
				String prefix = owningLibrary.getPrefix();
				
				if (prefix != null) {
					displayName = prefix + ":" + displayName;
				}
			}
		}
		return displayName;
	}
	
	/**
	 * Returns a display name for the local component of the entity's name.
	 * 
	 * @param entity  the entity for which to return a local display name
	 * @return String
	 */
	public String getLocalDisplayName(NamedEntity entity) {
		String displayName;
		
		if (entity instanceof TLOperation) {
			displayName = ((TLOperation) entity).getName();
			
		} else if (entity != null) {
			displayName = entity.getLocalName();
			
		} else {
			displayName = "UNKNOWN";
		}
		return displayName;
	}
	
	/**
	 * Returns the name of the field as it will be referenced in an XML schema
	 * declaration.
	 * 
	 * @param field  the member field for which to return a name
	 * @return String
	 */
	public String getFieldName(TLMemberField<?> field) {
		String fieldName = field.getName();
		
		// Default value is the assigned field name, but this may be different for elements (properties)
		if (field instanceof TLProperty) {
			TLProperty element = (TLProperty) field;
	        TLPropertyType propertyType = PropertyCodegenUtils.resolvePropertyType( element.getOwner(), element.getType() );
			
	        // If the property has a global element, use that name instead of the one assigned to the property
	        if (PropertyCodegenUtils.hasGlobalElement( propertyType )) {
	            QName propertyRef = PropertyCodegenUtils.getDefaultSchemaElementName( propertyType, false );
				AbstractLibrary refOwningLibrary = propertyType.getOwningLibrary();
	        	
	            if (propertyRef != null) {
	            	fieldName = propertyRef.getLocalPart();
	            }
	            
	            // If the assigned type is not in the same library as the element declaration, add
	            // a prefix to the name.
	            if ((refOwningLibrary != null) && (refOwningLibrary != element.getOwningLibrary())) {
	            	String prefix = refOwningLibrary.getPrefix();
	            	
	            	if (prefix != null) {
	            		fieldName = prefix + ":" + fieldName;
	            	}
	            }
	        }
		}
		return fieldName;
	}
	
}
