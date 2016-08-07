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
package org.opentravel.diffutil;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.opentravel.schemacompiler.diff.ModelCompareOptions;

/**
 * Persists settings for the <code>ExampleHelper</code> application between sessions.
 */
public class UserSettings {
	
	private static final String USER_SETTINGS_FILE = "/.ota2/.du-settings.properties";
	
	private static File settingsFile = new File( System.getProperty( "user.home" ), USER_SETTINGS_FILE );
	
	private Point windowPosition;
	private Dimension windowSize;
	private File oldProjectFolder;
	private File newProjectFolder;
	private File oldLibraryFolder;
	private File newLibraryFolder;
	private File reportFolder;
	private ModelCompareOptions compareOptions = new ModelCompareOptions();
	
	/**
	 * Returns the user settings from the prior session.  If no prior settings exist,
	 * default settings are returned.
	 * 
	 * @return UserSettings
	 */
	public static UserSettings load() {
		UserSettings settings;
		
		if (!settingsFile.exists()) {
			settings = getDefaultSettings();
			
		} else {
			try (InputStream is = new FileInputStream( settingsFile )) {
				String currentFolder = System.getProperty( "user.dir" );
				Properties usProps = new Properties();
				usProps.load( is );
				
				int windowPositionX = Integer.parseInt( usProps.getProperty( "windowPositionX" ) );
				int windowPositionY = Integer.parseInt( usProps.getProperty( "windowPositionY" ) );
				int windowWidth = Integer.parseInt( usProps.getProperty( "windowWidth" ) );
				int windowHeight = Integer.parseInt( usProps.getProperty( "windowHeight" ) );
				String opFolder = usProps.getProperty( "oldProjectFolder", currentFolder );
				String npFolder = usProps.getProperty( "newProjectFolder", currentFolder );
				String olFolder = usProps.getProperty( "oldLibraryFolder", currentFolder );
				String nlFolder = usProps.getProperty( "newLibraryFolder", currentFolder );
				String reportFolder = usProps.getProperty( "reportFolder", currentFolder );
				
				settings = new UserSettings();
				settings.setWindowPosition( new Point( windowPositionX, windowPositionY ) );
				settings.setWindowSize( new Dimension( windowWidth, windowHeight ) );
				settings.setOldProjectFolder( new File( opFolder ) );
				settings.setNewProjectFolder( new File( npFolder ) );
				settings.setOldLibraryFolder( new File( olFolder ) );
				settings.setNewLibraryFolder( new File( nlFolder ) );
				settings.setReportFolder( new File( reportFolder ) );
				settings.compareOptions.loadOptions( usProps );
				
			} catch(Throwable t) {
				t.printStackTrace( System.out );
				System.out.println("Error loading settings from prior session (using defaults).");
				settings = getDefaultSettings();
			}
			
			
		}
		return settings;
	}
	
	/**
	 * Saves the settings in the user's home directory.
	 */
	public void save() {
		if (!settingsFile.getParentFile().exists()) {
			settingsFile.getParentFile().mkdirs();
		}
		try (OutputStream out = new FileOutputStream( settingsFile )) {
			UserSettings defaultValues = getDefaultSettings();
			String currentFolder = System.getProperty( "user.dir" );
			Properties usProps = new Properties();
			Point windowPosition = (this.windowPosition == null) ?
					defaultValues.getWindowPosition() : this.windowPosition;
			Dimension windowSize = (this.windowSize == null) ?
					defaultValues.getWindowSize() : this.windowSize;
			String opFolder = (oldProjectFolder == null) ? currentFolder : oldProjectFolder.getAbsolutePath();
			String npFolder = (newProjectFolder == null) ? currentFolder : newProjectFolder.getAbsolutePath();
			String olFolder = (oldLibraryFolder == null) ? currentFolder : oldLibraryFolder.getAbsolutePath();
			String nlFolder = (newLibraryFolder == null) ? currentFolder : newLibraryFolder.getAbsolutePath();
			String rptFolder = (reportFolder == null) ? currentFolder : reportFolder.getAbsolutePath();
			
			usProps.put( "windowPositionX", windowPosition.x + "" );
			usProps.put( "windowPositionY", windowPosition.y + "" );
			usProps.put( "windowWidth", windowSize.width + "" );
			usProps.put( "windowHeight", windowSize.height + "" );
			usProps.put( "oldProjectFolder", opFolder );
			usProps.put( "newProjectFolder", npFolder );
			usProps.put( "oldLibraryFolder", olFolder );
			usProps.put( "newLibraryFolder", nlFolder );
			usProps.put( "reportFolder", rptFolder );
			compareOptions.saveOptions( usProps );
			
			usProps.store( out, null );
			
		} catch(IOException e) {
			System.out.println("Error saving user settings...");
			e.printStackTrace( System.out );
		}
	}
	
