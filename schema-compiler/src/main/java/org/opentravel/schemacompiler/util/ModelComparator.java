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

package org.opentravel.schemacompiler.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.opentravel.schemacompiler.diff.EntityChangeSet;
import org.opentravel.schemacompiler.diff.LibraryChangeSet;
import org.opentravel.schemacompiler.diff.ModelCompareOptions;
import org.opentravel.schemacompiler.diff.ProjectChangeSet;
import org.opentravel.schemacompiler.diff.ResourceChangeSet;
import org.opentravel.schemacompiler.diff.impl.DisplayFormatter;
import org.opentravel.schemacompiler.diff.impl.EntityComparator;
import org.opentravel.schemacompiler.diff.impl.EntityComparisonFacade;
import org.opentravel.schemacompiler.diff.impl.LibraryComparator;
import org.opentravel.schemacompiler.diff.impl.ProjectComparator;
import org.opentravel.schemacompiler.diff.impl.ResourceComparator;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.repository.Project;

/**
 * Performs model-level comparisons of two OTM model components.  The algorithm can handle
 * comparisons of two versions of the same OTM component or two different components that
 * are otherwise unrelated.  There is no requirement for both components to exist within
 * the same OTM model instance.
 */
public class ModelComparator {
	
	private static final String TEMPLATE_FOLDER = "/org/opentravel/schemacompiler/templates";
	private static final String PROJECT_DIFF_TEMPLATE  = TEMPLATE_FOLDER + "/project-diff-report.vm";
	private static final String LIBRARY_DIFF_TEMPLATE  = TEMPLATE_FOLDER + "/library-diff-report.vm";
	private static final String ENTITY_DIFF_TEMPLATE   = TEMPLATE_FOLDER + "/entity-diff-report.vm";
	private static final String RESOURCE_DIFF_TEMPLATE = TEMPLATE_FOLDER + "/resource-diff-report.vm";
	
	private static final String VELOCITY_CONFIG_FILE = TEMPLATE_FOLDER + "/velocity.properties";
	
	private static VelocityEngine velocityEngine;
	
	private ModelCompareOptions compareOptions;
	
	/**
	 * Default constructor.
	 */
	public ModelComparator() {}
	
	/**
	 * Constructor that initializes the comparison options for the comparator.
	 * 
	 * @param compareOptions  the model comparison options to apply during processing
	 */
	public ModelComparator(ModelCompareOptions compareOptions) {
		this.compareOptions = compareOptions;
	}
	
	/**
	 * Compares two versions of the same OTM project.
	 * 
	 * @param oldProject  the old project version
	 * @param newProject  the new project version
	 * @return ProjectChangeSet
	 */
	public ProjectChangeSet compareProjects(Project oldProject, Project newProject) {
		return new ProjectComparator( compareOptions ).compareProjects( oldProject, newProject );
	}
	
	/**
	 * Compares two versions of the same OTM project and writes a formatted HTML report of the
	 * results to the output stream provided.
	 * 
	 * @param oldProject  the old project version
	 * @param newProject  the new project version
	 * @param out  the output stream to which the formatted report will be written
	 * @throws IOException  thrown if an error occurs during report generation
	 */
	public void compareProjects(Project oldProject, Project newProject, OutputStream out)
			throws IOException {
		generateReport( compareProjects( oldProject, newProject ), PROJECT_DIFF_TEMPLATE, out );
	}
	
	/**
	 * Compares two versions of the same OTM library.
	 * 
	 * @param oldLibrary  the old library version
	 * @param newLibrary  the new library version
	 * @return LibraryChangeSet
	 */
	public LibraryChangeSet compareLibraries(TLLibrary oldLibrary, TLLibrary newLibrary) {
		LibraryComparator comparator = new LibraryComparator( compareOptions, null );
		
		comparator.addNamespaceMapping( oldLibrary.getNamespace(), newLibrary.getNamespace() );
		return comparator.compareLibraries( oldLibrary, newLibrary );
	}
	
