/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.model;

import java.util.Comparator;
import java.util.List;


/**
 * Interface to be implemented by any model component that can provide an example of its usage.
 * 
 * @author S. Livezey
 */
public interface TLExampleOwner extends LibraryElement {
	
	/**
	 * Returns the value of the 'examples' field.
	 *
	 * @return List<TLExample>
	 */
	public List<TLExample> getExamples();
	
	/**
	 * Returns the example with the specified context ID.
	 * 
	 * @param contextId  the context of the example to return
	 * @return TLExample
	 */
	public TLExample getExample(String contextId);
	
	/**
	 * Adds a <code>TLExample</code> element to the current list.
	 * 
	 * @param example  the example value to add
	 */
	public void addExample(TLExample example);
	
	/**
	 * Adds a <code>TLExample</code> element to the current list.
	 * 
	 * @param index  the index at which the given example should be added
	 * @param example  the example value to add
	 * @throws IndexOutOfBoundsException  thrown if the index is out of range (index < 0 || index > size())
	 */
	public void addExample(int index, TLExample example);
	
	/**
	 * Removes the specified <code>TLExample</code> from the current list.
	 * 
	 * @param example  the example value to remove
	 */
	public void removeExample(TLExample example);
	
	/**
	 * Moves this example up by one position in the list.  If the example is not owned by this
	 * object or it is already at the front of the list, this method has no effect.
	 * 
	 * @param example  the example to move
	 */
	public void moveUp(TLExample example);
	
	/**
	 * Moves this example down by one position in the list.  If the example is not owned by this
	 * object or it is already at the end of the list, this method has no effect.
	 * 
	 * @param example  the example to move
	 */
	public void moveDown(TLExample example);
	
	/**
	 * Sorts the list of examples using the comparator provided.
	 * 
	 * @param comparator  the comparator to use when sorting the list
	 */
	public void sortExamples(Comparator<TLExample> comparator);
	
}
