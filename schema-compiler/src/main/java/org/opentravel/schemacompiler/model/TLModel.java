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
import org.opentravel.schemacompiler.event.ModelEventListener;
import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.ic.LibraryMemberChangeIntegrityChecker;
import org.opentravel.schemacompiler.ic.NameChangeIntegrityChecker;
import org.opentravel.schemacompiler.loader.BuiltInLibraryFactory;
import org.opentravel.schemacompiler.transform.AnonymousEntityFilter;
import org.opentravel.schemacompiler.transform.util.ModelReferenceResolver;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.Validatable;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Container that encapsulates all namespaces and libraries within a single semantic model. Every new model instance is
 * pre-populated with the available built-in libraries.
 * 
 * @author S. Livezey
 */
public class TLModel implements Validatable {

    private List<AbstractLibrary> libraryList = new ArrayList<>();
    private List<ModelEventListener<?,?>> listeners = new ArrayList<>();
    private boolean listenersEnabled = true;
    private int chameleonCounter;

    /**
     * Default constructor.
     */
    public TLModel() {
        initModel();
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        return "";
    }

    /**
     * Returns the list of namespaces that have been registered for this model.
     * 
     * @return Collection&lt;String&gt;
     */
    public Collection<String> getNamespaces() {
        Set<String> namespaceSet = new HashSet<>();

        for (AbstractLibrary library : libraryList) {
            String namespace = library.getNamespace();

            if ((namespace != null) && !namespace.equals( AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE )) {
                namespaceSet.add( library.getNamespace() );
            }
        }
        return Collections.unmodifiableCollection( namespaceSet );
    }

