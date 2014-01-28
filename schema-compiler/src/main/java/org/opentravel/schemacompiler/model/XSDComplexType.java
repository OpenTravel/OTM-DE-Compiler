package org.opentravel.schemacompiler.model;

import java.util.ArrayList;
import java.util.List;

import org.w3._2001.xmlschema.TopLevelComplexType;

/**
 * Library member that represents a complex type declaration from a legacy XML schema.
 * 
 * <p>
 * Complex type definitions can have implicit associations with <code>XSDElement</code> entities.
 * Assuming both definitions are in the same namespace, and element that references an
 * <code>XSDComplexType</code> is assumed to be an alias of the type. If the alias has the same name
 * as the complex type, it is considered to be the identity alias. A complex type can have multiple
 * aliases, but only one identity alias (if one is defined at all).
 * 
 * @author S. Livezey
 */
public class XSDComplexType extends LibraryMember implements TLPropertyType {

    private String name;
    private TopLevelComplexType jaxbType;
    private XSDElement identityAlias;
    private List<XSDElement> aliases = new ArrayList<XSDElement>();

    /**
     * Constructor that specifies the name of this model element and the underlying JAXB type from
     * which it was created.
     * 
     * @param name
     *            the name of the model element
     * @param jaxbType
     *            the JAXB type that was used to create this element
     */
    public XSDComplexType(String name, TopLevelComplexType jaxbType) {
        this.name = name;
        this.jaxbType = jaxbType;
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        AbstractLibrary owningLibrary = getOwningLibrary();
        StringBuilder identity = new StringBuilder();

        if (owningLibrary != null) {
            identity.append(owningLibrary.getValidationIdentity()).append(" : ");
        }
        if (name == null) {
            identity.append("[Unnamed XSD Complex Type]");
        } else {
            identity.append(name);
        }
        return identity.toString();
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
     * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
     */
    @Override
    public String getLocalName() {
        return name;
    }

    /**
     * Returns the value of the 'jaxbType' field.
     * 
     * @return TopLevelComplexType
     */
    public TopLevelComplexType getJaxbType() {
        return jaxbType;
    }

    /**
     * Returns the global element that aliases this complex type with the same name.
     * 
     * @return XSDElement
     */
    public XSDElement getIdentityAlias() {
        return identityAlias;
    }

    /**
     * Assigns the global element that aliases this complex type with the same name.
     * 
     * @param identityAlias
     *            the identity alias element to assign
     */
    public void setIdentityAlias(XSDElement identityAlias) {
        if (this.identityAlias != null) {
            this.identityAlias.setAliasedType(null);
        }
        if (identityAlias != null) {
            identityAlias.setAliasedType(this);
        }
        this.identityAlias = identityAlias;
    }

    /**
     * Returns the list of elements that alias this complex type with a different name.
     * 
     * @return List<XSDElement>
     */
    public List<XSDElement> getAliases() {
        return aliases;
    }

    /**
     * Adds an alias element to the current list.
     * 
     * @param alias
     *            the alias element to add
     */
    public void addAlias(XSDElement alias) {
        if ((alias != null) && !this.aliases.contains(alias)) {
            alias.setAliasedType(this);
            this.aliases.add(alias);
        }
    }

    /**
     * Removes an alias element to the current list.
     * 
     * @param alias
     *            the alias element to remove
     */
    public void removeAlias(XSDElement alias) {
        if ((alias != null) && this.aliases.contains(alias)) {
            alias.setAliasedType(null);
            this.aliases.remove(alias);
        }
    }

}
