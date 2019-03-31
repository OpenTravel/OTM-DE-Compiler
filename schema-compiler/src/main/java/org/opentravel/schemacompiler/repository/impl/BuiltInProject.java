/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.schemacompiler.repository.impl;

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;

import java.io.File;
import java.net.URL;
import java.util.Locale;

/**
 * Specialized project type used to represent the built-in libraries of a model.
 * 
 * @author S. Livezey
 */
public class BuiltInProject extends Project {

    private static final String OPERATION_NOT_SUPPORTED = "Operation not supported for the built-in project.";

    public static final String BUILTIN_PROJECT_ID = "http://opentravel.org/schemacompiler/projects/built-in";
    public static final String BUILTIN_PROJECT_NAME_KEY = "schemacompiler.project.builtInProject.name";

    /**
     * Constructor that assigns the project manager and the model instance that will provide the built-in libraries to
     * be owned by this project.
     * 
     * @param projectManager the project manager instance
     */
    public BuiltInProject(ProjectManager projectManager) {
        super( projectManager );
        super.setProjectId( BUILTIN_PROJECT_ID );
        super.setName( SchemaCompilerApplicationContext.getContext().getMessage( BUILTIN_PROJECT_NAME_KEY, null,
            Locale.getDefault() ) );

        for (BuiltInLibrary builtInLibrary : projectManager.getModel().getBuiltInLibraries()) {
            super.add( ProjectItemImpl.newUnmanagedItem( builtInLibrary, projectManager ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Project#setProjectId(java.lang.String)
     */
    @Override
    public void setProjectId(String projectId) {
        throw new UnsupportedOperationException( OPERATION_NOT_SUPPORTED );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Project#setProjectFile(java.io.File)
     */
    @Override
    public void setProjectFile(File projectFile) {
        throw new UnsupportedOperationException( OPERATION_NOT_SUPPORTED );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Project#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException( OPERATION_NOT_SUPPORTED );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Project#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        throw new UnsupportedOperationException( OPERATION_NOT_SUPPORTED );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Project#add(org.opentravel.schemacompiler.repository.ProjectItem)
     */
    @Override
    protected void add(ProjectItem item) {
        throw new UnsupportedOperationException( OPERATION_NOT_SUPPORTED );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Project#remove(org.opentravel.schemacompiler.repository.ProjectItem)
     */
    @Override
    public void remove(ProjectItem item) {
        throw new UnsupportedOperationException( OPERATION_NOT_SUPPORTED );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Project#remove(org.opentravel.schemacompiler.model.AbstractLibrary)
     */
    @Override
    public void remove(AbstractLibrary library) {
        throw new UnsupportedOperationException( OPERATION_NOT_SUPPORTED );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Project#remove(java.net.URL)
     */
    @Override
    public void remove(URL libraryUrl) {
        throw new UnsupportedOperationException( OPERATION_NOT_SUPPORTED );
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Project#getDefaultItem()
     */
    @Override
    public ProjectItem getDefaultItem() {
        return null;
    }

    /**
     * @see org.opentravel.schemacompiler.repository.Project#setDefaultItem(org.opentravel.schemacompiler.repository.ProjectItem)
     */
    @Override
    public void setDefaultItem(ProjectItem defaultItem) {
        throw new UnsupportedOperationException( OPERATION_NOT_SUPPORTED );
    }

}