    /**
     * Returns true if the specified namespace has been registered with this model.
     * 
     * @param libraryNamespace the library namespace to check
     * @return boolean
     */
    public boolean hasNamespace(String libraryNamespace) {
        boolean result = false;

        if (libraryNamespace != null) {
            for (AbstractLibrary library : libraryList) {
                if (libraryNamespace.equals( library.getNamespace() )) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns true if a library with the specified URL has been registered with this model.
     * 
     * @param libraryUrl the library URL to check
     * @return boolean
     */
    public boolean hasLibrary(URL libraryUrl) {
        return (getLibrary( libraryUrl ) != null);
    }

    /**
     * Returns true if a library has been registered with the given name that is assigned to the indicated namespace.
     * 
     * @param libraryNamespace the library namespace to check
     * @param libraryName the name of the library to check
     * @return boolean
     */
    public boolean hasLibrary(String libraryNamespace, String libraryName) {
        boolean result = false;

        if ((libraryNamespace != null) && (libraryName != null)) {
            for (AbstractLibrary library : libraryList) {
                if (libraryNamespace.equals( library.getNamespace() ) && libraryName.equals( library.getName() )) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Adds the given library to this model.
     * 
     * @param library the library to add
     */
    public void addLibrary(AbstractLibrary library) {
        if ((library != null) && !libraryList.contains( library )) {
            checkDuplicateLibrary( library.getNamespace(), library.getName(), library.getLibraryUrl(), null );

            // Special prefix assignments for chameleon libraries (needed since no target namespace
            // can be defined for a chameleon schema -- hence, no prefix mapping)
            if (AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE.equals( library.getNamespace() )) {
                chameleonCounter++;
                library.setPrefix( "ca" + chameleonCounter );
            }

            // User-defined libraries will automatically receive import statements for all built-in
            // libraries (if they are not already defined)
            if (library instanceof TLLibrary) {
                addBuiltInImports( (TLLibrary) library );
            }
            library.setOwningModel( this );
            libraryList.add( library );
            publishEvent(
                new ModelEventBuilder( ModelEventType.LIBRARY_ADDED, this ).setAffectedItem( library ).buildEvent() );
        }
    }

    /**
     * Removes the given library from this model
     * 
     * @param library the library to remove
     */
    public void removeLibrary(AbstractLibrary library) {
        if (libraryList.contains( library )) {
            library.setOwningModel( null );
            libraryList.remove( library );
            publishEvent(
                new ModelEventBuilder( ModelEventType.LIBRARY_REMOVED, this ).setAffectedItem( library ).buildEvent() );
        }
    }

    /**
     * Moves the given library member to the specified destination library. As part of this operation, the imports
     * and/or includes of the source and destination libraries are updated. Also, any references to the moved entity are
     * updated to reflect the new library to which the member is assigned.
     * 
     * @param member the library member to move
     * @param destLibrary the destination library to which the member is to be assigned
     */
    public void moveToLibrary(LibraryMember member, TLLibrary destLibrary) {
        // Perform validation checks before attempting to perform the move
        if (member.getOwningLibrary() == null) {
            throw new IllegalStateException(
                "Unable to move library member because it is not currently a member of an existing library." );
        }
        if (!(member.getOwningLibrary() instanceof TLLibrary)) {
            throw new IllegalArgumentException( "Only members of user-defined libraries can be moved." );
        }
        if (destLibrary == null) {
            throw new IllegalArgumentException( "The destination library cannot be null." );
        }
        if (member.getOwningLibrary() == destLibrary) {
            return; // Trivial Case: The source and destination libraries are the same
        }
        if (member instanceof TLService) {
            if (destLibrary.getService() != null) {
                throw new IllegalArgumentException( "A service is already assigned to the destination library." );
            }

        } else if (!destLibrary.isValidMember( member )) {
            throw new IllegalArgumentException( "The entity is not a valid member of the destination library." );
        }

        try {
            // Disable model events during the processing of the operation
            setListenersEnabled( false );

            // Move the entity to the destination library
            TLLibrary sourceLibrary = (TLLibrary) member.getOwningLibrary();

            sourceLibrary.removeNamedMember( member );

            if (member instanceof TLService) {
                destLibrary.setService( (TLService) member );

            } else {
                destLibrary.addNamedMember( member );
            }

            // Update the import/includes of the source and destination libraries
            OwnershipEvent<TLLibrary,LibraryMember> removeEvent =
                new OwnershipEvent<>( ModelEventType.MEMBER_REMOVED, sourceLibrary, member );
            OwnershipEvent<TLLibrary,LibraryMember> addEvent =
                new OwnershipEvent<>( ModelEventType.MEMBER_ADDED, destLibrary, member );
            LibraryMemberChangeIntegrityChecker lmcListener = new LibraryMemberChangeIntegrityChecker();

            lmcListener.processModelEvent( removeEvent );
            lmcListener.processModelEvent( addEvent );

            // Update the typeName fields for all entities in the model that reference
            // the moved entity
            ValueChangeEvent<ModelElement,NamedEntity> nameChangeEvent =
                new ValueChangeEvent<>( ModelEventType.NAME_MODIFIED, member );
            new NameChangeIntegrityChecker().processModelEvent( nameChangeEvent );

        } finally {
            // Re-enable event processing, regardless of the result of the operation
            setListenersEnabled( true );
        }
    }

    /**
     * Clears all non-built-in libraries from the model. Any registered listeners are unaffected by this operation, but
     * no events are emitted by the removal of libraries from the model by this operation.
     */
    public void clearModel() {
        boolean listenerFlag = isListenersEnabled();

        setListenersEnabled( false );
        libraryList = new ArrayList<>();
        initModel();
        setListenersEnabled( listenerFlag );
    }

    /**
     * Returns the library with the specified namespace and name.
     * 
     * @param namespace the namespace of the library to return
     * @param libraryName the name of the library to return
     * @return AbstractLibrary
     */
    public AbstractLibrary getLibrary(String namespace, String libraryName) {
        AbstractLibrary library = null;

        if ((namespace != null) && (libraryName != null)) {
            for (AbstractLibrary lib : libraryList) {
                if (namespace.equals( lib.getNamespace() ) && libraryName.equals( lib.getName() )) {
                    library = lib;
                    break;
                }
            }
        }
        return library;
    }

    /**
     * Returns the library with the specified resource URL.
     * 
     * @param libraryUrl the resource URL of the library to return
     * @return AbstractLibrary
     */
    public AbstractLibrary getLibrary(URL libraryUrl) {
        AbstractLibrary library = null;

        if (libraryUrl != null) {
            String urlString = libraryUrl.toExternalForm();

            for (AbstractLibrary lib : libraryList) {
                if (urlString.equals( lib.getLibraryUrl().toExternalForm() )) {
                    library = lib;
                    break;
                }
            }
        }
        return library;
    }

    /**
     * Returns all libraries that belong to this model instance.
     * 
     * @return List&lt;AbstractLibrary&gt;
     */
    public List<AbstractLibrary> getAllLibraries() {
        return Collections.unmodifiableList( libraryList );
    }

    /**
     * Returns a list of all built-in libraries that belong to this model.
     * 
     * @return List&lt;BuiltInLibrary&gt;
     */
    public List<BuiltInLibrary> getBuiltInLibraries() {
        List<BuiltInLibrary> libraries = new ArrayList<>();

        for (AbstractLibrary lib : libraryList) {
            if (lib instanceof BuiltInLibrary) {
                libraries.add( (BuiltInLibrary) lib );
            }
        }
        return libraries;
    }

    /**
     * Returns a list of all built-in libraries that belong to this model.
     * 
     * @return List&lt;XSDLibrary&gt;
     */
    public List<XSDLibrary> getLegacySchemaLibraries() {
        List<XSDLibrary> libraries = new ArrayList<>();

        for (AbstractLibrary lib : libraryList) {
            if (lib instanceof XSDLibrary) {
                libraries.add( (XSDLibrary) lib );
            }
        }
        return libraries;
    }

    /**
     * Returns a list of all user-defined libraries that belong to this model.
     * 
     * @return List&lt;TLLibrary&gt;
     */
    public List<TLLibrary> getUserDefinedLibraries() {
        List<TLLibrary> libraries = new ArrayList<>();

        for (AbstractLibrary lib : libraryList) {
            if (lib instanceof TLLibrary) {
                libraries.add( (TLLibrary) lib );
            }
        }
        return libraries;
    }

    /**
     * Returns the libraries that are assigned to the specified namespace within this model instance.
     * 
     * @param namespace the namespace from which to return libraries
     * @return List&lt;AbstractLibrary&gt;
     */
    public List<AbstractLibrary> getLibrariesForNamespace(String namespace) {
        List<AbstractLibrary> libraries = new ArrayList<>();

        if (namespace != null) {
            for (AbstractLibrary library : libraryList) {
                if (namespace.equals( library.getNamespace() )) {
                    libraries.add( library );
                }
            }
        }
        return libraries;
    }

    /**
     * Registers the given listener for published events from this model.
     * 
     * @param listener the listener to register
     */
    public void addListener(ModelEventListener<?,?> listener) {
        if ((listener != null) && !listeners.contains( listener )) {
            listeners.add( listener );
        }
    }

    /**
     * Un-registers the given listener for published events from this model.
     * 
     * @param listener the listener to remove
     */
    public void removeListener(ModelEventListener<?,?> listener) {
        if (listeners.contains( listener )) {
            listeners.remove( listener );
        }
    }

    /**
     * Returns true if registered listeners are to be notified of model events.
     * 
     * @return boolean
     */
    public boolean isListenersEnabled() {
        return listenersEnabled;
    }

    /**
     * Assigns the flag value that indicates whenter registered listeners are to be notified of model events.
     * 
     * @param listenersEnabled the flag value to assign
     */
    public void setListenersEnabled(boolean listenersEnabled) {
        this.listenersEnabled = listenersEnabled;
    }

    /**
     * Initializes the model by adding all of the available built-in libraries.
     */
    protected void initModel() {
        for (BuiltInLibrary builtIn : BuiltInLibraryFactory.getInstance().getBuiltInLibraries()) {
            this.addLibrary( builtIn );
        }
        chameleonCounter = 0;

        // Resolve any type references between members of the built-in libraries
        ModelReferenceResolver.resolveReferences( this );
    }

    /**
     * Publishes the given event to all registered listeners that are capable of processing it.
     * 
     * @param event the event to publish
     * @param <E> the type of the event to publish
     */
    @SuppressWarnings("unchecked")
    protected <E extends ModelEvent<?>> void publishEvent(E event) {
        if ((event != null) && listenersEnabled) {
            List<ModelEventListener<?,?>> tempListeners = new ArrayList<>( listeners );

            for (ModelEventListener<?,?> listener : tempListeners) {
                if (event.canBeProcessedBy( listener )) {
                    ((ModelEventListener<E,?>) listener).processModelEvent( event );
                }
            }
        }
    }

    /**
     * Checks the namespace+name and resource URL of the given library against the current members of this model. If a
     * conflict is discovered, this method will throw an <code>IllegalArgumentException</code>. If the library is
     * already a member of this model, all of the other members will be checked. This is useful in the event of changes
     * to the name, namespace, or URL of the library.
     * 
     * <p>
     * NOTE: This method should be called before the library is added to the internal 'libraryList' of the model or, in
     * the case of updates to existing libraries, before the fields of the library are actually modified.
     * 
     * @param proposedNamespace the proposed namespace value for the library to be added or modified
     * @param proposedName the proposed name value for the library to be added or modified
     * @param proposedUrl the proposed resource URL for the library to be added or modified
     * @param existingLibrary the existing library member instance (may be null)
     * @throws IllegalArgumentException thrown if a name/namespace conflict is discovered
     */
    protected void checkDuplicateLibrary(String proposedNamespace, String proposedName, URL proposedUrl,
        AbstractLibrary existingLibrary) {
        for (AbstractLibrary memberLib : libraryList) {
            if (existingLibrary == memberLib) {
                continue;
            }
            boolean namespaceMatch = (proposedNamespace == null) ? (memberLib.getNamespace() == null)
                : proposedNamespace.equals( memberLib.getNamespace() );
            boolean nameMatch =
                (proposedName == null) ? (memberLib.getName() == null) : proposedName.equals( memberLib.getName() );

            if (namespaceMatch && nameMatch) {
                throw new IllegalArgumentException(
                    "A library with the requested name + namespace values already exists in the model." );

            } else {
                checkDuplicateUrl( proposedUrl, memberLib );
            }
        }
    }

    /**
     * Checks to see if the proposed URL is a duplicate of the URL for the current member library provided.
     * 
     * @param proposedUrl the proposed URL for the new library
     * @param memberLib the member library to check for duplication
     */
    private void checkDuplicateUrl(URL proposedUrl, AbstractLibrary memberLib) {
        URL memberUrl = memberLib.getLibraryUrl();
        boolean urlMatch;

        if (proposedUrl == null) {
            urlMatch = (memberUrl == null);

        } else {
            String proposedUrlStr = URLUtils.normalizeUrl( proposedUrl ).toExternalForm();
            String memberUrlStr = (memberUrl == null) ? "" : URLUtils.normalizeUrl( memberUrl ).toExternalForm();

            urlMatch = proposedUrlStr.equals( memberUrlStr );
        }

        if (urlMatch) {
            throw new IllegalArgumentException(
                "A library with the requested resource URL location already exists in the model." );
        }
    }

    /**
     * Automatically adds imports for all of the built-in libraries that have been regsitered with this model.
     * 
     * @param library the library to receive the imports
     */
    protected void addBuiltInImports(TLLibrary library) {
        Map<String,String> existingImports = new HashMap<>();

        // Collect namespace mappings from the library's existing imports
        for (TLNamespaceImport nsImport : library.getNamespaceImports()) {
            existingImports.put( nsImport.getPrefix(), nsImport.getNamespace() );
        }

        // Search the built-ins to determine whether new import statements are required
        for (BuiltInLibrary builtIn : getBuiltInLibraries()) {
            if (!builtIn.getSchemaDeclaration().isImportByDefault()
                || existingImports.containsValue( builtIn.getNamespace() )) {
                continue;
            }
            String prefix = builtIn.getPrefix();

            // Find a unique prefix for the built-in namespace
            if ((prefix == null) || existingImports.containsKey( prefix )) {
                if (prefix == null) {
                    prefix = "ns";
                }
                String uniquePrefix = prefix + "1";
                int prefixCounter = 1;

                while (existingImports.containsKey( uniquePrefix )) {
                    uniquePrefix = prefix + (prefixCounter++);
                }
                prefix = uniquePrefix;
            }
            library.addNamespaceImport( new TLNamespaceImport( prefix, builtIn.getNamespace() ) );
            existingImports.put( prefix, builtIn.getNamespace() );
        }
    }

}
