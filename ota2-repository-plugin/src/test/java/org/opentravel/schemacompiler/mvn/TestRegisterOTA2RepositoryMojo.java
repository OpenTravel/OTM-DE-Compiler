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

package org.opentravel.schemacompiler.mvn;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.SilentLog;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.junit.Test;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;

import java.io.File;

/**
 * Verifies the functions of the <code>RegisterOTA2RepositoryMojo</code> build plugin.
 */
public class TestRegisterOTA2RepositoryMojo extends AbstractRepositoryMojoTest {

    @Test
    public void testAddRemoteRepository() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-9/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-repository-plugin", pomFile );
        RemoteRepository mockRepo = getMockRemoteRepository();

        // Register a previously-unknown repository
        repositoryManager = mock( RepositoryManager.class );
        when( repositoryManager.addRemoteRepository( mockRepo.getEndpointUrl() ) ).thenReturn( mockRepo );
        System.setProperty( RegisterOTA2RepositoryMojo.FORCE_UPDATE_SYSPROP, "false" );
        executeMojo( config );

        // Re-register the repository (mock out an error that should not be thrown)
        when( repositoryManager.getRepository( mockRepo.getId() ) ).thenReturn( mockRepo );
        when( repositoryManager.addRemoteRepository( mockRepo.getEndpointUrl() ) )
            .thenThrow( RepositoryException.class );
        executeMojo( config );

        // Execute again and force the update (configure for anonymous access this time)
        repositoryManager = mock( RepositoryManager.class );
        when( repositoryManager.getRepository( mockRepo.getId() ) ).thenReturn( mockRepo );
        when( repositoryManager.addRemoteRepository( mockRepo.getEndpointUrl() ) ).thenReturn( mockRepo );
        System.setProperty( RegisterOTA2RepositoryMojo.FORCE_UPDATE_SYSPROP, "true" );
        config.getChild( "userId" ).setValue( null );
        config.getChild( "userPassword" ).setValue( null );
        executeMojo( config );
    }

    @Test(expected = MojoExecutionException.class)
    public void testRepositoryIdMismatch() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-9/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-repository-plugin", pomFile );
        RemoteRepository mockRepo = getMockRemoteRepository();

        config.getChild( "repositoryId" ).setValue( "error-repository" );
        repositoryManager = mock( RepositoryManager.class );
        when( repositoryManager.addRemoteRepository( mockRepo.getEndpointUrl() ) ).thenReturn( mockRepo );
        executeMojo( config );
    }

    @Test(expected = MojoExecutionException.class)
    public void testRemoteRepository() throws Exception {
        File pomFile = new File( testProjectsFolder, "/test-project-9/pom.xml" );
        PlexusConfiguration config = rule.extractPluginConfiguration( "ota2-repository-plugin", pomFile );
        RemoteRepository mockRepo = getMockRemoteRepository();

        config.getChild( "repositoryId" ).setValue( "error-repository" );
        repositoryManager = mock( RepositoryManager.class );
        when( repositoryManager.addRemoteRepository( mockRepo.getEndpointUrl() ) )
            .thenThrow( RepositoryException.class );
        executeMojo( config );
    }

    protected void executeMojo(PlexusConfiguration config) throws Exception {
        RegisterOTA2RepositoryMojo mojo = new RegisterOTA2RepositoryMojo();

        mojo = (RegisterOTA2RepositoryMojo) rule.configureMojo( mojo, config );
        mojo.initRepositoryManager( repositoryManager );
        mojo.setLog( new SilentLog() );
        mojo.execute();
    }

    private RemoteRepository getMockRemoteRepository() {
        RemoteRepositoryClient repository = new RemoteRepositoryClient( repositoryManager );

        repository.setId( "test-remote-repository" );
        repository.setDisplayName( "Test Remote Repository" );
        repository.setEndpointUrl( "http://www.mock-repository.org/ota2-repository-service" );
        return repository;
    }

}
