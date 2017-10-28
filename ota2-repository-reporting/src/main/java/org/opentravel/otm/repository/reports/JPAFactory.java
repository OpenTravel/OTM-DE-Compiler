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
package org.opentravel.otm.repository.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.engine.spi.SessionImplementor;

/**
 * Used to create JPA entity manager factories based that may be configured
 * using settings from a locally-accessible properties file.
 */
public class JPAFactory {
	
	public static final String JPA_PERSISTENCE_UNIT = "org.opentravel.reports";
	
	private static Map<String,EntityManagerFactory> instanceMap = new HashMap<>();
	
	/**
	 * Returns the default entity manager factory that uses the default settings
	 * from the 'persistence.xml' file on the local classpath.
	 * 
	 * @return EntityManagerFactory
	 */
	public static EntityManagerFactory getFactory() {
		try {
			return getFactory( null );
			
		} catch (IOException e) {
			// Should never happen since we did not pass a configuration file
			throw new RuntimeException("Unexpected exception loading JPA configuration.", e);
		}
	}
	
	/**
	 * Returns the default entity manager factory that uses the default settings
	 * from the 'persistence.xml' file on the local classpath, plus overrides from
	 * the given properties file.
	 * 
	 * @param jpaConfigFile  the properties file with setting overrides for the JPA
	 *						 configuration (null for default settings)
	 * @return EntityManagerFactory
	 * @throws IOException  thrown if the specified JPA configuration does not exist or cannot be read from
	 */
	public static EntityManagerFactory getFactory(File jpaConfigFile) throws IOException {
		if ((jpaConfigFile != null) && (!jpaConfigFile.exists() || !jpaConfigFile.isFile())) {
			throw new FileNotFoundException("The specified JPA configuration file was not found: " + jpaConfigFile.getName());
		}
		String configFilename = ((jpaConfigFile == null) ? "DEFAULT_SETTINGS" : jpaConfigFile.getAbsolutePath()).intern();
		
		synchronized (configFilename) {
			EntityManagerFactory factory = instanceMap.get( configFilename );
			
			if ((factory == null) || !factory.isOpen()) {
				factory = Persistence.createEntityManagerFactory( JPA_PERSISTENCE_UNIT, loadProperties( jpaConfigFile ) );
			}
			return factory;
		}
	}
	
	/**
	 * Convenience method that returns the underlying database connection for the
	 * given entity manager.
	 * 
	 * @param entityManager  the entity manager for which to return the connection
	 * @return Connection
	 */
	public static Connection getConnection(EntityManager entityManager) {
		return entityManager.unwrap( SessionImplementor.class ).connection();
	}
	
	/**
	 * Loads the properties from the specified configuration file.
	 * 
	 * @param jpaConfigFile  the configuration file to load
	 * @return Map<String,String>
	 * @throws IOException
	 */
	private static Map<String,String> loadProperties(File jpaConfigFile) throws IOException {
		Map<String,String> configProps = null;
		
		if (jpaConfigFile != null) {
			Properties props = new Properties();
			
			try (InputStream is = new FileInputStream( jpaConfigFile )) {
				props.load( is );
			}
			configProps = new HashMap<>();
			
			for (Object propertyName : props.keySet()) {
				String propertyValue = props.getProperty( (String) propertyName );
				configProps.put( (String) propertyName, propertyValue );
			}
		}
		return configProps;
	}
	
}
