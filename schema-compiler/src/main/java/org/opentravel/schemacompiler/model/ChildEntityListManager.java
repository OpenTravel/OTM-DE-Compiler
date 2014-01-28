
package org.opentravel.schemacompiler.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventBuilder;
import org.opentravel.schemacompiler.event.ModelEventType;

/**
 * Base class that encapsulates the ownership of a list of child entities, including the supporting
 * methods used to manipulate the list of children.  This class is intended to be utilized as an internal
 * component of the model and should not be called or referenced beyond that scope.
 * 
 * @param <C>  the type of child entity being managed by this component
 * @param <O>  the type of the parent entity that owns and manages the underlying list
 * @author S. Livezey
 */
abstract class ChildEntityListManager<C,O> {
	
	private List<DerivedChildEntityListManager<C,?>> derivedListManagers = new ArrayList<DerivedChildEntityListManager<C,?>>();
	private List<C> children = new ArrayList<C>();
	private O owner;
	private ModelEventType addEventType;
	private ModelEventType removeEventType;
	
	/**
	 * Constructor that specifies the owner of the unerlying list, as well as the types of ownership
	 * events that will be published when a child entity is added or removed.
	 * 
	 * @param owner  the owner of the underlying list of children
	 * @param addEventType  the type of event to publish when a child entity is added
	 * @param removeEventType  the type of event to publish when a child entity is removed
	 */
	public ChildEntityListManager(O owner, ModelEventType addEventType, ModelEventType removeEventType) {
		this.owner = owner;
		this.addEventType = addEventType;
		this.removeEventType = removeEventType;
	}
	
	/**
	 * Adds a <code>DerivedChildEntityListManager</code> whose list of derived child entities should be synchronized
	 * with the list of children managed by this component instance.
	 * 
	 * @param derivedListManager  the manager for the list of derived child entities
	 */
	public void addDerivedListManager(DerivedChildEntityListManager<C,?> derivedListManager) {
		if ((derivedListManager != null) && !derivedListManagers.contains(derivedListManager)) {
			derivedListManagers.add(derivedListManager);
			
			for (int i = 0; i < children.size(); i++) {
				derivedListManager.addDerivedChild(i, children.get(i));
			}
		}
	}
	
	/**
	 * Removed the given <code>DerivedChildEntityListManager</code> from the current list.
	 * 
	 * @param derivedListManager  the manager for the list of derived child entities to remove
	 */
	public void removeDerivedListManager(DerivedChildEntityListManager<C,?> derivedListManager) {
		if (derivedListManagers.contains(derivedListManager)) {
			derivedListManagers.remove(derivedListManager);
		}
	}
	
	/**
	 * Returns the implementation-specific name of the given child.
	 * 
	 * @param child  the child entity whose name should be returned
	 * @return String
	 */
	protected abstract String getChildName(C child);
	
	/**
	 * Assigns the owner of the given child when it is added to the underlying list.
	 * 
	 * @param child  the child entity to which the owner will be assigned
	 * @param owner  the owning entity to assign (may be null)
	 */
	protected abstract void assignOwner(C child, O owner);
	
	/**
	 * Publishes the given event to all registered listeners of the owning model that are
	 * capable of processing it.
	 * 
	 * @param owner  the owning entity that will broadcast the event
	 * @param event  the event to publish
	 */
	protected abstract void publishEvent(O owner, ModelEvent<?> event);
	
	/**
	 * Returns the list of children for this entity.
	 *
	 * @return List<C>
	 */
	public List<C> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	/**
	 * Returns the child with the specified name or null if no such child has been defined.
	 * 
	 * @param childName  the name of the child to return
	 * @return C
	 */
	public C getChild(String childName) {
		C child = null;
		
		if (childName != null) {
			for (C c : children) {
				if (childName.equals( getChildName(c) )) {
					child = c;
					break;
				}
			}
		}
		return child;
	}
	
	/**
	 * Adds a child to the current list.
	 * 
	 * @param child  the child to add
	 */
	public void addChild(C child) {
		addChild(children.size(), child);
	}
	
	/**
	 * Adds a child to the current list.
	 * 
	 * @param index  the index at which the given child should be added
	 * @param child  the child to add
	 * @throws IndexOutOfBoundsException  thrown if the index is out of range (index < 0 || index > size())
	 */
	public void addChild(int index, C child) {
		if ((child != null) && !children.contains(child)) {
			assignOwner(child, owner);
			children.add(index, child);
			publishEvent(owner, new ModelEventBuilder(addEventType, owner).setAffectedItem(child).buildEvent());
			
			for (DerivedChildEntityListManager<C,?> derivedListManager : derivedListManagers) {
				derivedListManager.addDerivedChild(index, child);
			}
		}
	}
	
	/**
	 * Removes the specified child from the current list.
	 * 
	 * @param child  the child to remove
	 */
	public void removeChild(C child) {
		if (children.contains(child)) {
			assignOwner(child, null);
			children.remove(child);
			publishEvent(owner, new ModelEventBuilder(removeEventType, owner).setAffectedItem(child).buildEvent());
			
			for (DerivedChildEntityListManager<C,?> derivedListManager : derivedListManagers) {
				derivedListManager.removeDerivedChild(child);
			}
		}
	}
	
	/**
	 * Removes all children from the current list.  Events will be emitted for each child that
	 * is removed.
	 */
	public void clearChildren() {
		List<C> childList = new ArrayList<C>(children);
		
		for (C child : childList) {
			removeChild(child);
		}
	}
	
	/**
	 * Moves this child up by one position in the list.  If the child is not owned by this
	 * object or it is already at the front of the list, this method has no effect.
	 * 
	 * @param child  the child to move
	 */
	public void moveUp(C child) {
		int currentIndex = children.indexOf(child);
		
		if (currentIndex > 0) {
			children.remove(currentIndex);
			children.add(currentIndex - 1, child);
			
			for (DerivedChildEntityListManager<C,?> derivedListManager : derivedListManagers) {
				derivedListManager.moveDerivedChildUp(child);
			}
		}
	}
	
	/**
	 * Moves this child down by one position in the list.  If the child is not owned by this
	 * object or it is already at the end of the list, this method has no effect.
	 * 
	 * @param child  the child to move
	 */
	public void moveDown(C child) {
		int currentIndex = children.indexOf(child);
		
		if ((currentIndex >= 0) && (currentIndex < (children.size() - 1))) {
			children.remove(currentIndex);
			children.add(currentIndex + 1, child);
			
			for (DerivedChildEntityListManager<C,?> derivedListManager : derivedListManagers) {
				derivedListManager.moveDerivedChildDown(child);
			}
		}
	}
	
	/**
	 * Sorts the list of childes using the comparator provided.
	 * 
	 * @param comparator  the comparator to use when sorting the list
	 */
	public void sortChildren(Comparator<C> comparator) {
		if (comparator != null) {
			Collections.sort(children, comparator);
			
			for (DerivedChildEntityListManager<C,?> derivedListManager : derivedListManagers) {
				derivedListManager.sortDerivedChildren(children);
			}
		}
	}
	
}
