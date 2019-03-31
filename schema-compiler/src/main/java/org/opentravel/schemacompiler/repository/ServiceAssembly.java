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

package org.opentravel.schemacompiler.repository;

import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.Validatable;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A service assembly is composed of multiple releases, presumably one for each resource (API) version. Releases are
 * divided into two groups, a service provider group and a service consumer group.
 */
public class ServiceAssembly implements Validatable {

    private static VersionScheme versionScheme;

    private URL assemblyUrl;
    private String baseNamespace;
    private String name;
    private String version;
    private List<ServiceAssemblyItem> providerApis = new ArrayList<>();
    private List<ServiceAssemblyItem> consumerApis = new ArrayList<>();

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        String identity = "[ UNKNOWN ASSEMBLY ]";

        if (assemblyUrl != null) {
            identity = URLUtils.getUrlFilename( assemblyUrl );

        } else if (name != null) {
            identity = name;
        }
        return identity;
    }

    /**
     * Returns the URL location where this assembly's content is stored.
     *
     * @return URL
     */
    public URL getAssemblyUrl() {
        return assemblyUrl;
    }

    /**
     * Assigns the URL location where this assembly's content is stored.
     *
     * @param assemblyUrl the URL location to assign
     */
    public void setAssemblyUrl(URL assemblyUrl) {
        this.assemblyUrl = assemblyUrl;
    }

    /**
     * Returns the base namespace of the assembly.
     *
     * @return String
     */
    public String getBaseNamespace() {
        return baseNamespace;
    }

    /**
     * Assigns the base namespace of the assembly.
     *
     * @param baseNamespace the base namespace URI to assign
     */
    public void setBaseNamespace(String baseNamespace) {
        this.baseNamespace = baseNamespace;
    }

    /**
     * The full namespace of the assembly (including the version identifier suffix).
     * 
     * @return String
     */
    public String getNamespace() {
        return versionScheme.setVersionIdentifier( baseNamespace, version );
    }

    /**
     * Returns the name of the assembly.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Assigns the name of the assembly.
     *
     * @param name the name value to assign
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the version of the assembly.
     *
     * @return String
     */
    public String getVersion() {
        return version;
    }

    /**
     * Assigns the version of the assembly.
     *
     * @param version the version identifier value to assign
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the list of provider API releases for this assembly.
     *
     * @return List&lt;ServiceAssemblyItem&gt;
     */
    public List<ServiceAssemblyItem> getProviderApis() {
        return Collections.unmodifiableList( providerApis );
    }

    /**
     * Adds the given provider API release to this assembly.
     * 
     * @param assemblyItem the assembly item to add
     */
    public void addProviderApi(ServiceAssemblyItem assemblyItem) {
        if (assemblyItem != null) {
            providerApis.add( assemblyItem );
            assemblyItem.setOwner( this );
        }
    }

    /**
     * Removes the given provider API release to this assembly.
     * 
     * @param assemblyItem the assembly item to remove
     */
    public void removeProviderApi(ServiceAssemblyItem assemblyItem) {
        if (providerApis.contains( assemblyItem )) {
            providerApis.remove( assemblyItem );
            assemblyItem.setOwner( null );
        }
    }

    /**
     * Returns the list of consumer API releases for this assembly.
     *
     * @return List&lt;ServiceAssemblyItem&gt;
     */
    public List<ServiceAssemblyItem> getConsumerApis() {
        return Collections.unmodifiableList( consumerApis );
    }

    /**
     * Adds the given consumer API release to this assembly.
     * 
     * @param assemblyItem the assembly item to add
     */
    public void addConsumerApi(ServiceAssemblyItem assemblyItem) {
        if (assemblyItem != null) {
            consumerApis.add( assemblyItem );
            assemblyItem.setOwner( this );
        }
    }

    /**
     * Removes the given consumer API release to this assembly.
     * 
     * @param assemblyItem the assembly item to remove
     */
    public void removeConsumerApi(ServiceAssemblyItem assemblyItem) {
        if (consumerApis.contains( assemblyItem )) {
            consumerApis.remove( assemblyItem );
            assemblyItem.setOwner( null );
        }
    }

    /**
     * Returns the list of all API releases for this assembly.
     *
     * @return List&lt;ServiceAssemblyItem&gt;
     */
    public List<ServiceAssemblyItem> getAllApis() {
        List<ServiceAssemblyItem> allApis = new ArrayList<>();

        allApis.addAll( providerApis );
        allApis.addAll( consumerApis );
        return Collections.unmodifiableList( allApis );
    }


    /**
     * Initialize the default version scheme.
     */
    static {
        try {
            VersionSchemeFactory factory = VersionSchemeFactory.getInstance();
            versionScheme = factory.getVersionScheme( factory.getDefaultVersionScheme() );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
