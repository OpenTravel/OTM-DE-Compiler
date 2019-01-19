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

import java.util.Properties;

/**
 * Used to specify the options that may be applied during a model comparison.
 */
public class ModelCompareOptions {
	
	private static ModelCompareOptions defaultOptions;
	
	private boolean suppressFieldVersionChanges;
	private boolean suppressLibraryPropertyChanges;
	private boolean suppressDocumentationChanges;
	
	/**
	 * Default constructor.
	 */
	public ModelCompareOptions() {
		configureDefaultOptions( this );
	}
	
	/**
	 * Returns the comparison options that should be applied if no other ones are specified.
	 * 
	 * @return ModelCompareOptions
	 */
	public static ModelCompareOptions getDefaultOptions() {
		return defaultOptions;
	}
	
	/**
	 * Returns the flag indicating whether field-level version changes will be supressed from
	 * the generation diff reports.  During major version upgrades, this can have a large effect
	 * on reducing the "noise" due to nothing but version updates at the field level.
	 *
	 * @return boolean
	 */
	public boolean isSuppressFieldVersionChanges() {
		return suppressFieldVersionChanges;
	}
	
	/**
	 * Assigns the flag indicating whether field-level version changes will be supressed from
	 * the generation diff reports.  During major version upgrades, this can have a large effect
	 * on reducing the "noise" due to nothing but version updates at the field level.
	 *
	 * @param suppressFieldVersionChanges  the flag value to assign
	 */
	public void setSuppressFieldVersionChanges(boolean suppressFieldVersionChanges) {
		this.suppressFieldVersionChanges = suppressFieldVersionChanges;
	}
	
	/**
	 * Returns the flag indicating whether library-level NS/Prefix/Version/Status changes will be supressed from
	 * the generation diff reports.  During major version upgrades, this can have a large effect
	 * on reducing the "noise" due to nothing but version updates at the field level.
	 *
	 * @return boolean
	 */
	public boolean isSuppressLibraryPropertyChanges() {
		return suppressLibraryPropertyChanges;
	}
	
	/**
	 * Assigns the flag indicating whether library-level NS/Prefix/Version/Status changes will be supressed from
	 * the generation diff reports.  During major version upgrades, this can have a large effect
	 * on reducing the "noise" due to nothing but version updates at the field level.
	 *
	 * @param suppressLibraryPropertyChanges  the flag value to assign
	 */

	public void setSuppressLibraryPropertyChanges(boolean suppressLibPropertyChanges) {
		this.suppressLibraryPropertyChanges = suppressLibPropertyChanges;
	}

	/**
	 * Returns the flag indicating whether library/Entity/Field-level documentation changes will be supressed from
	 * the generation diff reports.  During major version upgrades, this can have a large effect
	 * on reducing the "noise" due to nothing but version updates at the field level.
	 *
	 * @return boolean
	 */
	
	public boolean isSuppressDocumentationChanges() {
		return suppressDocumentationChanges;
	}
	
	/**
	 * Assigns the flag indicating whether library/Entity/Field-level documentation changes will be supressed from
	 * the generation diff reports.  During major version upgrades, this can have a large effect
	 * on reducing the "noise" due to nothing but version updates at the field level.
	 *
	 * @param suppressDocumentationChanges  the flag value to assign
	 */

	public void setSuppressDocumentationChanges(boolean suppressDocumentChanges) {
		this.suppressDocumentationChanges = suppressDocumentChanges;
	}
	
	/**
	 * Loads the settings for this options instance from the given properties.
	 * 
	 * @param props  the properties from which to load the comparison options
	 */
	public void loadOptions(Properties props) {
		String suppressFieldVersionChangesStr = props.getProperty(
				"suppressFieldVersionChanges", defaultOptions.isSuppressFieldVersionChanges() + "" );
		String suppressLibraryPropertyChangesStr = props.getProperty(
				"suppressLibraryPropertyChanges", defaultOptions.isSuppressLibraryPropertyChanges() + "" );
		String suppressDocumentationChangesStr = props.getProperty(
				"suppressDocumentationChanges", defaultOptions.isSuppressDocumentationChanges() + "" );
		
		this.suppressFieldVersionChanges = Boolean.parseBoolean( suppressFieldVersionChangesStr );
		this.suppressLibraryPropertyChanges = Boolean.parseBoolean( suppressLibraryPropertyChangesStr );
		this.suppressDocumentationChanges = Boolean.parseBoolean( suppressDocumentationChangesStr );
	}
	
	/**
	 * Saves the current settings of this options instance to the given properties.
	 * 
	 * @param props  the properties to which the comparison options should be saved
	 */
	public void saveOptions(Properties props) {
		props.put( "suppressFieldVersionChanges", suppressFieldVersionChanges + "" );
		props.put( "suppressLibraryPropertyChanges", suppressLibraryPropertyChanges + "" );
		props.put( "suppressDocumentationChanges", suppressDocumentationChanges + "" );
	}
	
	/**
	 * Assigns the default option settings for the given instance.
	 * 
	 * @param options  the options instance to be configured
	 */
	private static void configureDefaultOptions(ModelCompareOptions options) {
		options.setSuppressFieldVersionChanges( false );
		options.setSuppressLibraryPropertyChanges( false );
		options.setSuppressDocumentationChanges( false );
	}
	
	/**
	 * Initializes the settings for the default comparison options.
	 */
	static {
		try {
			defaultOptions = new ModelCompareOptions();
			configureDefaultOptions( defaultOptions );
			
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
}
