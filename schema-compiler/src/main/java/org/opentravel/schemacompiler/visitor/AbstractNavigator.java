package org.opentravel.schemacompiler.visitor;

import java.util.HashSet;
import java.util.Set;

/**
 * Base navigator class that provides common methods for navigation and duplicate node avoidance
 * during the traversal process.
 * 
 * @param <T>
 *            the type of the root-level entity whose structure will be navigated
 * @author S. Livezey
 */
public abstract class AbstractNavigator<T> {

    protected ModelElementVisitor visitor;
    protected Set<Object> visitedNodes = new HashSet<Object>();

    /**
     * Default constructor.
     */
    public AbstractNavigator() {
    }

    /**
     * Constructor that initializes the visitor to be notified when child entities are encountered
     * during navigation.
     * 
     * @param visitor
     *            the visitor to be notified when child entities are encountered
     */
    public AbstractNavigator(ModelElementVisitor visitor) {
        if (visitor == null) {
            throw new NullPointerException("The model element visitor cannot be null.");
        }
        this.visitor = visitor;
    }

    /**
     * Navigates the entity and its children, invoking the applicable visitor method each time an
     * entity of the structure is encountered.
     * 
     * @param target
     *            the target entity to navigate
     */
    public abstract void navigate(T target);

    /**
     * Assigns the visitor to be used by this navigator instance.
     * 
     * @param visitor
     *            the visitor instance to assign
     */
    public void setVisitor(ModelElementVisitor visitor) {
        this.visitor = visitor;
    }

    /**
     * Returns true if the given node can be visited and its children traversed. This base class
     * implementation only checks to ensure the node is non-null and that it is not in the list of
     * already-visited elements. Sub-classes may add additional logic if required.
     * 
     * <p>
     * NOTE: A side effect of this method is that the node being checked is automatically marked as
     * visited. Hence, a direct call to 'addVisitedNode()' is redundant unless the objective is to
     * skip visitation of the node altogether.
     * 
     * @param node
     *            the node instance to check
     * @return boolean
     */
    protected boolean canVisit(Object node) {
        boolean visitAllowed = !alreadyVisited(node);

        addVisitedNode(node);
        return visitAllowed;
    }

    /**
     * Performs the same function as the 'canVisit()' method, but does not automatically mark the
     * node as visited.
     * 
     * @param node
     *            the node instance to check
     * @return boolean
     */
    protected boolean alreadyVisited(Object node) {
        return (node == null) || visitedNodes.contains(node);
    }

    /**
     * Adds the given node to the collection of visited entities.
     * 
     * @param node
     *            the node to add
     */
    protected void addVisitedNode(Object node) {
        if ((node != null) && !visitedNodes.contains(node)) {
            visitedNodes.add(node);
        }
    }

}
