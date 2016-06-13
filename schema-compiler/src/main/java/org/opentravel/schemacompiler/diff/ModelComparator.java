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

package org.opentravel.schemacompiler.diff;

import org.opentravel.schemacompiler.diff.impl.EntityComparator;
import org.opentravel.schemacompiler.diff.impl.EntityComparisonFacade;
import org.opentravel.schemacompiler.diff.impl.LibraryComparator;
import org.opentravel.schemacompiler.diff.impl.ProjectComparator;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.repository.Project;

/**
 * Performs model-level comparisons of two OTM model components.  The algorithm can handle
 * comparisons of two versions of the same OTM component or two different components that
 * are otherwise unrelated.  There is no requirement for both components to exist within
 * the same OTM model instance.
 */
public class ModelComparator {
	
	/**
	 * Compares two versions of the same OTM project.
	 * 
	 * @param oldProject  the old project version
	 * @param newProject  the new project version
	 * @return ProjectChangeSet
	 */
	public static ProjectChangeSet compareProjects(Project oldProject, Project newProject) {
		return new ProjectComparator().compareProjects( oldProject, newProject );
	}
	
	/**
	 * Compares two versions of the same OTM library.
	 * 
	 * @param oldLibrary  the old library version
	 * @param newLibrary  the new library version
	 * @return LibraryChangeSet
	 */
	public static LibraryChangeSet compareLibraries(TLLibrary oldLibrary, TLLibrary newLibrary) {
		return new LibraryComparator().compareLibraries( oldLibrary, newLibrary );
	}
	
	/**
	 * Compares two versions of the same OTM entity.
	 * 
	 * @param oldEntity  the old entity version
	 * @param newEntity  the new entity version
	 * @return EntityChangeSet
	 */
	public static EntityChangeSet compareEntities(NamedEntity oldEntity, NamedEntity newEntity) {
		return new EntityComparator().compareEntities(
				new EntityComparisonFacade( oldEntity ), new EntityComparisonFacade( newEntity ) );
	}
	
}
