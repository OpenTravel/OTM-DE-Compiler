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
package org.opentravel.schemacompiler.model;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.ioc.SchemaLocation;
import org.opentravel.schemacompiler.version.XSDVersionScheme;

/**
 * Base class for all built-in libraries that are loaded automatically for each <code>TLModel</code> instance.
 * 
 * @author S. Livezey
 */
public class BuiltInLibrary extends AbstractLibrary {
	
	private static final String NOT_SUPPORTED_FOR_BUILT_INS = "Operation not supported for built-in libraries.";
	
	public enum BuiltInType {
		XSD_BUILTIN, TLLIBRARY_BUILTIN, SCHEMA_FOR_SCHEMAS_BUILTIN
	}
	
	private static final Set<Class<?>> xsdMemberTypes;
	
	private SchemaDeclaration schemaDeclaration;
	private BuiltInType builtInType;
	
	/**
	 * Constructor that provides all required information for the library. Once constructed, the attributes and members
	 * of a built-in library cannot be modified. This library will be imported by default by all user-defined libraries.
	 * 
	 * @param qualifiedName qualified name of the library that supplies its namespace, name, and prefix
	 * @param libraryUrl the URL of the library
	 * @param members the member types of the library
	 */
	public BuiltInLibrary(QName qualifiedName, URL libraryUrl, List<LibraryMember> members) {
		
		this( qualifiedName, libraryUrl, members, null, null,
				createDefaultSchemaDeclaration( qualifiedName, libraryUrl.toString() ), XSDVersionScheme.ID );
	}
	
	/**
	 * Creates a default schema declaration for this library.
	 * 
	 * @param qualifiedName qualified name of the library that supplies its namespace, name, and prefix
	 * @param libraryUrl  the URL of the built-in library
	 * @return SchemaDeclaration
	 */
	private static SchemaDeclaration createDefaultSchemaDeclaration(QName qualifiedName, String libraryUrl) {
		SchemaDeclaration schemaDeclaration = new SchemaDeclaration();
		schemaDeclaration.setNamespace( qualifiedName.getNamespaceURI() );
		schemaDeclaration.setName( qualifiedName.getLocalPart() );
		schemaDeclaration.setDefaultPrefix( qualifiedName.getPrefix() );
		schemaDeclaration
			.setLocations( Arrays.asList( new SchemaLocation( CodeGeneratorFactory.XSD_TARGET_FORMAT, libraryUrl ) ) );
		schemaDeclaration.setImportByDefault( true );
		return schemaDeclaration;
	}
	
	/**
	 * Constructor that provides all required information for the library. Once constructed, the attributes and members
	 * of a built-in library cannot be modified.
	 * 
	 * @param qualifiedName qualified name of the library that supplies its namespace, name, and prefix
	 * @param libraryUrl the URL of the library
	 * @param members the member types of the library
	 */
	public BuiltInLibrary(QName qualifiedName, URL libraryUrl, List<LibraryMember> members,
			List<TLNamespaceImport> importList, List<TLInclude> includeList, SchemaDeclaration schemaDeclaration,
			String versionScheme) {
		super.setNamespace( qualifiedName.getNamespaceURI() );
		super.setName( qualifiedName.getLocalPart() );
		super.setLibraryUrl( libraryUrl );
		super.setPrefix( qualifiedName.getPrefix() );
		super.setVersionScheme( versionScheme );
		
		if (members != null) {
			setBuiltInType( qualifiedName.getNamespaceURI(), members );
		}
		
		if (importList != null) {
			for (TLNamespaceImport nsImport : importList) {
				super.addNamespaceImport( nsImport );
			}
		}
		if (includeList != null) {
			for (TLInclude include : includeList) {
				super.addInclude( include );
			}
		}
		this.schemaDeclaration = schemaDeclaration;
	}
	
