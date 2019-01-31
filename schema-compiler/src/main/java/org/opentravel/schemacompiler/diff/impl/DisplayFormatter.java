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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.Release;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.util.SchemaCompilerRuntimeException;
import org.opentravel.schemacompiler.util.URLUtils;

/**
 * Methods used to format user-displayable names for various OTM object types.
 */
public class DisplayFormatter {
	
	private static final String UTF_8 = "UTF-8";
	private static final String UNKNOWN = "UNKNOWN";
	
	private DateFormat dateFormat = new SimpleDateFormat( "MMMMM d, yyyy '&amp;' h:mma z" );
	private RepositoryManager repositoryManager;
	
	/**
	 * Default constructor.
	 */
	public DisplayFormatter() {
		try {
			repositoryManager = RepositoryManager.getDefault();
			
		} catch (RepositoryException e) {
			throw new SchemaCompilerRuntimeException("Unable to initialize repository manager.", e);
		}
	}
	
    /**
     * Constructor that supplies the repository manager instance to use for
     * remote library lookups.
     * 
     * @param repositoryManager  the repository manager instance to use
     */
    public DisplayFormatter(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }
    
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
	 * Returns the display name for the given library status.
	 * 
	 * @param status  the library status for which to return a display name
	 * @return String
	 */
    public String getLibraryStatusDisplayName(TLLibraryStatus status) {
		return (status == null) ? null : SchemaCompilerApplicationContext.getContext().getMessage(
				status.toString(), null, Locale.getDefault() );
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
				name = UNKNOWN;
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
			RepositoryItem item = getRepositoryItem( library );
			
			if (item != null) {
				filename = item.getFilename();
				
			} else {
				URL libraryUrl = library.getLibraryUrl();
				String url = (libraryUrl == null) ? null : libraryUrl.toExternalForm();
				
				if (url != null) {
					filename = url.substring( url.lastIndexOf('/') + 1 );
				}
				if (filename == null) {
					filename = library.getName();
				}
			}
		}
		if (filename == null) {
			filename = UNKNOWN;
		}
		return filename;
	}
	
	/**
	 * Returns the filename component of the release's URL.
	 * 
	 * @param release  the release for which to return the filename
	 * @return String
	 */
	public String getReleaseFilename(Release release) {
		String filename = null;
		
		if (release != null) {
			URL libraryUrl = release.getReleaseUrl();
			String url = (libraryUrl == null) ? null : libraryUrl.toExternalForm();
			
			if (url != null) {
				filename = url.substring( url.lastIndexOf('/') + 1 );
			}
			if (filename == null) {
				filename = release.getName();
			}
		}
		if (filename == null) {
			filename = UNKNOWN;
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
			displayName = UNKNOWN;
		}
		return displayName;
	}
	
	/**
	 * Returns a display name for the given entity.
	 * 
	 * @param entity  the entity for which to return a display name
	 * @return String
	 */
	public String getParentRefDisplayName(TLResourceParentRef entity) {
		TLResource parentResource = entity.getParentResource();
		TLParamGroup parentParamGroup = entity.getParentParamGroup();
		StringBuilder displayName = new StringBuilder();
		
		displayName.append( (parentResource == null) ? UNKNOWN : getLocalDisplayName( parentResource ) );
		displayName.append(" / ");
		displayName.append( (parentParamGroup == null) ? UNKNOWN : getParamGroupDisplayName( parentParamGroup ) );
		return displayName.toString();
	}
	
	/**
	 * Returns a display name for the given entity.
	 * 
	 * @param entity  the entity for which to return a display name
	 * @return String
	 */
	public String getParamGroupDisplayName(TLParamGroup entity) {
		return entity.getName();
	}
	
	/**
	 * Returns a display name for the given entity.
	 * 
	 * @param entity  the entity for which to return a display name
	 * @return String
	 */
	public String getParameterDisplayName(TLParameter entity) {
		TLParamGroup paramGroup = entity.getOwner();
		StringBuilder displayName = new StringBuilder();
		
		displayName.append( paramGroup.getName() );
		displayName.append(" - ");
		displayName.append(entity.getFieldRefName());
		return displayName.toString();
	}
	
	/**
	 * Returns a display name for the given entity.
	 * 
	 * @param entity  the entity for which to return a display name
	 * @return String
	 */
	public String getActionDisplayName(TLAction entity) {
		return entity.getActionId();
	}
	
	/**
	 * Returns a display name for the given entity.
	 * 
	 * @param entity  the entity for which to return a display name
	 * @return String
	 */
	public String getActionResponseDisplayName(TLActionResponse entity) {
		StringBuilder displayName = new StringBuilder();
		TLAction action = entity.getOwner();
		
		displayName.append( (action == null) ? UNKNOWN : action.getActionId() );
		displayName.append( " [" );
		displayName.append( ResourceComparator.getResponseId( entity ) );
		displayName.append( "]" );
		return displayName.toString();
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
	        TLPropertyType propertyType = PropertyCodegenUtils.resolvePropertyType( element.getType() );
			
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
	
	/**
	 * Returns the repository URL for the given library or null if the library
	 * is not managed by a repository.
	 * 
	 * @param library  the library for which to return a repository URL
	 * @return String
	 */
	public String getLibraryViewDetailsUrl(TLLibrary library) {
		RepositoryItem item = getRepositoryItem( library );
		String url = null;
		
		if ((item != null) && (item.getRepository() instanceof RemoteRepository)) {
			try {
				StringBuilder urlBuilder = new StringBuilder( ((RemoteRepository) item.getRepository()).getEndpointUrl() );
				
				urlBuilder.append( "/console/libraryDictionary.html" );
				urlBuilder.append( "?baseNamespace=" ).append( URLEncoder.encode( library.getBaseNamespace(), UTF_8 ) );
				urlBuilder.append( "&filename=" ).append( item.getFilename() );
				urlBuilder.append( "&version=" ).append( item.getVersion() );
				url = urlBuilder.toString();
				
			} catch (UnsupportedEncodingException e) {
				// Ignore error and return null
			}
		}
		return (url == null) ? "" : url;
	}
	
	/**
	 * Returns the repository URL for the given release or null if the release
	 * is not managed by a repository.
	 * 
	 * @param release  the release for which to return a repository URL
	 * @return String
	 */
	public String getReleaseViewDetailsUrl(Release release) {
		RepositoryItem item = getRepositoryItem( release );
		String url = null;
		
		if ((item != null) && (item.getRepository() instanceof RemoteRepository)) {
			try {
				StringBuilder urlBuilder = new StringBuilder( ((RemoteRepository) item.getRepository()).getEndpointUrl() );
				
				urlBuilder.append( "/console/releaseView.html" );
				urlBuilder.append( "?baseNamespace=" ).append( URLEncoder.encode( release.getBaseNamespace(), UTF_8 ) );
				urlBuilder.append( "&filename=" ).append( item.getFilename() );
				urlBuilder.append( "&version=" ).append( item.getVersion() );
				url = urlBuilder.toString();
				
			} catch (UnsupportedEncodingException e) {
				// Ignore error and return null
			}
		}
		return (url == null) ? "" : url;
	}
	
	/**
	 * Returns the repository URL for the given entity or null if the entity's library
	 * is not managed by a repository.
	 * 
	 * @param library  the entity for which to return a repository URL
	 * @return String
	 */
	public String getEntityViewDetailsUrl(NamedEntity entity) {
		AbstractLibrary lib = ((entity == null) || (entity instanceof TLActionFacet)) ? null : entity.getOwningLibrary();
		String url = null;
		
		if (lib instanceof TLLibrary) {
			TLLibrary library = (TLLibrary) lib;
			RepositoryItem item = getRepositoryItem( library );
			
			if ((item != null) && (item.getRepository() instanceof RemoteRepository)) {
				try {
					StringBuilder urlBuilder = new StringBuilder( ((RemoteRepository) item.getRepository()).getEndpointUrl() );
					
					urlBuilder.append( "/console/entityDictionary.html" );
					urlBuilder.append( "?namespace=" ).append( URLEncoder.encode( entity.getNamespace(), UTF_8 ) );
					urlBuilder.append( "&localName=" ).append( entity.getLocalName() );
					url = urlBuilder.toString();
					
				} catch (UnsupportedEncodingException e) {
					// Ignore error and return null
				}
			}
		}
		return (url == null) ? "" : url;
	}
	
	/**
	 * Returns the repository item for the given library or null if the
	 * library is not managed by a repository.
	 * 
	 * @param library  the library for which to return the repository item
	 * @return RemoteRepository
	 */
	private RepositoryItem getRepositoryItem(AbstractLibrary library) {
		RepositoryItem item = null;
		
		if (library != null) {
			if (URLUtils.isFileURL( library.getLibraryUrl() )) {
				try {
					File libraryFile = URLUtils.toFile( library.getLibraryUrl() );
					item = repositoryManager.getRepositoryItem( libraryFile );
					
				} catch (RepositoryException e) {
					// Ignore error and return null
				}
				
			} else if ((library instanceof TLLibrary) &&
					library.getLibraryUrl().toExternalForm().contains("/historical-content?")) {
				// Special case for historical content URL's (by definition, these are managed libraries)
				try {
					TLLibrary tlLibrary = (TLLibrary) library;
					
					item = repositoryManager.getRepositoryItem( tlLibrary.getBaseNamespace(),
							ProjectManager.getPublicationFilename( library ), tlLibrary.getVersion() );
					
				} catch (RepositoryException e) {
					// Ignore error and return null
				}
			}
			
		}
		return item;
	}
	
	/**
	 * Returns the repository item for the given release or null if the
	 * release is not managed by a repository.
	 * 
	 * @param release  the release for which to return the repository item
	 * @return RemoteRepository
	 */
	private RepositoryItem getRepositoryItem(Release release) {
		RepositoryItem item = null;
		
		if ((release != null) && URLUtils.isFileURL( release.getReleaseUrl() )) {
			try {
				File releaseFile = URLUtils.toFile( release.getReleaseUrl() );
				item = repositoryManager.getRepositoryItem( releaseFile );
				
			} catch (RepositoryException e) {
				// Ignore error and return null
			}
		}
		return item;
	}
	
}
