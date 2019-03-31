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

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventBuilder;
import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.model.TLInclude.IncludeListManager;
import org.opentravel.schemacompiler.model.TLNamespaceImport.NamespaceImportListManager;
import org.opentravel.schemacompiler.transform.AnonymousEntityFilter;
import org.opentravel.schemacompiler.util.ReferenceCountVisitor;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Base class for the various types of libraries that can be managed within a <code>TLModel</code> instance.
 * 
 * @author S. Livezey
 */
public abstract class AbstractLibrary extends TLModelElement {

    private TLModel owningModel;
    private URL libraryUrl;
    private String name;
    private String namespace;
    private String prefix;
    private IncludeListManager includeManager = new IncludeListManager( this );
    private NamespaceImportListManager namespaceImportManager = new NamespaceImportListManager( this );
    private List<LibraryMember> namedMembers = new ArrayList<>();
    protected String versionScheme;
    protected VersionScheme vScheme;

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        String identity = name;

        if (libraryUrl != null) {
            String urlPath = libraryUrl.getPath();
            int idx = urlPath.lastIndexOf( '/' );

            if (!urlPath.endsWith( "/" ) && (idx >= 0)) {
                urlPath = urlPath.substring( idx + 1 );
            }
            return urlPath;
        }
        return identity;
    }

    /**
     * Returns the total number of referenced from entities defined in other libraries to entities defined in this one.
     * 
     * @return int
     */
    public int getReferenceCount() {
        int referenceCount = 0;

        if (owningModel != null) {
            ReferenceCountVisitor visitor = new ReferenceCountVisitor( this );

            ModelNavigator.navigate( this.getOwningModel(), visitor );
            referenceCount = visitor.getReferenceCount();
        }
        return referenceCount;
    }

    /**
     * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
     */
    @Override
    public TLModel getOwningModel() {
        return owningModel;
    }

    /**
     * Returns true if the 'otherLibrary' meets both of the following conditions:
     * <ul>
     * <li>The other library is assigned to the same version scheme and base namespace as this one.</li>
     * <li>The version of the other library is considered to be later than this library's version according to the
     * version scheme.</li>
     * </ul>
     * 
     * @param otherLibrary the other library with which to compare this one
     * @return boolean
     */
    public boolean isLaterVersion(AbstractLibrary otherLibrary) {
        return false; // See 'TLLibrary.isLaterVersion()' for implementation
    }

    /**
     * Assigns the value of the 'owningModel' field.
     * 
     * @param owningModel the field value to assign
     */
    public void setOwningModel(TLModel owningModel) {
        this.owningModel = owningModel;
    }

    /**
     * Returns the value of the 'libraryUrl' field.
     * 
     * @return URL
     */
    public URL getLibraryUrl() {
        return libraryUrl;
    }

    /**
     * Assigns the value of the 'libraryUrl' field.
     * 
     * @param libraryUrl the field value to assign
     */
    public void setLibraryUrl(URL libraryUrl) {
        ModelEvent<?> event = new ModelEventBuilder( ModelEventType.URL_MODIFIED, this ).setOldValue( this.libraryUrl )
            .setNewValue( libraryUrl ).buildEvent();

        if (getOwningModel() != null) {
            getOwningModel().checkDuplicateLibrary( getNamespace(), getName(), libraryUrl, this );
        }
        this.libraryUrl = libraryUrl;
        publishEvent( event );
    }

    /**
     * Returns true if the library is to be considered read-only by an editor application. By default, this method
     * returns true; sub-classes should override to change this behavior.
     * 
     * @return boolean
     */
    public boolean isReadOnly() {
        return true;
    }

    /**
     * Returns the value of the 'name' field.
     * 
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Assigns the value of the 'name' field.
     * 
     * @param name the field value to assign
     * @throws IllegalArgumentException thrown if the new value creates a name/namespace conflict with another library
     */
    public void setName(String name) {
        ModelEvent<?> event = new ModelEventBuilder( ModelEventType.NAME_MODIFIED, this ).setOldValue( this.name )
            .setNewValue( name ).buildEvent();

        if (owningModel != null) {
            owningModel.checkDuplicateLibrary( this.namespace, name, this.libraryUrl, this );
        }
        this.name = name;
        publishEvent( event );
    }

    /**
     * Returns the value of the 'namespace' field.
     * 
     * @return String
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Assigns the value of the 'namespace' field.
     * 
     * @param namespace the field value to assign
     * @throws IllegalArgumentException thrown if the new value creates a name/namespace conflict with another library
     */
    public void setNamespace(String namespace) {
        ModelEvent<?> event = new ModelEventBuilder( ModelEventType.NAMESPACE_MODIFIED, this )
            .setOldValue( this.namespace ).setNewValue( namespace ).buildEvent();

        if (owningModel != null) {
            owningModel.checkDuplicateLibrary( namespace, this.name, this.libraryUrl, this );
        }
        this.namespace = namespace;
        publishEvent( event );
    }

    /**
     * Returns the value of the 'prefix' field.
     * 
     * @return String
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Assigns the value of the 'prefix' field.
     * 
     * @param prefix the field value to assign
     */
    public void setPrefix(String prefix) {
        ModelEvent<?> event = new ModelEventBuilder( ModelEventType.PREFIX_MODIFIED, this ).setOldValue( this.prefix )
            .setNewValue( prefix ).buildEvent();

        this.prefix = prefix;
        publishEvent( event );
    }

    /**
     * Returns the value of the 'includes' field.
     * 
     * @return List&lt;TLInclude&gt;
     */
    public List<TLInclude> getIncludes() {
        return includeManager.getChildren();
    }

    /**
     * Adds an include value to the current list.
     * 
     * @param include the included namespace to add
     */
    public void addInclude(TLInclude include) {
        includeManager.addChild( include );
    }

    /**
     * Adds an include value to the current list.
     * 
     * @param index the index at which the given include should be added
     * @param include the included namespace to add
     * @throws IndexOutOfBoundsException thrown if the index is out of range (index &lt; 0 || index &gt; size())
     */
    public void addInclude(int index, TLInclude include) {
        includeManager.addChild( index, include );
    }

    /**
     * Removes an include value from the current list.
     * 
     * @param include the included namespace to remove
     */
    public void removeInclude(TLInclude include) {
        includeManager.removeChild( include );
    }

    /**
     * Sorts the list of includes using the comparator provided.
     * 
     * @param comparator the comparator to use when sorting the list
     */
    public void sortIncludes(Comparator<TLInclude> comparator) {
        includeManager.sortChildren( comparator );
    }

    /**
     * Returns the list of namespace imports for this library.
     * 
     * @return List&lt;TLNamespaceImport&gt;
     */
    public List<TLNamespaceImport> getNamespaceImports() {
        return namespaceImportManager.getChildren();
    }

    /**
     * Adds a namespace import to the current list.
     * 
     * @param namespaceImport the namespace import to add
     */
    public void addNamespaceImport(TLNamespaceImport namespaceImport) {
        namespaceImportManager.addChild( namespaceImport );
    }

    /**
     * Adds a <code>TLNamespaceImport</code> element to the current list.
     * 
     * @param index the index at which the given namespace import should be added
     * @param namespaceImport the namespace import value to add
     * @throws IndexOutOfBoundsException thrown if the index is out of range (index &lt; 0 || index &gt; size())
     */
    public void addNamespaceImport(int index, TLNamespaceImport namespaceImport) {
        namespaceImportManager.addChild( index, namespaceImport );
    }

    /**
     * Adds a namespace import to the current list.
     * 
     * @param prefix the prefix used to reference the imported namespace
     * @param namespace the namespace to be imported
     * @param fileHints hints to the location of imported files
     */
    public void addNamespaceImport(String prefix, String namespace, String[] fileHints) {
        TLNamespaceImport nsImport = new TLNamespaceImport( prefix, namespace );

        if (fileHints != null) {
            for (String fileHint : fileHints) {
                nsImport.getFileHints().add( fileHint );
            }
        }
        addNamespaceImport( nsImport );
    }

    /**
     * Removes a namespace import from the current list.
     * 
     * @param namespaceImport the namespace import to remove
     */
    public void removeNamespaceImport(TLNamespaceImport namespaceImport) {
        namespaceImportManager.removeChild( namespaceImport );
    }

    /**
     * Removes a namespace import from the current list.
     * 
     * @param prefix the prefix used to reference the namespace import to be removed
     */
    public void removeNamespaceImport(String prefix) {
        TLNamespaceImport nsImport = namespaceImportManager.getChild( prefix );

        if (nsImport != null) {
            namespaceImportManager.removeChild( nsImport );
        }
    }

    /**
     * Sorts the list of attributes using the comparator provided.
     * 
     * @param comparator the comparator to use when sorting the list
     */
    public void sortNamespaceImports(Comparator<TLNamespaceImport> comparator) {
        namespaceImportManager.sortChildren( comparator );
    }

    /**
     * Moves this include up by one position in the list. If the include is not owned by this object or it is already at
     * the front of the list, this method has no effect.
     * 
     * @param include the include to move
     */
    public void moveUp(TLInclude include) {
        includeManager.moveUp( include );
    }

    /**
     * Moves this namespace import up by one position in the list. If the namespace import is not owned by this object
     * or it is already at the front of the list, this method has no effect.
     * 
     * @param namespaceImport the namespace import to move
     */
    public void moveUp(TLNamespaceImport namespaceImport) {
        namespaceImportManager.moveUp( namespaceImport );
    }

    /**
     * Moves this include down by one position in the list. If the include is not owned by this object or it is already
     * at the end of the list, this method has no effect.
     * 
     * @param include the include to move
     */
    public void moveDown(TLInclude include) {
        includeManager.moveDown( include );
    }

    /**
     * Moves this namespace import down by one position in the list. If the namespace import is not owned by this object
     * or it is already at the end of the list, this method has no effect.
     * 
     * @param namespaceImport the namespace import to move
     */
    public void moveDown(TLNamespaceImport namespaceImport) {
        namespaceImportManager.moveDown( namespaceImport );
    }

    /**
     * Returns the imported namespace that is referenced by the specified prefix within this library.
     * 
     * @param prefix the namespace prefix
     * @return String
     */
    public String getNamespaceForPrefix(String prefix) {
        String result = null;

        if (prefix != null) {
            for (TLNamespaceImport nsImport : namespaceImportManager.getChildren()) {
                if (prefix.equals( nsImport.getPrefix() )) {
                    result = nsImport.getNamespace();
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns the prefix that should be used to refer to the indicated namespace. If an import has not been defined for
     * the namespace, this method will return null. If the requested namespace matches the library's assigned namespace,
     * an empty string will be returned.
     * 
     * @param namespace the namespace for which to return a prefix
     * @return String
     */
    public String getPrefixForNamespace(String namespace) {
        String result = null;

        if ((namespace != null) && !namespace.equals( AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE )) {
            for (TLNamespaceImport nsImport : namespaceImportManager.getChildren()) {
                if (namespace.equals( nsImport.getNamespace() )) {
                    result = nsImport.getPrefix();
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns the consolidated list of all types and services defined for this library.
     * 
     * @return List&lt;LibraryMember&gt;
     */
    public List<LibraryMember> getNamedMembers() {
        return Collections.unmodifiableList( namedMembers );
    }

    /**
     * Returns the member with the specified local name.
     * 
     * @param memberName the name of the member to return
     * @return LibraryMember
     */
    public LibraryMember getNamedMember(String memberName) {
        LibraryMember member = null;

        if (memberName != null) {
            for (LibraryMember e : namedMembers) {
                if (memberName.equals( e.getLocalName() )) {
                    member = e;
                    break;
                }
            }
        }
        return member;
    }

    /**
     * Adds a <code>LibraryMember</code> member to the list of type and service definitions for this library.
     * 
     * @param namedMember the named entity to add to this library
     */
    public void addNamedMember(LibraryMember namedMember) {
        if (namedMember != null) {
            if (!isValidMember( namedMember )) {
                String memberType = namedMember.getClass().getSimpleName();

                throw new IllegalArgumentException( "Items of type '" + memberType + "' are not allowed as members of "
                    + this.getClass().getSimpleName() + " libraries." );
            }
            namedMember.setOwningLibrary( this );
            this.namedMembers.add( namedMember );
            publishEvent( new ModelEventBuilder( ModelEventType.MEMBER_ADDED, this ).setAffectedItem( namedMember )
                .buildEvent() );
        }
    }

    /**
     * Removes a <code>LibraryMember</code> member from the list of type and service definitions for this library.
     * 
     * @param namedMember the named entity to remove from this library
     */
    public void removeNamedMember(LibraryMember namedMember) {
        if (namedMembers.contains( namedMember )) {
            namedMember.setOwningLibrary( null );
            this.namedMembers.remove( namedMember );
            publishEvent( new ModelEventBuilder( ModelEventType.MEMBER_REMOVED, this ).setAffectedItem( namedMember )
                .buildEvent() );
        }
    }

    /**
     * Returns true if the given member is a valid member of this library (false otherwise).
     * 
     * @param namedMember the candidate member to check
     * @return boolean
     */
    protected abstract boolean isValidMember(LibraryMember namedMember);

    /**
     * Returns the library's version identifier.
     * 
     * @return String
     */
    public String getVersion() {
        String ns = getNamespace();
        String version;

        if (vScheme != null) {
            version = ((ns == null) || ns.equals( "" )) ? vScheme.getDefaultVersionIdentifier()
                : vScheme.getVersionIdentifier( ns );
        } else {
            version = null;
        }
        return version;
    }

    /**
     * Assigns the given version to this library. NOTE: Because the version of a library is derived from the namespace
     * and patch level, it is likely that this method call will result in updates to those field values.
     * 
     * @param version the version identifier to assign
     * @throws IllegalArgumentException thrown if the version identifier is not valid for the current version scheme
     * @throws IllegalStateException thrown if a valid version scheme identifer has not been assigned prior to this
     *         method call
     */
    public void setVersion(String version) {
        String ns = getNamespace();

        if ((ns == null) || ns.equals( "" )) {
            throw new IllegalStateException(
                "Library versions cannot be set before a valid namespace has been assigned." );
        }
        if (vScheme == null) {
            throw new IllegalStateException( "No valid version scheme assigned to library: " + getName() );
        }
        if (!vScheme.isValidVersionIdentifier( version )) {
            throw new IllegalArgumentException(
                "Invalid version identifier for version scheme " + versionScheme + ": '" + version + "'" );
        }
        setNamespace( vScheme.setVersionIdentifier( ns, version ) );
    }

    /**
     * Returns the library's version scheme identifier.
     * 
     * @return String
     */
    public String getVersionScheme() {
        return versionScheme;
    }

    /**
     * Assigns the value of the 'versionScheme' field.
     * 
     * @param versionScheme the field value to assign
     */
    public void setVersionScheme(String versionScheme) {
        ModelEvent<?> event = new ModelEventBuilder( ModelEventType.VERSION_SCHEME_MODIFIED, this )
            .setOldValue( this.versionScheme ).setNewValue( versionScheme ).buildEvent();

        try {
            // Lookup the executable component for the assigned version scheme
            vScheme =
                (versionScheme == null) ? null : VersionSchemeFactory.getInstance().getVersionScheme( versionScheme );

        } catch (VersionSchemeException e) {
            vScheme = null;
        }
        this.versionScheme = versionScheme;
        publishEvent( event );
    }

}
