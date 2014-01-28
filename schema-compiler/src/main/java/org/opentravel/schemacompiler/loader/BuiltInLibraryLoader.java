package org.opentravel.schemacompiler.loader;

import org.opentravel.schemacompiler.model.BuiltInLibrary;

/**
 * Interface to be implemented by components that are capable of loading a single built-in library.
 * 
 * @author S. Livezey
 */
public interface BuiltInLibraryLoader {

    /**
     * Loads and returs a single built-in library to be incorporated in a new <code>TLModel</code>
     * instance.
     * 
     * @return BuiltInLibrary
     * @throws LibraryLoaderException
     */
    public BuiltInLibrary loadBuiltInLibrary() throws LibraryLoaderException;

}
