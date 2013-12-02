/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.repository.impl;

import java.io.File;
import java.net.URL;
import java.util.Locale;

import com.sabre.schemacompiler.ioc.SchemaCompilerApplicationContext;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.BuiltInLibrary;
import com.sabre.schemacompiler.repository.Project;
import com.sabre.schemacompiler.repository.ProjectItem;
import com.sabre.schemacompiler.repository.ProjectManager;

/**
 * Specialized project type used to represent the built-in libraries of a model.
 * 
 * @author S. Livezey
 */
public class BuiltInProject extends Project {
	
	public static final String BUILTIN_PROJECT_ID = "http://opentravel.org/schemacompiler/projects/built-in";
	public static final String BUILTIN_PROJECT_NAME_KEY = "schemacompiler.project.builtInProject.name";
	
	/**
	 * Constructor that assigns the project manager and the model instance that will provide
	 * the built-in libraries to be owned by this project.
	 * 
	 * @param projectManager  the project manager instance
	 */
	public BuiltInProject(ProjectManager projectManager) {
		super( projectManager );
		super.setProjectId(BUILTIN_PROJECT_ID);
		super.setName( SchemaCompilerApplicationContext.getContext().getMessage(
				BUILTIN_PROJECT_NAME_KEY, null, Locale.getDefault()) );
		
		for (BuiltInLibrary builtInLibrary : projectManager.getModel().getBuiltInLibraries()) {
			super.add( ProjectItemImpl.newUnmanagedItem(null, builtInLibrary, projectManager) );
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.repository.Project#setProjectId(java.lang.String)
	 */
	@Override
	public void setProjectId(String projectId) {
		throw new UnsupportedOperationException("Operation not supported for the built-in project.");
	}

	/**
	 * @see com.sabre.schemacompiler.repository.Project#setProjectFile(java.io.File)
	 */
	@Override
	public void setProjectFile(File projectFile) {
		throw new UnsupportedOperationException("Operation not supported for the built-in project.");
	}

	/**
	 * @see com.sabre.schemacompiler.repository.Project#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("Operation not supported for the built-in project.");
	}

	/**
	 * @see com.sabre.schemacompiler.repository.Project#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
		throw new UnsupportedOperationException("Operation not supported for the built-in project.");
	}

	/**
	 * @see com.sabre.schemacompiler.repository.Project#add(com.sabre.schemacompiler.repository.ProjectItem)
	 */
	@Override
	protected void add(ProjectItem item) {
		throw new UnsupportedOperationException("Operation not supported for the built-in project.");
	}

	/**
	 * @see com.sabre.schemacompiler.repository.Project#remove(com.sabre.schemacompiler.repository.ProjectItem)
	 */
	@Override
	public void remove(ProjectItem item) {
		throw new UnsupportedOperationException("Operation not supported for the built-in project.");
	}

	/**
	 * @see com.sabre.schemacompiler.repository.Project#remove(com.sabre.schemacompiler.model.AbstractLibrary)
	 */
	@Override
	public void remove(AbstractLibrary library) {
		throw new UnsupportedOperationException("Operation not supported for the built-in project.");
	}

	/**
	 * @see com.sabre.schemacompiler.repository.Project#remove(java.net.URL)
	 */
	@Override
	public void remove(URL libraryUrl) {
		throw new UnsupportedOperationException("Operation not supported for the built-in project.");
	}

	/**
	 * @see com.sabre.schemacompiler.repository.Project#getDefaultItem()
	 */
	@Override
	public ProjectItem getDefaultItem() {
		return null;
	}

	/**
	 * @see com.sabre.schemacompiler.repository.Project#setDefaultItem(com.sabre.schemacompiler.repository.ProjectItem)
	 */
	@Override
	public void setDefaultItem(ProjectItem defaultItem) {
		throw new UnsupportedOperationException("Operation not supported for the built-in project.");
	}
	
}
