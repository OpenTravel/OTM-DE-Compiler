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
package org.opentravel.schemacompiler.version;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.springframework.context.ApplicationContext;

/**
 * Factory used to obtain <code>VersionScheme</code> instances based on a version scheme identifier
 * string.
 * 
 * @author S. Livezey
 */
public class VersionSchemeFactory {

    private static final String FACTORY_NAME = "versionSchemeFactory";

    private Map<String, Class<?>> versionSchemeMappings = new HashMap<String, Class<?>>();
    private String defaultVersionScheme;

    /**
     * Private constructor.
     */
    private VersionSchemeFactory() {
    }

    /**
     * Returns the default factory instance.
     * 
     * @return VersionSchemeFactory
     */
    public static VersionSchemeFactory getInstance() {
        ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();
        VersionSchemeFactory factory = (VersionSchemeFactory) appContext.getBean(FACTORY_NAME);

        return factory;
    }

    /**
     * Examines the local classpath for the file that will identify the version scheme
     * implementation class for the requested version scheme identifier.
     * 
     * @param versionSchemeIdentifier
     *            the string that identifies the desired version scheme implementation
     * @return VersionScheme
     * @throws VersionSchemeException
     *             thrown if the a class cannot be identified for the specified version scheme
     *             identifier
     */
    @SuppressWarnings("unchecked")
    public VersionScheme getVersionScheme(String versionSchemeIdentifier)
            throws VersionSchemeException {
        Class<? extends VersionScheme> versionSchemeClass = (Class<? extends VersionScheme>) versionSchemeMappings
                .get(versionSchemeIdentifier);
        VersionScheme versionScheme = null;

        if (versionSchemeClass != null) {
            try {
                versionScheme = versionSchemeClass.newInstance();

            } catch (Throwable t) {
                throw new VersionSchemeException(
                        "Error instantiating version scheme implementation class: "
                                + versionSchemeClass.getName(), t);
            }
        } else {
            throw new VersionSchemeException("Unknown version scheme identifier: "
                    + versionSchemeIdentifier);
        }
        return versionScheme;
    }

    /**
     * Assigns the version scheme mappings for this factory.
     * 
     * @param mappings
     *            the version scheme mappings to assign
     */
    public void setVersionSchemeMappings(Properties mappings) {
        try {
            versionSchemeMappings.clear();

            for (Object key : mappings.keySet()) {
                String versionSchemeIdentitifer = (String) key;
                String versionSchemeClass = mappings.getProperty(versionSchemeIdentitifer);

                versionSchemeMappings.put(versionSchemeIdentitifer,
                        Class.forName(versionSchemeClass));
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Invalid version scheme mapping configuration.", e);
        }
    }

    /**
     * Returns the default version scheme identifier for the OTA2 schema compiler.
     * 
     * @return String
     */
    public String getDefaultVersionScheme() {
        return defaultVersionScheme;
    }

    /**
     * Assigns the default version scheme identifier for the OTA2 schema compiler.
     * 
     * @param defaultVersionScheme
     *            the default version scheme to assign
     */
    public void setDefaultVersionScheme(String defaultVersionScheme) {
        this.defaultVersionScheme = defaultVersionScheme;
    }

}
