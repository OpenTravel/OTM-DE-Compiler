
package org.opentravel.schemacompiler.repository;

import java.util.List;

import org.opentravel.schemacompiler.model.AbstractLibrary;

/**
 * Represents a <code>RepositoryItem</code> component that is accessible from a local model project.
 * 
 * @author S. Livezey
 */
public interface ProjectItem extends RepositoryItem {
	
	/**
	 * Returns the <code>ProjectManager</code> that owns this item.
	 * 
	 * @return ProjectManager
	 */
	public ProjectManager getProjectManager();
	
	/**
	 * Returns the list of projects of which this <code>ProjectItem</code> is a member.
	 * 
	 * @return List<Project>
	 */
	public List<Project> memberOfProjects();
	
	/**
	 * Returns the library content of this repository item.
	 * 
	 * @return AbstractLibrary
	 */
	public AbstractLibrary getContent();
	
	/**
	 * Returns true if the project item's content is to be considered read-only by an editor application.
	 * 
	 * @return boolean
	 */
	public boolean isReadOnly();
	
}