	/**
	 * Returns the default user settings.
	 * 
	 * @return UserSettings
	 */
	public static UserSettings getDefaultSettings() {
		String userHomeDirectory = System.getProperty( "user.home" );
		UserSettings settings = new UserSettings();
		
		settings.setWindowPosition( new Point( 0, 0 ) );
		settings.setWindowSize( new Dimension( 800, 600 ) );
		settings.setOldProjectFolder( new File( userHomeDirectory ) );
		settings.setNewProjectFolder( new File( userHomeDirectory ) );
		settings.setOldLibraryFolder( new File( userHomeDirectory ) );
		settings.setNewLibraryFolder( new File( userHomeDirectory ) );
		settings.compareOptions = new ModelCompareOptions();
		return settings;
	}

	/**
	 * Returns the location of the application window.
	 *
	 * @return Point
	 */
	public Point getWindowPosition() {
		return windowPosition;
	}

	/**
	 * Assigns the location of the application window.
	 *
	 * @param windowPosition  the window position to assign
	 */
	public void setWindowPosition(Point windowPosition) {
		this.windowPosition = windowPosition;
	}

	/**
	 * Returns the size of the application window.
	 *
	 * @return Dimension
	 */
	public Dimension getWindowSize() {
		return windowSize;
	}

	/**
	 * Assigns the size of the application window.
	 *
	 * @param windowSize  the window size to assign
	 */
	public void setWindowSize(Dimension windowSize) {
		this.windowSize = windowSize;
	}

	/**
	 * Returns the location of the old project folder.
	 *
	 * @return File
	 */
	public File getOldProjectFolder() {
		return oldProjectFolder;
	}

	/**
	 * Assigns the location of the old project folder.
	 *
	 * @param oldProjectFolder  the folder location to assign
	 */
	public void setOldProjectFolder(File oldProjectFolder) {
		this.oldProjectFolder = oldProjectFolder;
	}

	/**
	 * Returns the location of the new project folder.
	 *
	 * @return File
	 */
	public File getNewProjectFolder() {
		return newProjectFolder;
	}

	/**
	 * Assigns the location of the new project folder.
	 *
	 * @param newProjectFolder  the folder location to assign
	 */
	public void setNewProjectFolder(File newProjectFolder) {
		this.newProjectFolder = newProjectFolder;
	}

	/**
	 * Returns the location of the old library folder.
	 *
	 * @return File
	 */
	public File getOldLibraryFolder() {
		return oldLibraryFolder;
	}

	/**
	 * Assigns the location of the old library folder.
	 *
	 * @param oldLibraryFolder  the folder location to assign
	 */
	public void setOldLibraryFolder(File oldLibraryFolder) {
		this.oldLibraryFolder = oldLibraryFolder;
	}

	/**
	 * Returns the location of the new library folder.
	 *
	 * @return File
	 */
	public File getNewLibraryFolder() {
		return newLibraryFolder;
	}

	/**
	 * Assigns the location of the new library folder.
	 *
	 * @param newLibraryFolder  the folder location to assign
	 */
	public void setNewLibraryFolder(File newLibraryFolder) {
		this.newLibraryFolder = newLibraryFolder;
	}

	/**
	 * Returns the location of the report folder.
	 *
	 * @return File
	 */
	public File getReportFolder() {
		return reportFolder;
	}

	/**
	 * Assigns the location of the report folder.
	 *
	 * @param reportFolder  the folder location to assign
	 */
	public void setReportFolder(File reportFolder) {
		this.reportFolder = reportFolder;
	}

	/**
	 * Returns the options that should be applied when comparing two OTM models,
	 * libraries, or entities.
	 * 
	 * @return ModelCompareOptions
	 */
	public ModelCompareOptions getCompareOptions() {
		return compareOptions;
	}
	
}
