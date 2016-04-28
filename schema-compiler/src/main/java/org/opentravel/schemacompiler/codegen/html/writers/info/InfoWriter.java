/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers.info;

import org.opentravel.schemacompiler.codegen.html.Content;

/**
 * @author Eric.Bronson
 *
 */
public interface InfoWriter {
	
	/**
	 * Add the info tree.
	 * 
	 * @param memberTree
	 */
	public void addInfo(Content memberTree);
	
	/**
	 * @param title the title to set
	 */
	public void setTitle(Content title);
	
	/**
	 * @param title the title to set
	 */
	public void setCaption(String caption);

}
