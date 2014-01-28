package org.opentravel.schemacompiler.model;

import java.util.Comparator;
import java.util.List;

import org.opentravel.schemacompiler.model.TLRole.RoleListManager;

/**
 * The role enumeration is a specialized open enumeration composed of the roles defined for a core
 * object.
 * 
 * @author S. Livezey
 */
public class TLRoleEnumeration extends TLModelElement implements TLAttributeType {

    private TLCoreObject owningEntity;
    private RoleListManager roleManager = new RoleListManager(this);

    /**
     * Constructor that specifies the owning core object for the enumeration instance.
     * 
     * @param owningEntity
     *            the owning core object for the enumeration
     */
    public TLRoleEnumeration(TLCoreObject owningEntity) {
        this.owningEntity = owningEntity;
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();

        if (owningEntity != null) {
            identity.append(owningEntity.getValidationIdentity()).append(" : ");
        }
        identity.append("Role Enumeration");
        return identity.toString();
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAttributeType#getXSDFacetProfile()
     */
    @Override
    public XSDFacetProfile getXSDFacetProfile() {
        return null;
    }

    /**
     * @see org.opentravel.schemacompiler.model.NamedEntity#getNamespace()
     */
    @Override
    public String getNamespace() {
        return (owningEntity == null) ? null : owningEntity.getNamespace();
    }

    /**
     * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
     */
    @Override
    public String getLocalName() {
        return "Enum_" + ((owningEntity == null) ? "UnknownCore" : owningEntity.getLocalName())
                + "Role";
    }

    /**
     * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
     */
    @Override
    public AbstractLibrary getOwningLibrary() {
        return (owningEntity == null) ? null : owningEntity.getOwningLibrary();
    }

    /**
     * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
     */
    @Override
    public TLModel getOwningModel() {
        return (owningEntity == null) ? null : owningEntity.getOwningModel();
    }

    /**
     * Returns the value of the 'owningEntity' field.
     * 
     * @return TLCoreObject
     */
    public TLCoreObject getOwningEntity() {
        return owningEntity;
    }

    /**
     * Returns the value of the 'roles' field.
     * 
     * @return List<TLRole>
     */
    public List<TLRole> getRoles() {
        return roleManager.getChildren();
    }

    /**
     * Adds a role string value to the current list.
     * 
     * @param role
     *            the role string value to add
     */
    public void addRole(TLRole role) {
        roleManager.addChild(role);
    }

    /**
     * Adds a <code>TLRole</code> element to the current list.
     * 
     * @param index
     *            the index at which the given role should be added
     * @param role
     *            the role value to add
     * @throws IndexOutOfBoundsException
     *             thrown if the index is out of range (index < 0 || index > size())
     */
    public void addRole(int index, TLRole role) {
        roleManager.addChild(index, role);
    }

    /**
     * Removes the specified role string value from the current list.
     * 
     * @param role
     *            the role string value to remove
     */
    public void removeRole(TLRole role) {
        roleManager.removeChild(role);
    }

    /**
     * Moves this role up by one position in the list. If the role is not owned by this object or it
     * is already at the front of the list, this method has no effect.
     * 
     * @param role
     *            the role to move
     */
    public void moveUp(TLRole role) {
        roleManager.moveUp(role);
    }

    /**
     * Moves this role down by one position in the list. If the role is not owned by this object or
     * it is already at the end of the list, this method has no effect.
     * 
     * @param role
     *            the role to move
     */
    public void moveDown(TLRole role) {
        roleManager.moveDown(role);
    }

    /**
     * Sorts the list of roles using the comparator provided.
     * 
     * @param comparator
     *            the comparator to use when sorting the list
     */
    public void sortRoles(Comparator<TLRole> comparator) {
        roleManager.sortChildren(comparator);
    }

}
