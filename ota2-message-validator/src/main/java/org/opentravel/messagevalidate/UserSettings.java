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
package org.opentravel.messagevalidate;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Persists settings for the <code>ExampleHelper</code> application between sessions.
 */
public class UserSettings {
	
	private static final String USER_SETTINGS_FILE = "/.ota2/.mv-settings.properties";
	
	private static File settingsFile = new File( System.getProperty( "user.home" ), USER_SETTINGS_FILE );
	
	private Point windowPosition;
	private Dimension windowSize;
	private File projectFolder;
	private File messageFolder;
	
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
				String projectFolder = usProps.getProperty( "projectFolder", currentFolder );
				String messageFolder = usProps.getProperty( "messageFolder", currentFolder );
				
				settings = new UserSettings();
				settings.setWindowPosition( new Point( windowPositionX, windowPositionY ) );
				settings.setWindowSize( new Dimension( windowWidth, windowHeight ) );
				settings.setProjectFolder( new File( projectFolder ) );
				settings.setMessageFolder( new File( messageFolder ) );
				
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
			String pFolder = (projectFolder == null) ? currentFolder : projectFolder.getAbsolutePath();
			String mFolder = (messageFolder == null) ? currentFolder : messageFolder.getAbsolutePath();
			
			usProps.put( "windowPositionX", windowPosition.x + "" );
			usProps.put( "windowPositionY", windowPosition.y + "" );
			usProps.put( "windowWidth", windowSize.width + "" );
			usProps.put( "windowHeight", windowSize.height + "" );
			usProps.put( "projectFolder", pFolder );
			usProps.put( "messageFolder", mFolder );
			
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
		settings.setProjectFolder( new File( userHomeDirectory ) );
		settings.setMessageFolder( new File( userHomeDirectory ) );
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
	 * Returns the folder location of the OTM project file.
	 *
	 * @return File
	 */
	public File getProjectFolder() {
		return projectFolder;
	}

	/**
	 * Assigns the folder location of the OTM project file.
	 *
	 * @param projectFolder  the folder location to assign
	 */
	public void setProjectFolder(File projectFolder) {
		this.projectFolder = projectFolder;
	}

	/**
	 * Returns the folder location of the XML or JSON message
	 * to be validated.
	 *
	 * @return File
	 */
	public File getMessageFolder() {
		return messageFolder;
	}

	/**
	 * Assigns the folder location of the XML or JSON message
	 * to be validated.
	 *
	 * @param messageFolder  the folder location to assign
	 */
	public void setMessageFolder(File messageFolder) {
		this.messageFolder = messageFolder;
	}

}
