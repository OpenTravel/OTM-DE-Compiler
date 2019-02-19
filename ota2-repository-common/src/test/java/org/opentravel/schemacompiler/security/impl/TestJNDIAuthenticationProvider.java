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
package org.opentravel.schemacompiler.security.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.RepositorySecurityException;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldif.LDIFReader;

/**
 * Unit test cases for the <code>JNDIAuthenticationProvider</code> class.
 */
public class TestJNDIAuthenticationProvider {
    
    private static InMemoryDirectoryServer directoryServer;
    private static JNDIAuthenticationProvider authProvider;
    private static TemporaryFolder repositoryFolder = new TemporaryFolder();
    
    @BeforeClass
    public static void setup() throws Exception {
        File testResourcesFolder = new File( System.getProperty( "user.dir" ), "/src/test/resources" );
        File ldifFile = new File( testResourcesFolder, "/ldif-snapshots/user-lookup.ldif" );
        
        try (LDIFReader reader = new LDIFReader( ldifFile )) {
            InMemoryDirectoryServerConfig serverConfig = new InMemoryDirectoryServerConfig( "dc=opentravel,dc=org" );
            InMemoryListenerConfig listenerConfig = InMemoryListenerConfig.createLDAPConfig( "ldapListener", 1390 );
            
            serverConfig.addAdditionalBindCredentials( "cn=Manager,dc=opentravel,dc=org", "password" );
            serverConfig.setListenerConfigs( listenerConfig );
            directoryServer = new InMemoryDirectoryServer( serverConfig );
            directoryServer.importFromLDIF( true, reader );
            directoryServer.startListening();
        }
        
        repositoryFolder.create();
        authProvider = new JNDIAuthenticationProvider();
        authProvider.setContextFactory( "com.sun.jndi.ldap.LdapCtxFactory" );
        authProvider.setRepositoryManager( new RepositoryManager( repositoryFolder.getRoot() ) );
        authProvider.setConnectionUrl( "ldap://localhost:1389/dc=opentravel,dc=org" );
        authProvider.setAlternateUrl( "ldap://localhost:1390/dc=opentravel,dc=org" );
        authProvider.setConnectionProtocol( null );
        authProvider.setConnectionTimeout( 10000 );
        authProvider.setSecurityAuthentication( "simple" );
        authProvider.setConnectionPrincipal( "cn=Manager,dc=opentravel,dc=org" );
        authProvider.setConnectionPassword( "password" );
        authProvider.setUserSearchPatterns( "(&amp;(objectClass=person)(uid={0})):(uid={0})" );
        authProvider.setUserSearchBase( "ou=Users" );
        authProvider.setSearchUserSubtree( true );
        authProvider.setUserSearchTimeout( 10000 );
        authProvider.setUserPattern( "cn={0},ou=Users" );
        authProvider.setDigestAlgorithm( "SHA-1" );
        authProvider.setDigestEncoding( "UTF-8" );
        authProvider.setAuthenticationCacheTimeout( 1000 );
        authProvider.setUserIdAttribute( "uid" );
        authProvider.setUserPasswordAttribute( "userPassword" );
        authProvider.setUserLastNameAttribute( "sn" );
        authProvider.setUserFirstNameAttribute( "givenName" );
        authProvider.setUserFullNameAttribute( "cn" );
        authProvider.setUserEmailAttribute( "mail" );
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        directoryServer.shutDown( true );
        repositoryFolder.delete();
    }
    
    @Test
    public void testDefaultConfiguration() throws Exception {
        JNDIAuthenticationProvider ap = new JNDIAuthenticationProvider();
        
        assertNull( ap.getConnectionUrl() );
        assertNull( ap.getAlternateUrl() );
        assertNull( ap.getConnectionProtocol() );
        assertNull( ap.getSecurityAuthentication() );
        assertNull( ap.getConnectionPrincipal() );
        assertNull( ap.getConnectionPassword() );
        assertNull( ap.getDigestAlgorithm() );
        assertNull( ap.getDigestEncoding() );
        assertNull( ap.getUserPattern() );
        assertNull( ap.getUserSearchPatterns() );
        
        assertEquals( "com.sun.jndi.ldap.LdapCtxFactory", ap.getContextFactory() );
        assertEquals( 5000, ap.getConnectionTimeout() );
        assertEquals( "", ap.getUserSearchBase() );
        assertEquals( false, ap.isSearchUserSubtree() );
        assertEquals( 5000, ap.getUserSearchTimeout() );
        assertEquals( "uid", ap.getUserIdAttribute() );
        assertEquals( "sn", ap.getUserLastNameAttribute() );
        assertEquals( "givenName", ap.getUserFirstNameAttribute() );
        assertEquals( "cn", ap.getUserFullNameAttribute() );
        assertEquals( "mail", ap.getUserEmailAttribute() );
        assertEquals( "userPassword", ap.getUserPasswordAttribute() );
        assertEquals( "ignore", ap.getReferralStrategy() );
        assertEquals( 300000, ap.getAuthenticationCacheTimeout() );
    }
    
