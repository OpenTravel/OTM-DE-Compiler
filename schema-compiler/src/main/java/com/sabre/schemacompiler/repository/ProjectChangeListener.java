/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.repository;

/**
 * Defines the interface for components that need to be notified when a <code>Project</code> or
 * one of its <code>ProjectItem</code> members is modified.
 * 
 * @author S. Livezey
 */
public interface ProjectChangeListener {
	
	/**
	 * Called when the identity and/or descriptive information about a project has been modified.
	 * 
	 * @param project  the project that was modified
	 */
	public void projectInformationModified(Project project);
	
	/**
	 * Called when a <code>ProjectItem</code> member is added to the specified project.
	 * 
	 * @param project  the project to which the new member was added
	 * @param item  the project item that was added
	 */
	public void projectItemAdded(Project project, ProjectItem item);
	
	/**
	 * Called when a <code>ProjectItem</code> member is removed from the specified project.
	 * 
	 * @param project  the project from which the new member was removed
	 * @param item  the project item that was removed
	 */
	public void projectItemRemoved(Project project, ProjectItem item);
	
}
