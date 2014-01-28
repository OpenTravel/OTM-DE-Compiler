
package org.opentravel.schemacompiler.ioc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Configuration element used to declare the namespace, file location, and other key fields for
 * XML schemas that are used by the compiler application.
 * 
 * @author S. Livezey
 */
public class SchemaDeclaration {
	
	private static final String CLASSPATH_LOCATION_PREFIX = "classpath:";
	
	private String namespace;
	private String name;
	private String defaultPrefix;
	private String location;
	private List<String> dependencies;

    /**
     * User-defined libraries will automatically receive import statements for all built-in
     * libraries (if they are not already defined)
     */
    private boolean importByDefault;
	
	/**
	 * If the 'location' field is not null (or empty), this method will locate the file containing the
	 * schema's content and return an input stream that can be used to read it.  It is the responsibility
	 * of the caller to close the stream that is returned.
	 * 
	 * @return InputStream
	 * @throws IOException  thrown if the schema's file location is null/empty or the file cannot be found
	 */
	public InputStream getContent() throws IOException {
		InputStream contentStream = null;
		
		if ((location == null) || (location.length() == 0)) {
			throw new FileNotFoundException("No schema location found for namespace declaration: " + namespace);
			
		} else if (location.startsWith(CLASSPATH_LOCATION_PREFIX)) {
			String classpathLocation = location.substring(CLASSPATH_LOCATION_PREFIX.length());
			
			contentStream = CompilerExtensionRegistry.loadResource(classpathLocation);
			
		} else {
			File schemaFile = new File(location);
			
			if (schemaFile.exists()) {
				contentStream = new FileInputStream(schemaFile);
			}
		}
		if (contentStream == null) {
			throw new FileNotFoundException("Schema declaration content not found at location: " + location);
		}
		return contentStream;
	}
	
	/**
	 * Returns the name of the file (computed as the last element in the 'location' field).
	 * 
	 * @return String
	 */
	public String getFilename() {
		String filename;
		
		if ((location != null) && (location.length() > 0) && !location.endsWith("/")) {
			int slashIdx = location.lastIndexOf('/');
			
			if (slashIdx == 0) {
				filename = location;
			} else {
				filename = location.substring(slashIdx + 1);
			}
		} else {
			filename = null;
		}
		return filename;
	}
	
	/**
	 * Returns the namespace of the schema declaration.
	 *
	 * @return String
	 */
	public String getNamespace() {
		return namespace;
	}
	
	/**
	 * Assigns the namespace of the schema declaration.
	 *
	 * @param namespace  the namespace value to assign
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	/**
	 * Returns the name of the schema.
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Assigns the name of the schema.
	 *
	 * @param name  the name value to assign
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the default defaultPrefix to assign to the schema's namespace.
	 *
	 * @return String
	 */
	public String getDefaultPrefix() {
		return defaultPrefix;
	}
	
	/**
	 * Assigns the default defaultPrefix to assign to the schema's namespace.
	 *
	 * @param defaultPrefix  the defaultPrefix value to assign
	 */
	public void setDefaultPrefix(String prefix) {
		this.defaultPrefix = prefix;
	}
	
	/**
	 * Returns the classpath or file location of the schema.
	 *
	 * @return String
	 */
	public String getLocation() {
		return location;
	}
	
	/**
	 * Assigns the classpath or file location of the schema.
	 *
	 * @param location  the location value to assign
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	
	/**
	 * Returns the list of bean ID's for any <code>SchemaDeclaration</code> instances that
	 * this declaration depends on.
	 *
	 * @return List<String>
	 */
	public List<String> getDependencies() {
		List<String> dependencyList;
		
		if (dependencies == null) {
			dependencyList = Collections.emptyList();
		} else {
			dependencyList = dependencies;
		}
		return dependencyList;
	}

	/**
	 * Assigns the list of bean ID's for any <code>SchemaDeclaration</code> instances that
	 * this declaration depends on.
	 *
	 * @param dependencies  the list of dependencies to assign
	 */
	public void setDependencies(List<String> dependencies) {
		this.dependencies = dependencies;
	}

	/**
	 * Return the deprecated flag for schema.
	 * @return
	 */
    public boolean isImportByDefault() {
        return importByDefault;
    }

    /**
     * Assigns the deprecated flag for schema.
     *
     * @param importByDefault
     */
    public void setImportByDefault(boolean importByDefault) {
        this.importByDefault = importByDefault;
    }

    /**
	 * Returns true if the given object is a <code>SchemaDeclaration</code> with the same location value.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof SchemaDeclaration) {
			String thisLocation = this.location;
			String objLocation = ((SchemaDeclaration) obj).location;
			
			return (thisLocation == null) ? (objLocation == null) : thisLocation.equals(objLocation);
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return (location == null) ? 0 : location.hashCode();
	}

   
	
}
