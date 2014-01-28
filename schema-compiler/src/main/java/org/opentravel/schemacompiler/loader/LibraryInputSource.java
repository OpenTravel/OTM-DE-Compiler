package org.opentravel.schemacompiler.loader;

import java.net.URL;

/**
 * Input source used to access the content of a file-based type library module.
 * 
 * @param <C>
 *            the content type that will be returned by the input source
 * @author S. Livezey
 */
public interface LibraryInputSource<C> {

    /**
     * Returns the URL of the library module being accessed with this input source.
     * 
     * @return URL
     */
    public URL getLibraryURL();

    /**
     * Resolves the library module namespace provided and returns an input stream that can be used
     * to load the module's XML content. If the content of the module is not accessible, this method
     * will return null.
     * 
     * @param libraryContentURL
     *            the resource locator that can be used to obtain the module's content
     * @return C
     */
    public C getLibraryContent();

}
