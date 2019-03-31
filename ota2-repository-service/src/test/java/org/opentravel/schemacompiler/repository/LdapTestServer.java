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

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldif.LDIFReader;

import java.io.File;

/**
 * Provides a pre-populated embedded LDAP server used for testing the authentication protocols of the OTA2.0 repository.
 */
public class LdapTestServer {

    private InMemoryDirectoryServer directoryServer;

    /**
     * Constructor that specifies the configuration of the embedded LDAP server to be used for testing.
     * 
     * @param port the port number where the LDAP server will listen for requests
     * @param ldifFilePath the path of the LDIF file to use when populating the directory (relative to
     *        /src/test/resources)
     * @throws Exception thrown if the directory server cannot be created or configured properly
     */
    public LdapTestServer(int port, String ldifFilePath) throws Exception {
        File testResourcesFolder = new File( System.getProperty( "user.dir" ), "/src/test/resources" );
        File ldifFile = new File( testResourcesFolder, ldifFilePath );

        try (LDIFReader reader = new LDIFReader( ldifFile )) {
            InMemoryDirectoryServerConfig serverConfig = new InMemoryDirectoryServerConfig( "dc=opentravel,dc=org" );
            InMemoryListenerConfig listenerConfig = InMemoryListenerConfig.createLDAPConfig( "ldapListener", port );

            serverConfig.addAdditionalBindCredentials( "cn=Manager,dc=opentravel,dc=org", "password" );
            serverConfig.setListenerConfigs( listenerConfig );
            directoryServer = new InMemoryDirectoryServer( serverConfig );
            directoryServer.importFromLDIF( true, reader );
        }
    }

    /**
     * Directs the LDAP server to start listening for requests.
     * 
     * @throws Exception thrown if the directory server cannot be started
     */
    public void start() throws Exception {
        directoryServer.startListening();
    }

    /**
     * Shuts down the listening services of the LDAP server and closes all active connections.
     * 
     * @throws Exception thrown if the directory server cannot be shut down
     */
    public void stop() throws Exception {
        directoryServer.shutDown( true );
    }

}