    @Test
    public void testUserSearchPatterns() throws Exception {
        assertEquals( 2, authProvider.getUserSearchPatterns().split( ":" ).length );
    }
    
    @Test( expected = UnsupportedOperationException.class )
    public void testSetUserPassword() throws Exception {
        authProvider.setUserPassword( "testuser", "password" );
    }
    
    @Test
    public void testAlternateConnection() throws Exception {
        assertTrue( authProvider.isValidUser( "testuser", "password" ) );
        assertTrue( authProvider.isValidUser( "testuser", "password" ) ); // retrieves from cached authentication
    }
    
    @Test( expected = RepositorySecurityException.class )
    public void testUserLookupMode_missingConnectionPrincipal() throws Exception {
        JNDIAuthenticationProvider ap = new JNDIAuthenticationProvider();
        
        ap.setConnectionUrl( "ldap://localhost:1390/dc=opentravel,dc=org" );
        
        ap.searchCandidateUsers( "Doe", 10 );
    }
    
    @Test( expected = RepositorySecurityException.class )
    public void testUserLookupMode_missingConnectionPassword() throws Exception {
        JNDIAuthenticationProvider ap = new JNDIAuthenticationProvider();
        
        ap.setConnectionUrl( "ldap://localhost:1390/dc=opentravel,dc=org" );
        ap.setConnectionPrincipal( "cn=Manager,dc=opentravel,dc=org" );
        
        ap.searchCandidateUsers( "Doe", 10 );
    }
    
    @Test( expected = RepositorySecurityException.class )
    public void testUserLookupMode_missingUserSearchBase() throws Exception {
        JNDIAuthenticationProvider ap = new JNDIAuthenticationProvider();
        
        ap.setConnectionUrl( "ldap://localhost:1390/dc=opentravel,dc=org" );
        ap.setConnectionPrincipal( "cn=Manager,dc=opentravel,dc=org" );
        ap.setConnectionPassword( "password" );
        ap.setUserSearchBase( null );
        
        ap.searchCandidateUsers( "Doe", 10 );
    }
    
    @Test( expected = RepositorySecurityException.class )
    public void testUserLookupMode_missingUserSearchPatterns() throws Exception {
        JNDIAuthenticationProvider ap = new JNDIAuthenticationProvider();
        
        ap.setConnectionUrl( "ldap://localhost:1390/dc=opentravel,dc=org" );
        ap.setConnectionPrincipal( "cn=Manager,dc=opentravel,dc=org" );
        ap.setConnectionPassword( "password" );
        authProvider.setUserSearchBase( "ou=Users" );
        
        ap.searchCandidateUsers( "Doe", 10 );
    }
    
    @Test( expected = RepositorySecurityException.class )
    public void testUserSearchMode_missingConnectionPrincipal() throws Exception {
        JNDIAuthenticationProvider ap = new JNDIAuthenticationProvider();
        
        ap.setUserPattern( "cn={0},ou=Users" );
        ap.setConnectionUrl( "ldap://localhost:1390/dc=opentravel,dc=org" );
        
        ap.searchCandidateUsers( "Doe", 10 );
    }
    
    @Test( expected = RepositorySecurityException.class )
    public void testUserSearchMode_missingConnectionPassword() throws Exception {
        JNDIAuthenticationProvider ap = new JNDIAuthenticationProvider();
        
        ap.setUserPattern( "cn={0},ou=Users" );
        ap.setConnectionUrl( "ldap://localhost:1390/dc=opentravel,dc=org" );
        ap.setConnectionPrincipal( "cn=Manager,dc=opentravel,dc=org" );
        
        ap.searchCandidateUsers( "Doe", 10 );
    }
    
    @Test( expected = RepositorySecurityException.class )
    public void testUserSearchMode_missingDigestAlgorithm() throws Exception {
        JNDIAuthenticationProvider ap = new JNDIAuthenticationProvider();
        
        ap.setUserPattern( "cn={0},ou=Users" );
        ap.setConnectionUrl( "ldap://localhost:1390/dc=opentravel,dc=org" );
        ap.setConnectionPrincipal( "cn=Manager,dc=opentravel,dc=org" );
        ap.setDigestAlgorithm( null );
        
        ap.searchCandidateUsers( "Doe", 10 );
    }
    
}
