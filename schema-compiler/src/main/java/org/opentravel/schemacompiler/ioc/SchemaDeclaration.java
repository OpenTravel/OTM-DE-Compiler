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

package org.opentravel.schemacompiler.ioc;

import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Configuration element used to declare the namespace, file location, and other key fields for XML schemas that are
 * used by the compiler application.
 * 
 * @author S. Livezey
 */
public class SchemaDeclaration {

    private static final String CLASSPATH_LOCATION_PREFIX = "classpath:";

    private String namespace;
    private String name;
    private String defaultPrefix;
    private Map<String,String> locations = new HashMap<>();
    private List<String> dependencies;

    /**
     * User-defined libraries will automatically receive import statements for all built-in libraries (if they are not
     * already defined)
     */
    private boolean importByDefault;

    /**
     * If the 'location' field is not null (or empty), this method will locate the file containing the schema's content
     * and return an input stream that can be used to read it. It is the responsibility of the caller to close the
     * stream that is returned.
     * 
     * @return InputStream
     * @throws IOException thrown if the schema's file location is null/empty or the file cannot be found
     * @deprecated use {@link #getContent(String)} instead
     */
    @Deprecated
    public InputStream getContent() throws IOException {
        return getContent( CodeGeneratorFactory.XSD_TARGET_FORMAT );
    }

    /**
     * If the location of a schema with the specified format is not null (or empty), this method will locate the file
     * containing the schema's content and return an input stream that can be used to read it. It is the responsibility
     * of the caller to close the stream that is returned.
     * 
     * @param fileFormat the file format of the schema whose content stream should be returned
     * @return InputStream
     * @throws IOException thrown if the schema's file location is null/empty or the file cannot be found
     */
    public InputStream getContent(String fileFormat) throws IOException {
        String location = getLocation( fileFormat );
        InputStream contentStream = null;

        if ((location == null) || (location.length() == 0)) {
            throw new FileNotFoundException( "No schema location found for namespace declaration: " + namespace );

        } else if (location.startsWith( CLASSPATH_LOCATION_PREFIX )) {
            String classpathLocation = location.substring( CLASSPATH_LOCATION_PREFIX.length() );

            contentStream = CompilerExtensionRegistry.loadResource( classpathLocation );

        } else {
            File schemaFile = new File( location );

            if (schemaFile.exists()) {
                contentStream = new FileInputStream( schemaFile );
            }
        }
        if (contentStream == null) {
            throw new FileNotFoundException( "Schema declaration content not found at location: " + location );
        }
        return contentStream;
    }

    /**
     * Returns the name of the XSD schema file (computed as the last element of the schema's location path).
     * 
     * @return String
     * @deprecated use {@link #getFilename(String)} instead
     */
    @Deprecated
    public String getFilename() {
        return getFilename( CodeGeneratorFactory.XSD_TARGET_FORMAT );
    }

    /**
     * Returns the name of the schema file with the specified file format (computed as the last element in the schema's
     * location).
     * 
     * @param fileFormat the file format of the schema whose filename should be returned
     * @return String
     */
    public String getFilename(String fileFormat) {
        String location = getLocation( fileFormat );
        String filename;

        if ((location != null) && (location.length() > 0) && !location.endsWith( "/" )) {
            int slashIdx = location.lastIndexOf( '/' );

            if (slashIdx == 0) {
                filename = location;
            } else {
                filename = location.substring( slashIdx + 1 );
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
     * @param namespace the namespace value to assign
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
     * @param name the name value to assign
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
     * @param prefix the default prefix value to assign
     */
    public void setDefaultPrefix(String prefix) {
        this.defaultPrefix = prefix;
    }

    /**
     * Returns the classpath or file location of the schema (XSD file format).
     * 
     * @return String
     * @deprecated use {@link #getLocation(String)} instead
     */
    @Deprecated
    public String getLocation() {
        return getLocation( CodeGeneratorFactory.XSD_TARGET_FORMAT );
    }

    /**
     * Returns the classpath or file location of the schema.
     * 
     * @param fileFormat the format identifier of the file
     * @return String
     */
    public String getLocation(String fileFormat) {
        return locations.get( fileFormat );
    }

    /**
     * Returns the list of all schema locations for this declaration.
     *
     * @return List&lt;SchemaLocation&gt;
     */
    public List<SchemaLocation> getLocations() {
        List<SchemaLocation> sLocs = new ArrayList<>();

        for (Entry<String,String> entry : locations.entrySet()) {
            sLocs.add( new SchemaLocation( entry.getKey(), entry.getValue() ) );
        }
        return Collections.unmodifiableList( sLocs );
    }

    /**
     * Assigns the list of all schema locations for this declaration.
     *
     * @param locations the list of schema locations to assign
     */
    public void setLocations(List<SchemaLocation> locations) {
        this.locations.clear();

        if (locations != null) {
            for (SchemaLocation sLoc : locations) {
                this.locations.put( sLoc.getFormat(), sLoc.getLocation() );
            }
        }
    }

    /**
     * Returns the list of bean ID's for any <code>SchemaDeclaration</code> instances that this declaration depends on.
     * 
     * @return List&lt;String&gt;
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
     * Assigns the list of bean ID's for any <code>SchemaDeclaration</code> instances that this declaration depends on.
     * 
     * @param dependencies the list of dependencies to assign
     */
    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * Return the import-by-default flag for schema.
     * 
     * @return boolean
     */
    public boolean isImportByDefault() {
        return importByDefault;
    }

    /**
     * Assigns the import-by-default flag for schema.
     * 
     * @param importByDefault the flag value to assign
     */
    public void setImportByDefault(boolean importByDefault) {
        this.importByDefault = importByDefault;
    }

    /**
     * Returns true if the given object is a <code>SchemaDeclaration</code> with the same XSD location value.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof SchemaDeclaration) {
            String thisLocation = getLocation( CodeGeneratorFactory.XSD_TARGET_FORMAT );
            String objLocation = ((SchemaDeclaration) obj).getLocation( CodeGeneratorFactory.XSD_TARGET_FORMAT );

            return (thisLocation == null) ? (objLocation == null) : thisLocation.equals( objLocation );
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        String location = getLocation( CodeGeneratorFactory.XSD_TARGET_FORMAT );
        return (location == null) ? 0 : location.hashCode();
    }

}
