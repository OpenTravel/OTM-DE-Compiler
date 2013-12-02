/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.repository;

import java.io.File;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.sabre.schemacompiler.security.AuthenticationProvider;
import com.sabre.schemacompiler.security.RepositorySecurityManager;

/**
 * Handles the creation of key repository components using a Spring application context file.
 * 
 * @author S. Livezey
 */
public class RepositoryComponentFactory {
	
	public static final String SERVICE_CONFIGURATION_FILENAME = "ota2-repository-config.xml";
	
	private static final String REPOSITORY_LOCATION_KEY     = "repositoryLocation";
	private static final String SEARCH_INDEX_LOCATION_KEY   = "searchIndexLocation";
	private static final String REPOSITORY_MANAGER_KEY      = "repositoryManager";
	private static final String SECURITY_MANAGER_KEY        = "securityManager";
	private static final String AUTHENTICATION_PROVIDER_KEY = "authenticationProvider";
	private static final String DEVELOPMENT_REPOSITORY_KEY  = "developmentRepository";
	
	private static RepositoryComponentFactory defaultInstance;
	private static final Object defaultInstanceLock = new Object();
	
	private ApplicationContext appContext;
	
	/**
	 * Constructor that specifies the Spring application context file that contains the service
	 * configuration settings.
	 * 
	 * @param serviceConfigurationFile  the location of the service configuration file
	 */
	public RepositoryComponentFactory(File serviceConfigurationFile) {
		this( serviceConfigurationFile.getAbsolutePath() );
	}
	
	/**
	 * Constructor that specifies the Spring application context file that contains the service
	 * configuration settings.
	 * 
	 * @param serviceConfigurationFile  the location of the service configuration file
	 */
	public RepositoryComponentFactory(String serviceConfigurationFile) {
		if (serviceConfigurationFile.startsWith("classpath:")) {
			appContext = new ClassPathXmlApplicationContext( serviceConfigurationFile );
			
		} else {
			// For non-windows file systems, we need to add the "file://" prefix to avoid
			// spring's default behavior that interprets the filename as a relative path
			if (!System.getProperty("os.name").toLowerCase().startsWith("windows")) {
				serviceConfigurationFile = "file://" + serviceConfigurationFile;
			}
			appContext = new FileSystemXmlApplicationContext( serviceConfigurationFile );
		}
	}
	
	/**
	 * Returns the factory that is configured using the default application context file.  The default
	 * context XML is identified by looking in the following locations:
	 * <ol>
	 *   <li>If running in a Tomcat container environment, the file location "<code>${catalina.base}/conf/ota2-repository-config.xml</code>"
	 *   	is checked first</li>
	 *   <li>Next, the location identified by the system property "<code>ota2.repository.config</code>" is checked</li>
	 *   <li>If neither of the previous locations contains a valid application context file, the user's current working
	 *   	directory is checked for a file named "<code>ota2-repository-config.xml</code>"</li>
	 * </ol>
	 * @return
	 */
	public static RepositoryComponentFactory getDefault() {
		synchronized (defaultInstanceLock) {
			if (defaultInstance == null) {
				defaultInstance = new RepositoryComponentFactory( findConfigurationFile() );
			}
			return defaultInstance;
		}
	}
	
	/**
	 * Resets the default singleton instance of this factory, allowing the JVM to reconfigure
	 * the location of the configuration file.
	 * 
	 * NOTE: This method is only intended for testing purposes and may produce unpredictable results
	 * in a production environment.
	 */
	public static void resetDefault() {
		synchronized (defaultInstanceLock) {
			defaultInstance = null;
		}
	}
	
	/**
	 * Returns the root folder location of the OTA2.0 repository as defined in the service
	 * configuration file.
	 * 
	 * @return File
	 */
	public File getRepositoryLocation() {
		return (File) appContext.getBean( REPOSITORY_LOCATION_KEY );
	}
	
	/**
	 * Returns the root folder location of the Lucene index for the OTA2.0 repository.
	 * 
	 * @return File
	 */
	public File getSearchIndexLocation() {
		return (File) appContext.getBean( SEARCH_INDEX_LOCATION_KEY );
	}
	
	/**
	 * Returns the <code>RepositoryManager</code> as defined in the service configuration file.
	 * 
	 * @return RepositoryManager
	 */
	public RepositoryManager getRepositoryManager() {
		return (RepositoryManager) appContext.getBean( REPOSITORY_MANAGER_KEY );
	}
	
	/**
	 * Returns the <code>RepositorySecurityManager</code> as defined in the service configuration file.
	 * 
	 * @return RepositorySecurityManager
	 */
	public RepositorySecurityManager getSecurityManager() {
		return (RepositorySecurityManager) appContext.getBean( SECURITY_MANAGER_KEY );
	}
	
	/**
	 * Returns the <code>AuthenticationProvider</code> as defined in the service configuration file.
	 * 
	 * @return AuthenticationProvider
	 */
	public AuthenticationProvider getAuthenticationProvider() {
		return (AuthenticationProvider) appContext.getBean( AUTHENTICATION_PROVIDER_KEY );
	}
	
	/**
	 * Returns true if the managed repository has been designated as a development instance.
	 * 
	 * @return boolean
	 */
	public boolean isDevelopmentRepository() {
		return appContext.containsBean( DEVELOPMENT_REPOSITORY_KEY )
				? (Boolean) appContext.getBean( DEVELOPMENT_REPOSITORY_KEY ) : false;
	}
	
	/**
	 * Returns the location of the repository configuration file.
	 * 
	 * @return File
	 */
	private static File findConfigurationFile() {
		File configFile = null;
		
		if (System.getProperties().containsKey("catalina.base")) {
			configFile = new File(System.getProperty("catalina.base"), "/conf/" + SERVICE_CONFIGURATION_FILENAME);
			if (!configFile.exists()) configFile = null;
		}
		if ((configFile == null) && System.getProperties().containsKey("ota2.repository.config")) {
			configFile = new File(System.getProperty("ota2.repository.config"));
			if (!configFile.exists()) configFile = null;
		}
		if (configFile == null) {
			configFile = new File(System.getProperty("user.dir"), SERVICE_CONFIGURATION_FILENAME);
		}
		return configFile;
	}
	
}
