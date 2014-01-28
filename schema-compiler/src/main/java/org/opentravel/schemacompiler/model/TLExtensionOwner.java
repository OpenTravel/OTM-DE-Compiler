package org.opentravel.schemacompiler.model;

/**
 * Interface to be implemented by model elements that can extend a single entity.
 * 
 * @author S. Livezey
 */
public interface TLExtensionOwner extends LibraryElement {

    /**
     * Returns the extension element for this entity.
     * 
     * @return TLExtension
     */
    public TLExtension getExtension();

    /**
     * Assigns the extension element for this entity.
     * 
     * @param extension
     *            the etension element to assign
     */
    public void setExtension(TLExtension extension);

}