	/**
	 * Assigns the type of built-in library this is based upon its namespace and member entities.
	 * 
	 * @param namespace the namespace of this library
	 * @param members the list of member entities for this library
	 */
	private void setBuiltInType(String namespace, List<LibraryMember> members) {
		boolean hasTLLibraryMembers = true;
		boolean hasXsdMembers = true;
		
		for (LibraryMember member : members) {
			super.addNamedMember( member );
			hasXsdMembers &= xsdMemberTypes.contains( member.getClass() );
			hasTLLibraryMembers &= !xsdMemberTypes.contains( member.getClass() );
		}
		
		if ((namespace != null) && namespace.equals( XMLConstants.W3C_XML_SCHEMA_NS_URI )) {
			builtInType = BuiltInType.SCHEMA_FOR_SCHEMAS_BUILTIN; // special case:
																	// schema-for-schemas
			
		} else if (hasTLLibraryMembers && hasXsdMembers) {
			throw new IllegalArgumentException(
					"Error: Built-in libraries cannot be a mix of XSD and OTA2 member types." );
			
		} else {
			builtInType = (hasTLLibraryMembers) ? BuiltInType.TLLIBRARY_BUILTIN : BuiltInType.XSD_BUILTIN;
		}
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#isValidMember(org.opentravel.schemacompiler.model.LibraryMember)
	 */
	@Override
	protected boolean isValidMember(LibraryMember namedMember) {
		return (namedMember != null) && !(namedMember instanceof TLService);
	}
	
	/**
	 * Returns the <code>SchemaDeclaration</code> from which this built-in library was loaded.
	 * 
	 * @return SchemaDeclaration
	 */
	public SchemaDeclaration getSchemaDeclaration() {
		return schemaDeclaration;
	}
	
	/**
	 * Returns the type of built-in library this instance represents.
	 * 
	 * @return BuiltInType
	 */
	public BuiltInType getBuiltInType() {
		return builtInType;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#setLibraryUrl(java.net.URL)
	 */
	@Override
	public void setLibraryUrl(URL libraryUrl) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#setNamespace(java.lang.String)
	 */
	@Override
	public void setNamespace(String namespace) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#getIncludes()
	 */
	@Override
	public List<TLInclude> getIncludes() {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#addInclude(org.opentravel.schemacompiler.model.TLInclude)
	 */
	@Override
	public void addInclude(TLInclude include) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#addInclude(int,
	 *      org.opentravel.schemacompiler.model.TLInclude)
	 */
	@Override
	public void addInclude(int index, TLInclude include) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#removeInclude(org.opentravel.schemacompiler.model.TLInclude)
	 */
	@Override
	public void removeInclude(TLInclude include) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#moveUp(org.opentravel.schemacompiler.model.TLInclude)
	 */
	@Override
	public void moveUp(TLInclude include) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#moveDown(org.opentravel.schemacompiler.model.TLInclude)
	 */
	@Override
	public void moveDown(TLInclude include) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#sortIndicators(java.util.Comparator)
	 */
	@Override
	public void sortIncludes(Comparator<TLInclude> comparator) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#addNamespaceImport(org.opentravel.schemacompiler.model.TLNamespaceImport)
	 */
	@Override
	public void addNamespaceImport(TLNamespaceImport namespaceImport) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#addNamespaceImport(int,
	 *      org.opentravel.schemacompiler.model.TLNamespaceImport)
	 */
	@Override
	public void addNamespaceImport(int index, TLNamespaceImport namespaceImport) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#addNamespaceImport(java.lang.String, java.lang.String,
	 *      java.lang.String[])
	 */
	@Override
	public void addNamespaceImport(String prefix, String namespace, String[] fileHints) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#removeNamespaceImport(org.opentravel.schemacompiler.model.TLNamespaceImport)
	 */
	@Override
	public void removeNamespaceImport(TLNamespaceImport namespaceImport) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#removeNamespaceImport(java.lang.String)
	 */
	@Override
	public void removeNamespaceImport(String prefix) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#moveUp(org.opentravel.schemacompiler.model.TLNamespaceImport)
	 */
	@Override
	public void moveUp(TLNamespaceImport namespaceImport) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#moveDown(org.opentravel.schemacompiler.model.TLNamespaceImport)
	 */
	@Override
	public void moveDown(TLNamespaceImport namespaceImport) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#sortNamespaceImports(java.util.Comparator)
	 */
	@Override
	public void sortNamespaceImports(Comparator<TLNamespaceImport> comparator) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#addNamedMember(org.opentravel.schemacompiler.model.AbstractLibraryMember)
	 */
	@Override
	public void addNamedMember(LibraryMember namedMember) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.AbstractLibrary#removeNamedMember(org.opentravel.schemacompiler.model.AbstractLibraryMember)
	 */
	@Override
	public void removeNamedMember(LibraryMember namedMember) {
		throw new UnsupportedOperationException( NOT_SUPPORTED_FOR_BUILT_INS );
	}
	
	/**
	 * Initializes the list of valid member types for this library.
	 */
	static {
		try {
			Set<Class<?>> xsdTypes = new HashSet<>();
			
			xsdTypes.add( XSDSimpleType.class );
			xsdTypes.add( XSDComplexType.class );
			xsdTypes.add( XSDElement.class );
			xsdMemberTypes = Collections.unmodifiableSet( xsdTypes );
			
		} catch (Exception e) {
			throw new ExceptionInInitializerError( e );
		}
	}
	
}
