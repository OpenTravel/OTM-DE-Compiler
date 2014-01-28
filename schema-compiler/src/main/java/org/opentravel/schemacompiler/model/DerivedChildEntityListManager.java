
package org.opentravel.schemacompiler.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface used to manage a list of derived entities
 * 
 * @param <E>  the type of the original entity from which each derived entity is created
 * @param <D>  the type of the entity that is derived from the original
 * @author S. Livezey
 */
public abstract class DerivedChildEntityListManager<E,D> {
	
	private ChildEntityListManager<D,?> derivedEntityManager;
	
	/**
	 * Constructor that specifies the <code>ChildEntityListManager</code> that will be used to manage
	 * the list of derived entities.
	 * 
	 * @param derivedEntityManager  the manager for the list of derived entities
	 */
	public DerivedChildEntityListManager(ChildEntityListManager<D,?> derivedEntityManager) {
		this.derivedEntityManager = derivedEntityManager;
	}
	
	/**
	 * Returns the name of the original entity instance provided.
	 * 
	 * @param originalEntity  the original entity for which to return a name
	 * @return String
	 */
	protected abstract String getOriginalEntityName(E originalEntity);
	
	/**
	 * Returns the name of a derived entity whose existence is derived from the original
	 * entity whose name is specified in the string parameter passed to this method.
	 * 
	 * @param originalEntityName  the name of the original entity from which the resulting name should be derived
	 * @return String
	 */
	protected abstract String getDerivedEntityName(String originalEntityName);
	
	/**
	 * Constructs a new derived entity instance using the original entity as a template.
	 * 
	 * @param originalEntity  the original entity from which the resulting object is to be derived
	 * @return D
	 */
	protected abstract D createDerivedEntity(E originalEntity);
	
	/**
	 * Adds a derived child entity that corresponds to the original one provided.
	 * 
	 * @param index  the index at which the derived child should be added
	 * @param originalChild  the original child entity that was added
	 */
	public void addDerivedChild(int index, E originalChild) {
		derivedEntityManager.addChild( index, createDerivedEntity(originalChild) );
	}
	
	/**
	 * Removes the derived child entity from the list that corresponds to the original one provided.
	 * 
	 * @param originalChild  the original child entity that was removed
	 */
	public void removeDerivedChild(E originalChild) {
		String derivedEntityName = getDerivedEntityName( getOriginalEntityName(originalChild) );
		D derivedEntity = derivedEntityManager.getChild(derivedEntityName);
		
		if (derivedEntity != null) {
			derivedEntityManager.removeChild( derivedEntity );
		}
	}
	
	/**
	 * Moves the derived child entity that corresponds to the original one provided up one position
	 * in the list of derived children.
	 * 
	 * @param originalChild  the original child entity that was moved
	 */
	public void moveDerivedChildUp(E originalChild) {
		String derivedEntityName = getDerivedEntityName( getOriginalEntityName(originalChild) );
		D derivedEntity = derivedEntityManager.getChild(derivedEntityName);
		
		if (derivedEntity != null) {
			derivedEntityManager.moveUp( derivedEntity );
		}
	}
	
	/**
	 * Moves the derived child entity that corresponds to the original one provided down one position
	 * in the list of derived children.
	 * 
	 * @param originalChild  the original child entity that was moved
	 */
	public void moveDerivedChildDown(E originalChild) {
		String derivedEntityName = getDerivedEntityName( getOriginalEntityName(originalChild) );
		D derivedEntity = derivedEntityManager.getChild(derivedEntityName);
		
		if (derivedEntity != null) {
			derivedEntityManager.moveDown( derivedEntity );
		}
	}
	
	/**
	 * Sorts the list of derived children in the same order as the corresponding list of original child
	 * entities provided.
	 * 
	 * @param originalEntityList  the list of original child entities from which the order of the derived entities is to be derived
	 */
	public void sortDerivedChildren(List<E> originalEntityList) {
		derivedEntityManager.sortChildren( new DerivedEntityComparator(originalEntityList) );
	}
	
	/**
	 * Comparator used to sort the list of derived entities in the same order as their corresponding
	 * position(s) in the list of original entities.
	 *
	 * @author S. Livezey
	 */
	private class DerivedEntityComparator implements Comparator<D> {
		
		private Map<D,Integer> derivedEntityPositions = new HashMap<D,Integer>();
		
		/**
		 * Constructor used to initialize the comparator with the relative positions of the original
		 * entities in the list provided.
		 * 
		 * @param originalEntityList  the list of original entities from which to sort
		 */
		public DerivedEntityComparator(List<E> originalEntityList) {
			for (int i = 0; i < originalEntityList.size(); i++) {
				E originalEntity = originalEntityList.get(i);
				String derivedEntityName = getDerivedEntityName( getOriginalEntityName(originalEntity) );
				D derivedEntity = derivedEntityManager.getChild(derivedEntityName);
				
				if (derivedEntity != null) {
					derivedEntityPositions.put(derivedEntity, i);
				}
			}
		}
		
		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(D entity1, D entity2) {
			Integer entity1Position = derivedEntityPositions.containsKey(entity1) ? derivedEntityPositions.get(entity1) : -1;
			Integer entity2Position = derivedEntityPositions.containsKey(entity2) ? derivedEntityPositions.get(entity2) : -1;
			
			return entity1Position.compareTo(entity2Position);
		}
		
	}
	
}