	/**
	 * Compares two versions of the same OTM library and writes a formatted HTML report of the
	 * results to the output stream provided.
	 * 
	 * @param oldLibrary  the old library version
	 * @param newLibrary  the new library version
	 * @param out  the output stream to which the formatted report will be written
	 * @throws IOException  thrown if an error occurs during report generation
	 */
	public void compareLibraries(TLLibrary oldLibrary, TLLibrary newLibrary, OutputStream out)
			throws IOException {
		generateReport( compareLibraries( oldLibrary, newLibrary), LIBRARY_DIFF_TEMPLATE, out );
	}
	
	/**
	 * Compares two versions of the same OTM entity.
	 * 
	 * @param oldEntity  the old entity version
	 * @param newEntity  the new entity version
	 * @return EntityChangeSet
	 */
	public EntityChangeSet compareEntities(NamedEntity oldEntity, NamedEntity newEntity) {
		if ((oldEntity instanceof TLResource) || (newEntity instanceof TLResource)) {
			throw new IllegalArgumentException("Use 'compareResources()' to compare two OTM resource versions.");
		}
		EntityComparator comparator = new EntityComparator( compareOptions, null );
		
		comparator.addNamespaceMapping( oldEntity.getNamespace(), newEntity.getNamespace() );
		return comparator.compareEntities(
				new EntityComparisonFacade( oldEntity ), new EntityComparisonFacade( newEntity ) );
	}
	
	/**
	 * Compares two versions of the same OTM entity and writes a formatted HTML report of the
	 * results to the output stream provided.
	 * 
	 * @param oldEntity  the old entity version
	 * @param newEntity  the new entity version
	 * @param out  the output stream to which the formatted report will be written
	 * @throws IOException  thrown if an error occurs during report generation
	 */
	public void compareEntities(NamedEntity oldEntity, NamedEntity newEntity, OutputStream out)
			throws IOException {
		generateReport( compareEntities( oldEntity, newEntity), ENTITY_DIFF_TEMPLATE, out );
	}
	
	/**
	 * Compares two versions of the same OTM resource.
	 * 
	 * @param oldResource  the old resource version
	 * @param newResource  the new resource version
	 * @return ResourceChangeSet
	 */
	public ResourceChangeSet compareResources(TLResource oldResource, TLResource newResource) {
		ResourceComparator comparator = new ResourceComparator( compareOptions, null );
		
		comparator.addNamespaceMapping( oldResource.getNamespace(), newResource.getNamespace() );
		return comparator.compareResources( oldResource, newResource );
	}
	
	/**
	 * Compares two versions of the same OTM entity and writes a formatted HTML report of the
	 * results to the output stream provided.
	 * 
	 * @param oldEntity  the old entity version
	 * @param newEntity  the new entity version
	 * @param out  the output stream to which the formatted report will be written
	 * @throws IOException  thrown if an error occurs during report generation
	 */
	public void compareResources(TLResource oldResource, TLResource newResource, OutputStream out)
			throws IOException {
		generateReport( compareResources( oldResource, newResource), RESOURCE_DIFF_TEMPLATE, out );
	}
	
	/**
	 * Generates a formatted HTML report using a velocity template and the OTM-Diff
	 * results provided.
	 * 
	 * @param changeSet  the results of an OTM-Diff comparison
	 * @param templateName  the name of the velocity template to use
	 * @param out  the output stream to which the formatted report will be written
	 * @throws IOException  thrown if an error occurs during report generation
	 */
	private void generateReport(Object changeSet, String templateName, OutputStream out)
			throws IOException {
		Map<String,Object> context = new HashMap<>();
		
		context.put( "changeSet", changeSet );
		context.put( "formatter", new DisplayFormatter() );
		context.put( "TEMPLATE_FOLDER", TEMPLATE_FOLDER );
		
		try (Writer writer = new OutputStreamWriter( out )) {
			velocityEngine.mergeTemplate( templateName, "UTF-8", new VelocityContext( context ), writer );
			
		} catch (Exception e) {
			throw new IOException("Error during report generation.", e);
		}
	}
	
	/**
	 * Initializes and configures the default instance of the Velocity template
	 * processing engine.
	 */
	static {
		try {
			InputStream is = ModelComparator.class.getResourceAsStream ( VELOCITY_CONFIG_FILE );
			Properties velocityConfig = new Properties();
			
			velocityConfig.load( is );
			velocityEngine = new VelocityEngine( velocityConfig );
			
		} catch (Exception t) {
			throw new ExceptionInInitializerError( t );
		}
	}
	
}
